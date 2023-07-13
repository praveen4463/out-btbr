package com.zylitics.btbr.runner;

import com.google.cloud.storage.Storage;
import com.zylitics.btbr.SecretsManager;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.dao.DaoTestVersionProvider;
import com.zylitics.btbr.http.*;
import com.zylitics.btbr.http.ResponseStatus;
import com.zylitics.btbr.model.*;
import com.zylitics.btbr.runner.provider.*;
import com.zylitics.btbr.service.AuthService;
import com.zylitics.btbr.webdriver.Configuration;
import com.zylitics.btbr.webdriver.session.AbstractDriverSessionProvider;
import org.elasticsearch.client.RestHighLevelClient;
import org.openqa.selenium.Platform;
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
 * Supports running multiple builds one after another..
 */
@RestController
@RequestMapping("${app-short-version}/builds")
public class RunnerController {
  
  private static final Logger LOG = LoggerFactory.getLogger(RunnerController.class);
  private static final String BUILD_MAIN_THREAD_STARTS_WITH = "build_main_thread_";
  private static final String AUTHORIZATION = "Authorization";
  
  // a map is used rather than directly assigning current build's main thread.
  private final Map<Integer, BuildRunStatus> buildRunStatus = new ConcurrentHashMap<>();
  
  private final APICoreProperties apiCoreProperties;
  private final SecretsManager secretsManager;
  private final Storage storage;
  private final RestHighLevelClient restHighLevelClient;
  
  // db providers
  private final BuildProvider buildProvider;
  private final BuildRequestProvider buildRequestProvider;
  private final BuildStatusProvider buildStatusProvider;
  private final ImmutableMapProvider immutableMapProvider;
  private final BrowserProvider browserProvider;
  private final QuotaProvider quotaProvider;
  private final BuildOutputProvider buildOutputProvider;
  private final DaoTestVersionProvider daoTestVersionProvider;
  private final GitTestVersionProvider gitTestVersionProvider;
  
  // factories
  private final CaptureShotHandler.Factory captureShotHandlerFactory;
  private final ShotMetadataProvider.Factory shotMetadataProviderFactory;
  private final BuildRunHandler.Factory buildRunHandlerFactory;
  
  // handlers
  private final VMUpdateHandler vmUpdateHandler;
  private final BuildCompletionEmailHandler buildCompletionEmailHandler;
  
  // services
  private final AuthService authService;
  
  private final IOWrapper ioWrapper;
  private final Configuration configuration;
  
  @Autowired
  public RunnerController(APICoreProperties apiCoreProperties,
                          SecretsManager secretsManager,
                          Storage storage,
                          RestHighLevelClient restHighLevelClient,
                          BuildProvider buildProvider,
                          BuildRequestProvider buildRequestProvider,
                          BuildStatusProvider buildStatusProvider,
                          ImmutableMapProvider immutableMapProvider,
                          BrowserProvider browserProvider,
                          QuotaProvider quotaProvider,
                          BuildOutputProvider buildOutputProvider,
                          DaoTestVersionProvider daoTestVersionProvider,
                          GitTestVersionProvider gitTestVersionProvider,
                          CaptureShotHandler.Factory captureShotHandlerFactory,
                          ShotMetadataProvider.Factory shotMetadataProviderFactory,
                          VMUpdateHandler vmUpdateHandler,
                          BuildCompletionEmailHandler buildCompletionEmailHandler,
                          AuthService authService) {
    this(apiCoreProperties,
        secretsManager,
        storage,
        restHighLevelClient,
        buildProvider,
        buildRequestProvider,
        buildStatusProvider,
        immutableMapProvider,
        browserProvider,
        quotaProvider,
        buildOutputProvider,
        daoTestVersionProvider,
        gitTestVersionProvider,
        captureShotHandlerFactory,
        shotMetadataProviderFactory,
        new BuildRunHandler.Factory(),
        vmUpdateHandler,
        buildCompletionEmailHandler,
        new IOWrapper(),
        new Configuration(),
        authService);
  }
  
  RunnerController(APICoreProperties apiCoreProperties,
                   SecretsManager secretsManager,
                   Storage storage,
                   RestHighLevelClient restHighLevelClient,
                   BuildProvider buildProvider,
                   BuildRequestProvider buildRequestProvider,
                   BuildStatusProvider buildStatusProvider,
                   ImmutableMapProvider immutableMapProvider,
                   BrowserProvider browserProvider,
                   QuotaProvider quotaProvider,
                   BuildOutputProvider buildOutputProvider,
                   DaoTestVersionProvider daoTestVersionProvider,
                   GitTestVersionProvider gitTestVersionProvider,
                   CaptureShotHandler.Factory captureShotHandlerFactory,
                   ShotMetadataProvider.Factory shotMetadataProviderFactory,
                   BuildRunHandler.Factory buildRunHandlerFactory,
                   VMUpdateHandler vmUpdateHandler,
                   BuildCompletionEmailHandler buildCompletionEmailHandler,
                   IOWrapper ioWrapper,
                   Configuration configuration,
                   AuthService authService) {
    this.apiCoreProperties = apiCoreProperties;
    this.secretsManager = secretsManager;
    this.storage = storage;
    this.restHighLevelClient = restHighLevelClient;
    this.buildProvider = buildProvider;
    this.buildRequestProvider = buildRequestProvider;
    this.buildStatusProvider = buildStatusProvider;
    this.immutableMapProvider = immutableMapProvider;
    this.browserProvider = browserProvider;
    this.quotaProvider = quotaProvider;
    this.buildOutputProvider = buildOutputProvider;
    this.daoTestVersionProvider = daoTestVersionProvider;
    this.gitTestVersionProvider = gitTestVersionProvider;
    this.captureShotHandlerFactory = captureShotHandlerFactory;
    this.shotMetadataProviderFactory = shotMetadataProviderFactory;
    this.buildRunHandlerFactory = buildRunHandlerFactory;
    this.vmUpdateHandler = vmUpdateHandler;
    this.buildCompletionEmailHandler = buildCompletionEmailHandler;
    this.ioWrapper = ioWrapper;
    this.configuration = configuration;
    this.authService = authService;
  }
  
  @PostMapping
  public ResponseEntity<AbstractResponse> run(
      @Validated @RequestBody RequestBuildRun requestBuildRun,
      @RequestHeader(AUTHORIZATION) String authHeader) throws Exception {
    LOG.info("received request to run: {}", requestBuildRun.toString());
    // long startTime = System.nanoTime();
    if (!authService.isAuthorized(authHeader)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    // validate no build is currently running
    if (buildRunStatus.values().stream().anyMatch(b -> b == BuildRunStatus.RUNNING)) {
      return processErrResponse(new IllegalArgumentException("Can't run a new build here because" +
          " something is already running"), HttpStatus.TOO_MANY_REQUESTS);
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
      if (build.getFinalStatus() != null) {
        return processErrResponse(new IllegalArgumentException("The given buildId " +
            requestBuildRun.getBuildId() + " has already completed it's execution and can't run" +
            " again."), HttpStatus.BAD_REQUEST);
      }
      // mark the build running
      buildRunStatus.put(build.getBuildId(), BuildRunStatus.RUNNING);
      return run0(requestBuildRun, build);
    } catch (Throwable t) {
      /* cleanup when an uncaught exception occurs before the session is created, after that
         runner does it.*/
      // mark build as completed
      if (build != null) {
        buildRunStatus.put(build.getBuildId(), BuildRunStatus.COMPLETED);
        // delete VM
        vmUpdateHandler.update(build);
        // LOG error
        LOG.error(build.getBuildId() + " failed to start.", t);
      }
      // Throw to return some error response
      throw t;
    }
  }
  
  private ResponseEntity<AbstractResponse> run0(RequestBuildRun requestBuildRun, Build build)
      throws Exception {
    // get build capability
    BuildCapability buildCapability = build.getBuildCapability();
    LOG.debug("buildCapability is {}", buildCapability);
    
    // Get test version
    
    // Decide which testVersionProvider to use. If running from git is enabled and source is
    // not IDE, it's git provider otherwise the normal dao.
    TestVersionProvider testVersionProvider;
    Project project = build.getProject();
    if (build.getSourceType() != BuildSourceType.IDE && project.isGitEnabled()) {
      gitTestVersionProvider.init(project);
      testVersionProvider = gitTestVersionProvider;
    } else {
      testVersionProvider = daoTestVersionProvider;
    }
    
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
    
    // before a session is started, run our server script that will look into instance labels and
    // set things like resolution, timezone, download drivers etc. Wait until it has completed.
    if (Platform.fromString(buildCapability.getWdPlatformName()).is(Platform.WINDOWS)) {
      // script shouldn't require any specific environment, pass browser and version and any other
      // argument it may need. Note that script should call exit in the end
      // see https://stackoverflow.com/questions/15199119/runtime-exec-waitfor-doesnt-wait-until-process-is-done
      Process p = new ProcessBuilder(
          apiCoreProperties.getRunner().getWinServerBuildStartupScriptPath(),
          String.valueOf(build.getBuildId()),
          buildCapability.getWdBrowserName(),
          buildCapability.getWdBrowserVersion(),
          "\"" + build.getServerTimezone() + "\"")
          .start(); // put timezone is quotes as it has spaces
      p.waitFor();
    }
    
    // start driver session
    Optional<AbstractDriverSessionProvider> sessionProvider =
        configuration.getSessionProviderByBrowser(build, apiCoreProperties.getWebdriver(),
            buildCapability, buildDir, browserProvider);
    if (!sessionProvider.isPresent()) {
      return processErrResponse(new IllegalArgumentException("No session provider found for the" +
          " given browser " + buildCapability.getWdBrowserName()), HttpStatus.BAD_REQUEST);
    }
    
    // The idea is to validate everything that if invalid can fail the test immediately, such as
    // no test versions with the build. Driver session can start without it as well but we should
    // validate that (and other possible things) before doing so.
    RemoteWebDriver driver = sessionProvider.get().createSession();
    String sessionId = driver.getSessionId().toString();
  
    LOG.debug("A new session {} is created", sessionId);
  
    // start a new thread to run the build asynchronously because the current request will now
    // return.
    BuildRunHandler buildRunHandler = buildRunHandlerFactory.create(apiCoreProperties,
        storage,
        buildProvider,
        buildRequestProvider,
        buildStatusProvider,
        immutableMapProvider,
        quotaProvider,
        buildOutputProvider,
        testVersionProvider,
        shotMetadataProviderFactory.create(apiCoreProperties, restHighLevelClient),
        vmUpdateHandler,
        buildCompletionEmailHandler,
        build,
        testVersions.get(),
        captureShotHandlerFactory,
        driver,
        buildDir,
        buildRunStatus);
    
    if (build.getSourceType() == BuildSourceType.CI) {
      buildRunHandler.handle();
    } else {
      // Note: in unit test, I can catch the current thread on buildRunHandler.handle method, store
      // it, send a stop, and check it's name change to verify.
      String mainThreadName = BUILD_MAIN_THREAD_STARTS_WITH + build.getBuildId();
      Thread buildThread = new Thread(buildRunHandler::handle, mainThreadName);
      buildThread.setUncaughtExceptionHandler((t, e) -> LOG.error(e.getMessage(), e));
      buildThread.start();
      LOG.info("A new thread {} is assigned to run the build {} further, response will now return",
          mainThreadName, build.getBuildId());
    }
    return ResponseEntity.status(HttpStatus.OK).body(new ResponseBuildRun()
        .setSessionId(sessionId)
        .setStatus(ResponseStatus.RUNNING.name()).setHttpStatusCode(HttpStatus.OK.value()));
  }
  
  // TODO: this should change to patch
  @DeleteMapping("/{buildId}")
  public ResponseEntity<AbstractResponse> stop(@PathVariable int buildId,
                                               @RequestHeader(AUTHORIZATION) String authHeader) {
    if (!authService.isAuthorized(authHeader)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    LOG.info("Getting a stop for build {}", buildId);
    LOG.info("buildRunStatus before stop is {}", buildRunStatus);
    BuildRunStatus runningStatus = buildRunStatus.get(buildId);
    if (runningStatus == null) {
      return processErrResponse(new IllegalArgumentException("There is no such build running on" +
          " server with buildId " + buildId), HttpStatus.BAD_REQUEST);
    }
    if (runningStatus != BuildRunStatus.RUNNING) {
      return processErrResponse(new IllegalArgumentException("Given build " + buildId +
          " isn't in Running state"), HttpStatus.BAD_REQUEST);
    }
    buildRunStatus.put(buildId, BuildRunStatus.STOPPED);
    LOG.info("buildRunStatus after stop is {}", buildRunStatus);
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
  public ResponseEntity<AbstractResponse> handleExceptions(Throwable ex) {
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
