package com.zylitics.btbr.runner;

import com.google.cloud.storage.Storage;
import com.sun.tools.doclets.internal.toolkit.builders.AbstractBuilder;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.http.*;
import com.zylitics.btbr.http.ResponseStatus;
import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.model.TestVersion;
import com.zylitics.btbr.runner.provider.*;
import com.zylitics.btbr.webdriver.Configuration;
import com.zylitics.btbr.webdriver.session.AbstractDriverSessionProvider;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/*
 * We create a new app instance for every build run request this it is ok to keep global variables
 * if they are required.
 * I'm trying not to make this controller look like it is build to handle a single request and die
 * in hope that we may need to use single instance for multiple request atleast locally.
 */
@RestController
@RequestMapping("${app-short-version}/builds")
public class RunnerController {
  
  private static final Logger LOG = LoggerFactory.getLogger(RunnerController.class);
  private static final String BUILD_MAIN_THREAD_STARTS_WITH = "build_main_thread_";
  
  // a map is used rather than directly assigning current build's main thread.
  private final Map<String, Thread> threadMap = new ConcurrentHashMap<>();
  
  private final APICoreProperties apiCoreProperties;
  private final Storage storage;
  private final CaptureShotHandler.Factory captureShotHandlerFactory;
  private final BuildProvider buildProvider;
  private final BuildStatusProvider buildStatusProvider;
  private final BuildVMProvider buildVMProvider;
  private final ImmutableMapProvider immutableMapProvider;
  private final ShotMetadataProvider shotMetadataProvider;
  private final TestVersionProvider testVersionProvider;
  private final ZwlProgramOutputProvider zwlProgramOutputProvider;
  
  @Autowired
  public RunnerController(APICoreProperties apiCoreProperties,
                          Storage storage,
                          CaptureShotHandler.Factory captureShotHandlerFactory,
                          BuildProvider buildProvider,
                          BuildStatusProvider buildStatusProvider,
                          BuildVMProvider buildVMProvider,
                          ImmutableMapProvider immutableMapProvider,
                          ShotMetadataProvider shotMetadataProvider,
                          TestVersionProvider testVersionProvider,
                          ZwlProgramOutputProvider zwlProgramOutputProvider) {
    this.apiCoreProperties = apiCoreProperties;
    this.storage = storage;
    this.captureShotHandlerFactory = captureShotHandlerFactory;
    this.buildProvider = buildProvider;
    this.buildStatusProvider = buildStatusProvider;
    this.buildVMProvider = buildVMProvider;
    this.immutableMapProvider = immutableMapProvider;
    this.shotMetadataProvider = shotMetadataProvider;
    this.testVersionProvider = testVersionProvider;
    this.zwlProgramOutputProvider = zwlProgramOutputProvider;
  }
  
  @PostMapping
  public ResponseEntity<AbstractResponse> run(
      @Validated @RequestBody RequestBuildRun requestBuildRun) throws Exception {
    LOG.info("received request: {}", requestBuildRun.toString());
    
    // get build
    Optional<Build> b = buildProvider.getBuild(requestBuildRun.getBuildId());
    if (!b.isPresent()) {
      return processErrResponse(new IllegalArgumentException("The given buildId " +
          requestBuildRun.getBuildId() + " doesn't exists"), HttpStatus.BAD_REQUEST);
    }
    Build build = b.get();
    // get build capability
    BuildCapability buildCapability = build.getBuildCapability();
    // get test version
    Optional<List<TestVersion>> testVersions =
        testVersionProvider.getTestVersion(build.getBuildId());
    if (!testVersions.isPresent()) {
      return processErrResponse(new IllegalArgumentException("The given buildId " +
          requestBuildRun.getBuildId() + " has no associated tests"), HttpStatus.BAD_REQUEST);
    }
    
    // Create build's directory for keeping logs and test assets
    Path buildDir = Paths.get(Configuration.SYS_DEF_TEMP_DIR, "build-" + build.getBuildId());
    Files.createDirectory(buildDir);
    
    // start driver session
    Optional<AbstractDriverSessionProvider> sessionProvider =
        new Configuration().getSessionProviderByBrowser(apiCoreProperties.getWebdriver(),
            buildCapability, buildDir);
    if (!sessionProvider.isPresent()) {
      return processErrResponse(new IllegalArgumentException("No session provider found for the" +
          " given browser " + buildCapability.getWdBrowserName()), HttpStatus.BAD_REQUEST);
    }
    // The idea is to validate everything that if invalid can fail the test immediately, such as
    // no test versions with the build. Driver session can start without it as well but we should
    // validate that (and other possible things) before doing so.
    RemoteWebDriver driver = sessionProvider.get().createSession();
    
    // start a new thread to run the build asynchronously because the current request will now
    // return.
    BuildRunHandler buildRunHandler = new BuildRunHandler(requestBuildRun,
        apiCoreProperties,
        storage,
        captureShotHandlerFactory,
        buildStatusProvider,
        buildVMProvider,
        immutableMapProvider,
        shotMetadataProvider,
        zwlProgramOutputProvider,
        driver,
        build,
        testVersions.get(),
        buildDir);
    String mainThreadName = BUILD_MAIN_THREAD_STARTS_WITH + build.getBuildId();
    Thread buildThread = new Thread(buildRunHandler::handle, mainThreadName);
    buildThread.setUncaughtExceptionHandler((t, e) -> LOG.error(e.getMessage(), e));
    threadMap.put(mainThreadName, buildThread);
    buildThread.start();
    
    return ResponseEntity.status(HttpStatus.OK).body(new ResponseBuildRun()
        .setSessionId(driver.getSessionId().toString())
        .setStatus(ResponseStatus.RUNNING.name()).setHttpStatusCode(HttpStatus.OK.value()));
  }
  
  @GetMapping
  public ResponseEntity<AbstractResponse> stop(@RequestParam int buildId) {
    Thread buildThread = threadMap.get(BUILD_MAIN_THREAD_STARTS_WITH + buildId);
    if (buildThread == null) {
      return processErrResponse(new IllegalArgumentException("There is no such thread running on " +
          " server that matches build " + buildId), HttpStatus.BAD_REQUEST);
    }
    if (!buildThread.isAlive()) {
      return processErrResponse(new IllegalArgumentException("Thread for the given buildId isn't " +
          " alive" + buildId), HttpStatus.BAD_REQUEST);
    }
    // don't attempt to check whether build was completed, in which case an interrupt shouldn't be
    // issued as it may cause post build completion tasks to interrupt. Lets check this after build
    // completion and discard any interrupts after build is completed.
    buildThread.interrupt();
    return ResponseEntity.status(HttpStatus.OK).body(new ResponseCommon()
        .setStatus(ResponseStatus.SUCCESS.name()).setHttpStatusCode(HttpStatus.OK.value()));
  }
  
  /**
   * Invoked when @RequestBody binding is failed
   */
  @SuppressWarnings("unused")
  @ExceptionHandler
  public ResponseEntity<AbstractResponse> handleExceptions(MethodArgumentNotValidException ex) {
    return processErrResponse(ex, HttpStatus.BAD_REQUEST);
  }
  
  /**
   * Catch all exception handler for spring raised errors. Later divide it into specific errors.
   * Reference:
   * docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-exceptionhandler
   * @param ex the catched {@link Exception} type.
   * @return {@link ResponseEntity}
   */
  @SuppressWarnings("unused")
  @ExceptionHandler
  public ResponseEntity<AbstractResponse> handleExceptions(Exception ex) {
    return processErrResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
  }
  
  private ResponseEntity<AbstractResponse> processErrResponse(Throwable ex, HttpStatus status) {
    // Log exception.
    // TODO: we'll have to see what type of errors we may get here and may require more information
    //  from handlers to better debug error causes, for example the state of program when
    //  this exception occurred, the received parameters from client, etc.
    LOG.error("", ex);
    
    AbstractResponse errRes = new ResponseCommon();
    errRes.setHttpStatusCode(status.value());
    errRes.setError(ex.getMessage());
    errRes.setStatus(ResponseStatus.FAILURE.name());
    
    return ResponseEntity
        .status(status)
        .body(errRes);
  }
}
