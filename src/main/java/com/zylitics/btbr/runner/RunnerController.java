package com.zylitics.btbr.runner;

import com.google.cloud.storage.Storage;
import com.zylitics.btbr.SecretsManager;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.http.*;
import com.zylitics.btbr.http.ResponseStatus;
import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.model.TestVersion;
import com.zylitics.btbr.runner.provider.*;
import com.zylitics.btbr.webdriver.Configuration;
import com.zylitics.btbr.webdriver.session.AbstractDriverSessionProvider;
import org.elasticsearch.client.RestHighLevelClient;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Supports running multiple builds one after another. This could happen when builds are run in
 * debug mode and same VM is used for running multiple builds.
 */
@RestController
@RequestMapping("${app-short-version}/builds")
public class RunnerController {
  
  private static final Logger LOG = LoggerFactory.getLogger(RunnerController.class);
  private static final String BUILD_MAIN_THREAD_STARTS_WITH = "build_main_thread_";
  static final String STOPPED_BUILD_MAIN_THREAD_STARTS_WITH = "stopped_build_main_thread_";
  
  // a map is used rather than directly assigning current build's main thread.
  private final Map<String, Thread> threadMap = new ConcurrentHashMap<>();
  
  private final APICoreProperties apiCoreProperties;
  private final SecretsManager secretsManager;
  private final Storage storage;
  private final RestHighLevelClient restHighLevelClient;
  
  // db providers
  private final BuildProvider buildProvider;
  private final BuildStatusProvider buildStatusProvider;
  private final BuildVMProvider buildVMProvider;
  private final ImmutableMapProvider immutableMapProvider;
  private final TestVersionProvider testVersionProvider;
  
  // factories
  private final CaptureShotHandler.Factory captureShotHandlerFactory;
  private final ShotMetadataProvider.Factory shotMetadataProviderFactory;
  private final ZwlProgramOutputProvider.Factory zwlProgramOutputProviderFactory;
  private final BuildRunHandler.Factory buildRunHandlerFactory;
  
  // handlers
  private final VMDeleteHandler vmDeleteHandler;
  
  private final IOWrapper ioWrapper;
  private final Configuration configuration;
  
  @Autowired
  public RunnerController(APICoreProperties apiCoreProperties,
                          SecretsManager secretsManager,
                          Storage storage,
                          RestHighLevelClient restHighLevelClient,
                          BuildProvider buildProvider,
                          BuildStatusProvider buildStatusProvider,
                          BuildVMProvider buildVMProvider,
                          ImmutableMapProvider immutableMapProvider,
                          TestVersionProvider testVersionProvider,
                          CaptureShotHandler.Factory captureShotHandlerFactory,
                          ShotMetadataProvider.Factory shotMetadataProviderFactory,
                          ZwlProgramOutputProvider.Factory zwlProgramOutputProviderFactory) {
    this(apiCoreProperties,
        secretsManager,
        storage,
        restHighLevelClient,
        buildProvider,
        buildStatusProvider,
        buildVMProvider,
        immutableMapProvider,
        testVersionProvider,
        captureShotHandlerFactory,
        shotMetadataProviderFactory,
        zwlProgramOutputProviderFactory,
        new BuildRunHandler.Factory(),
        new VMDeleteHandler(apiCoreProperties, secretsManager, buildVMProvider),
        new IOWrapper(),
        new Configuration()
        );
  }
  
  RunnerController(APICoreProperties apiCoreProperties,
                   SecretsManager secretsManager,
                   Storage storage,
                   RestHighLevelClient restHighLevelClient,
                   BuildProvider buildProvider,
                   BuildStatusProvider buildStatusProvider,
                   BuildVMProvider buildVMProvider,
                   ImmutableMapProvider immutableMapProvider,
                   TestVersionProvider testVersionProvider,
                   CaptureShotHandler.Factory captureShotHandlerFactory,
                   ShotMetadataProvider.Factory shotMetadataProviderFactory,
                   ZwlProgramOutputProvider.Factory zwlProgramOutputProviderFactory,
                   BuildRunHandler.Factory buildRunHandlerFactory,
                   VMDeleteHandler vmDeleteHandler,
                   IOWrapper ioWrapper,
                   Configuration configuration) {
    this.apiCoreProperties = apiCoreProperties;
    this.secretsManager = secretsManager;
    this.storage = storage;
    this.restHighLevelClient = restHighLevelClient;
    this.buildProvider = buildProvider;
    this.buildStatusProvider = buildStatusProvider;
    this.buildVMProvider = buildVMProvider;
    this.immutableMapProvider = immutableMapProvider;
    this.testVersionProvider = testVersionProvider;
    this.captureShotHandlerFactory = captureShotHandlerFactory;
    this.shotMetadataProviderFactory = shotMetadataProviderFactory;
    this.zwlProgramOutputProviderFactory = zwlProgramOutputProviderFactory;
    this.buildRunHandlerFactory = buildRunHandlerFactory;
    this.vmDeleteHandler = vmDeleteHandler;
    this.ioWrapper = ioWrapper;
    this.configuration = configuration;
  }
  
  @PostMapping
  public ResponseEntity<AbstractResponse> run(
      @Validated @RequestBody RequestBuildRun requestBuildRun) throws Exception {
    LOG.info("received request to run: {}", requestBuildRun.toString());
    // validate no build is currently running
    if (threadMap.values().stream().anyMatch(Thread::isAlive)) {
      return processErrResponse(new IllegalArgumentException("Can't run a new build here because" +
          " something is already running"), HttpStatus.BAD_REQUEST);
    }
    
    Build build = null;
    try {
      // get build
      Optional<Build> b = buildProvider.getBuild(requestBuildRun.getBuildId());
      if (!b.isPresent()) {
        return processErrResponse(new IllegalArgumentException("The given buildId " +
            requestBuildRun.getBuildId() + " doesn't exists"), HttpStatus.BAD_REQUEST);
      }
      build = b.get();
      if (build.isSuccess() != null) {
        return processErrResponse(new IllegalArgumentException("The given buildId " +
            requestBuildRun.getBuildId() + " has already completed it's execution and can't run" +
            " again."), HttpStatus.BAD_REQUEST);
      }
      return run0(requestBuildRun, build);
    } catch (Throwable t) {
      // delete VM if an uncaught exception occurs before the session is created, after that
      // VM deletion is taken care by run handler.
      vmDeleteHandler.delete(build != null ? build.getBuildVMId() : null,
          requestBuildRun.getVmDeleteUrl());
      throw t;
    }
  }
  
  private ResponseEntity<AbstractResponse> run0(RequestBuildRun requestBuildRun, Build build)
      throws Exception {
    // get build capability
    BuildCapability buildCapability = build.getBuildCapability();
    LOG.debug("buildCapability is {}", buildCapability);
    // get test version
    Optional<List<TestVersion>> testVersions =
        testVersionProvider.getTestVersions(build.getBuildId());
    if (!testVersions.isPresent()) {
      return processErrResponse(new IllegalArgumentException("The given buildId " +
          requestBuildRun.getBuildId() + " has no associated tests"), HttpStatus.BAD_REQUEST);
    }
    LOG.debug("Total testVersions found {}", testVersions.get());
  
    // Create build's directory for keeping logs and test assets
    Path buildDir = Paths.get(Configuration.SYS_DEF_TEMP_DIR, "build-" + build.getBuildId());
    ioWrapper.createDirectory(buildDir);
  
    // start driver session
    Optional<AbstractDriverSessionProvider> sessionProvider =
        configuration.getSessionProviderByBrowser(apiCoreProperties.getWebdriver(),
            buildCapability, buildDir);
    if (!sessionProvider.isPresent()) {
      return processErrResponse(new IllegalArgumentException("No session provider found for the" +
          " given browser " + buildCapability.getWdBrowserName()), HttpStatus.BAD_REQUEST);
    }
    
    // The idea is to validate everything that if invalid can fail the test immediately, such as
    // no test versions with the build. Driver session can start without it as well but we should
    // validate that (and other possible things) before doing so.
    RemoteWebDriver driver = sessionProvider.get().createSession();
  
    LOG.debug("A new session {} is created", driver.getSessionId().toString());
  
    // start a new thread to run the build asynchronously because the current request will now
    // return.
    BuildRunHandler buildRunHandler = buildRunHandlerFactory.create(requestBuildRun,
        apiCoreProperties,
        secretsManager,
        storage,
        buildProvider,
        buildStatusProvider,
        buildVMProvider,
        immutableMapProvider,
        shotMetadataProviderFactory.create(apiCoreProperties, restHighLevelClient),
        zwlProgramOutputProviderFactory.create(apiCoreProperties, restHighLevelClient,
            buildCapability),
        build,
        testVersions.get(),
        captureShotHandlerFactory,
        driver,
        buildDir);
    // Note: in unit test, I can catch the current thread on buildRunHandler.handle method, store
    // it, send a stop, and check it's name change to verify.
    String mainThreadName = BUILD_MAIN_THREAD_STARTS_WITH + build.getBuildId();
    Thread buildThread = new Thread(buildRunHandler::handle, mainThreadName);
    threadMap.put(mainThreadName, buildThread);
    buildThread.start();
    LOG.debug("A new thread {} is assigned to run the build further, response will now return",
        mainThreadName);
    return ResponseEntity.status(HttpStatus.OK).body(new ResponseBuildRun()
        .setSessionId(driver.getSessionId().toString())
        .setStatus(ResponseStatus.RUNNING.name()).setHttpStatusCode(HttpStatus.OK.value()));
  }
  
  @DeleteMapping("/{buildId}")
  public ResponseEntity<AbstractResponse> stop(@PathVariable int buildId) {
    LOG.info("Getting a stop for build {}", buildId);
    Thread buildThread = threadMap.get(BUILD_MAIN_THREAD_STARTS_WITH + buildId);
    if (buildThread == null) {
      return processErrResponse(new IllegalArgumentException("There is no such thread running on" +
          " server that matches build " + buildId), HttpStatus.BAD_REQUEST);
    }
    if (!buildThread.isAlive()) {
      return processErrResponse(new IllegalArgumentException("Thread for the given buildId isn't" +
          " alive" + buildId), HttpStatus.BAD_REQUEST);
    }
    // don't interrupt thread to stop the build as it may raise InterruptedException from anywhere
    // that I don't want to handle separately. Let's change the name of the thread to something
    // that can be checked during build run and build could stop. Once build is completed and post
    // completion tasks are running, stop request won't do anything as we won't check it's name
    // change then (we don't want build to be halted once webdriver tests are completed and we're
    // on post completion tasks like saving shots/logs.)
    buildThread.setName(STOPPED_BUILD_MAIN_THREAD_STARTS_WITH + buildId);
    
    LOG.debug("The name of running thread was updated in response to a STOP, response will now" +
        " return");
    return ResponseEntity.status(HttpStatus.OK).body(new ResponseCommon()
        .setStatus(ResponseStatus.STOPPING.name()).setHttpStatusCode(HttpStatus.OK.value()));
  }
  
  /**
   * Invoked when @RequestBody binding is failed
   */
  @SuppressWarnings("unused")
  @ExceptionHandler
  public ResponseEntity<AbstractResponse> handleExceptions(MethodArgumentNotValidException ex) {
    LOG.debug("An ArgumentNotValid handler was called");
    
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
    LOG.debug("An Exception handler was called");
    
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
  
  // published when all beans are loaded
  @EventListener(ContextRefreshedEvent.class)
  void onContextRefreshedEvent() throws IOException {
    LOG.debug("ContextRefreshEvent was triggered");
    
    // Close SecretsManager once all beans that required it are loaded, as we don't need to until
    // this VM is deleted from here, where a new manager is created.
    if (secretsManager != null) {
      LOG.debug("secretsManager will now close");
      secretsManager.close();
    }
  }
}
