package com.zylitics.btbr.runner;

import com.google.cloud.storage.Storage;
import com.google.common.base.Preconditions;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.*;
import com.zylitics.btbr.runner.provider.*;
import com.zylitics.btbr.util.CallbackOnlyPrintStream;
import com.zylitics.btbr.util.DateTimeUtil;
import com.zylitics.btbr.webdriver.Configuration;
import com.zylitics.btbr.webdriver.TimeoutType;
import com.zylitics.btbr.webdriver.logs.WebdriverLogHandler;
import com.zylitics.zwl.antlr4.StoringErrorListener;
import com.zylitics.zwl.api.ZwlApi;
import com.zylitics.zwl.api.ZwlWdTestProperties;
import com.zylitics.zwl.exception.ZwlLangException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BuildRunHandler {
  
  private static final Logger LOG = LoggerFactory.getLogger(BuildRunHandler.class);
  
  private final APICoreProperties apiCoreProperties;
  private final APICoreProperties.Webdriver wdProps;
  private final Storage storage;
  
  // db providers
  private final BuildProvider buildProvider;
  private final BuildRequestProvider buildRequestProvider;
  private final BuildStatusProvider buildStatusProvider;
  private final ImmutableMapProvider immutableMapProvider;
  private final QuotaProvider quotaProvider;
  
  // esdb providers
  private final ZwlProgramOutputProvider zwlProgramOutputProvider;
  
  // retrieved data
  private final Build build;
  private final BuildCapability buildCapability;
  private final List<TestVersion> testVersions;
  
  // handlers
  private final CaptureShotHandler captureShotHandler;
  private final VMUpdateHandler vmUpdateHandler;
  private final WebdriverLogHandler webdriverLogHandler;
  private final LocalAssetsToCloudHandler localAssetsToCloudHandler;
  
  // started webdriver
  private final RemoteWebDriver driver;
  
  private final Path buildDir;
  
  private final PrintStream printStream;
  
  // providers
  private final ExceptionTranslationProvider exceptionTranslationProvider;
  
  // suppliers
  private final ZwlApiSupplier zwlApiSupplier;
  
  // clock
  private final Clock clock;
  
  // -----------program state start---------------
  private final CurrentTestVersion currentTestVersion = new CurrentTestVersion();
  private final Map<Integer, TestStatus> testVersionsStatus = new HashMap<>();
  
  /*
  it's fine to have a single storingErrorListener for all test versions, even if more than one
  test version fails, fields gets reset with consecutive failure, this happens because we run
  tests in sequence never parallel.
   */
  private final StoringErrorListener storingErrorListener = new StoringErrorListener();
  
  // instants
  private Instant lastLogCheckAt;
  private Instant lastBuildStatusLineUpdateAt;
  
  // timeouts for later resetting
  private final int storedPageLoadTimeout;
  private final int storedScriptTimeout;
  private final int storedElementAccessTimeout;
  
  // referenced from RunnerController
  private final Map<Integer, BuildRunStatus> buildRunStatus;
  // -----------program state ends----------------
  
  private BuildRunHandler(APICoreProperties apiCoreProperties,
                          Storage storage,
                          BuildProvider buildProvider,
                          BuildRequestProvider buildRequestProvider,
                          BuildStatusProvider buildStatusProvider,
                          ImmutableMapProvider immutableMapProvider,
                          QuotaProvider quotaProvider,
                          ShotMetadataProvider shotMetadataProvider,
                          ZwlProgramOutputProvider zwlProgramOutputProvider,
                          VMUpdateHandler vmUpdateHandler,
                          Build build,
                          List<TestVersion> testVersions,
                          CaptureShotHandler.Factory captureShotHandlerFactory,
                          RemoteWebDriver driver,
                          Path buildDir,
                          Map<Integer, BuildRunStatus> buildRunStatus) {
    this(apiCoreProperties,
        storage,
        buildProvider,
        buildRequestProvider,
        buildStatusProvider,
        immutableMapProvider,
        quotaProvider,
        shotMetadataProvider,
        zwlProgramOutputProvider,
        build,
        testVersions,
        captureShotHandlerFactory,
        vmUpdateHandler,
        new WebdriverLogHandler(driver, apiCoreProperties.getWebdriver(),
            build.getBuildCapability(), buildDir),
        new LocalAssetsToCloudHandler(apiCoreProperties.getWebdriver(), storage, buildDir),
        driver,
        buildDir,
        Clock.systemUTC(),
        buildRunStatus,
        new ZwlApiSupplier());
  }
  
  BuildRunHandler(APICoreProperties apiCoreProperties,
                  Storage storage,
                  BuildProvider buildProvider,
                  BuildRequestProvider buildRequestProvider,
                  BuildStatusProvider buildStatusProvider,
                  ImmutableMapProvider immutableMapProvider,
                  QuotaProvider quotaProvider,
                  ShotMetadataProvider shotMetadataProvider,
                  ZwlProgramOutputProvider zwlProgramOutputProvider,
                  Build build,
                  List<TestVersion> testVersions,
                  CaptureShotHandler.Factory captureShotHandlerFactory,
                  VMUpdateHandler vmUpdateHandler,
                  WebdriverLogHandler webdriverLogHandler,
                  LocalAssetsToCloudHandler localAssetsToCloudHandler,
                  RemoteWebDriver driver,
                  Path buildDir,
                  Clock clock,
                  Map<Integer, BuildRunStatus> buildRunStatus,
                  ZwlApiSupplier zwlApiSupplier) {
    this.apiCoreProperties = apiCoreProperties;
    wdProps = apiCoreProperties.getWebdriver();
    this.storage = storage;
    this.buildProvider = buildProvider;
    this.buildRequestProvider = buildRequestProvider;
    this.buildStatusProvider = buildStatusProvider;
    this.immutableMapProvider = immutableMapProvider;
    this.quotaProvider = quotaProvider;
    this.zwlProgramOutputProvider = zwlProgramOutputProvider;
    this.build = build;
    buildCapability = build.getBuildCapability();
    this.testVersions = testVersions;
    captureShotHandler = captureShotHandlerFactory.create(apiCoreProperties.getShot(),
        shotMetadataProvider,
        storage,
        build,
        driver.getSessionId().toString(),
        currentTestVersion);
    this.vmUpdateHandler = vmUpdateHandler;
    this.webdriverLogHandler = webdriverLogHandler;
    this.localAssetsToCloudHandler = localAssetsToCloudHandler;
    this.driver = driver;
    this.buildDir = buildDir;
    printStream = new CallbackOnlyPrintStream(this::sendOutput);
    exceptionTranslationProvider = new ExceptionTranslationProvider();
    this.clock = clock;
    // check buildRunStatus has current build
    Preconditions.checkArgument(buildRunStatus.get(build.getBuildId()) != null
        && buildRunStatus.get(build.getBuildId()) == BuildRunStatus.RUNNING,
        "buildRunStatus must contain current build in Running state");
    this.buildRunStatus = buildRunStatus;
    storedPageLoadTimeout = buildCapability.getWdTimeoutsPageLoad();
    storedScriptTimeout = buildCapability.getWdTimeoutsScript();
    storedElementAccessTimeout = buildCapability.getWdTimeoutsElementAccess();
    this.zwlApiSupplier = zwlApiSupplier;
  }
  
  void handle() {
    Thread.currentThread().setUncaughtExceptionHandler((t, e) -> LOG.error(e.getMessage(), e));
    boolean stopOccurred = false;
    LOG.debug("Build should start shortly");
    try {
      run();
    } catch(Throwable t) {
      LOG.debug("An exception was thrown while running the build {}.{}",
          t.getClass().getSimpleName(), t.getMessage());
      LOG.error(t.getMessage(), t);
      if (t instanceof StopRequestException) {
        stopOccurred = true;
        // a stop has arrived while the build was running
        updateBuildStatusOnStop();
      } else {
        updateBuildStatusOnError();
      }
    } finally {
      LOG.debug("Finishing the build");
      onBuildFinish(stopOccurred);
    }
  }
  
  private void run() {
    LOG.debug("Initializing ZwlWdTestProperties");
    // get ZwlWdTestProperties
    ZwlWdTestProperties zwlWdTestProperties = new ZwlWdTestPropertiesImpl(
        wdProps,
        storage,
        build,
        driver,
        printStream,
        immutableMapProvider.getMapFromTableByBuild(build.getBuildId()
            , "bt_build_zwl_build_variables").orElse(null),
        immutableMapProvider.getMapFromTableByBuild(build.getBuildId()
            , "bt_build_zwl_preferences").orElse(null),
        buildDir,
        immutableMapProvider.getMapFromTableByBuild(build.getBuildId()
            , "bt_build_zwl_globals").orElse(null));
    
    // let's start the build
    // mark build started date
    validateSingleRowDbCommit(buildProvider.updateOnStart(build.getBuildId(),
        DateTimeUtil.getCurrent(clock)));
    boolean firstTest = true;
    for (TestVersion testVersion : testVersions) {
      LOG.debug("Starting testVersion {}", getTestVersionIdentifierShort(testVersion));
      if (!firstTest) {
        LOG.debug("Going to sanitize before running next test");
        // sanitize only after the first version is completed
        sanitizeBetweenTests();
      }
      // keep it after 'sanitizeBetweenTests' cause it sets new test versions that need to be
      // set after extra windows are closed and blank url and page is shown so that shots can start
      // new version with blank screen.
      onTestVersionStart(testVersion);
      if (firstTest) {
        LOG.debug("Going to perform one time actions during testVersion(s) run");
        // run only for the first time, keep it after 'onTestVersionStart' as this starts shot
        // process that need test version detail.
        onBuildStart();
        firstTest = false;
      }
      String code = testVersion.getCode();
      LOG.debug("Going to run the code {} for testVersion {}", code,
          getTestVersionIdentifierShort(testVersion));
      ZwlApi zwlApi = zwlApiSupplier.get(code, Collections.singletonList(storingErrorListener));
      try {
        // handle exceptions only while reading the code, other exceptions will be relayed to
        // handle()
        zwlApi.interpret(zwlWdTestProperties,
            z -> z.setLineChangeListener(this::onZwlProgramLineChanged));
      } catch (StopRequestException s) {
        throw s;
      } catch (Throwable t) {
        LOG.debug("An exception occurred while running testVersion {}: {}.{}",
            getTestVersionIdentifierShort(testVersion), t.getClass().getSimpleName(), t.getMessage());
        onTestVersionFailed(testVersion, t);
        // try to run other versions only when the exception is a ZwlLangException, cause it's very
        // unlikely any other test will pass when there is a problem in our application that caused
        // an unknown exception.
        if (t instanceof ZwlLangException && !build.isAbortOnFailure()) {
          LOG.debug("Will continue running from next testVersion after an error in {}",
              getTestVersionIdentifierShort(testVersion));
          // when we continue, log the exception.
          LOG.error(t.getMessage(), t);
          continue;
        }
        LOG.debug("Will not continue to next testVersion, throwing exception");
        throw t; // handle() will catch it
      }
      LOG.debug("testVersion {} was successful", getTestVersionIdentifierShort(testVersion));
      onTestVersionSuccess(testVersion);
    }
    // once build is completed, even with errors, handle() will take care of it.
  }
  
  // order of actions matter, they are in priority
  private void onZwlProgramLineChanged(int currentLine) {
    LOG.debug("onZwlProgramLineChanged invoked for testVersion {}, line {}",
        currentTestVersion.getTestVersionId(), currentLine);
    
    // set line to currentTestVersion so that shots process can take it.
    currentTestVersion.setControlAtLineInProgram(currentLine);
    
    // I've use this for two things, checking arrival of a STOP and line update. Checking the
    // arrival doesn't have to be done on every line change, and don't want to keep another timer
    // for simplicity, just use this for now.
    // push build status line update after a delay
    if (ChronoUnit.MILLIS.between(lastBuildStatusLineUpdateAt, clock.instant()) >=
        apiCoreProperties.getRunner().getUpdateLineBuildStatusAfter()) {
      // check if we can't move forward
      // Note: We don't want build to be halted once webdriver tests are completed and we're
      // on post completion tasks like saving shots/logs, thus this is checked only here.
      if (buildRunStatus.get(build.getBuildId()) == BuildRunStatus.STOPPED) {
        LOG.debug("A stop request has arrived while running testVersion {}",
            currentTestVersion.getTestVersionId());
        // a stop request arrived, handle() will catch the thrown exception.
        throw new StopRequestException("A STOP was requested");
      }
      
      LOG.debug("Pushing a line update for testVersion {}, line {}",
          currentTestVersion.getTestVersionId(), currentLine);
      int result = buildStatusProvider.updateLine(new BuildStatusUpdateLine(build.getBuildId(),
          currentTestVersion.getTestVersionId(), currentLine));
      validateSingleRowDbCommit(result);
      // reset to current instant
      lastBuildStatusLineUpdateAt = clock.instant();
    }
    
    // !! I don't think we need a line change message to be pushed as all webdriver functions
    // push a message on begin with line number, but if required do it from here.
    
    // for webdriver logs, check if sufficient time has been passed since we last captured logs, if
    // so capture them again
    if (ChronoUnit.MILLIS.between(lastLogCheckAt, clock.instant()) >=
        wdProps.getWaitBetweenLogsCapture()) {
      LOG.debug("Capturing logs in onZwlProgramLineChanged");
      webdriverLogHandler.capture();
      // reset to current instant
      lastLogCheckAt = clock.instant();
    }
  }
  
  private void sanitizeBetweenTests() {
    if (build.isAetKeepSingleWindow()) {
      LOG.debug("Cleaning up opened windows, maximizing browser..");
      // delete any open windows and leave just one with about:blank, delete all cookies before
      // reading new test
      List<String> winHandles = new ArrayList<>(driver.getWindowHandles());
      LOG.debug("Found total windows {}, will close extra windows and keep single",
          winHandles.size());
      if (winHandles.size() > 1) {
        for (int i = 0; i < winHandles.size(); i++) {
          driver.switchTo().window(winHandles.get(i));
          if (i < winHandles.size() - 1) {
            driver.close();
          }
        }
      }
      // maximizing and resetting url takes affect only when keep single window is true.
      if (buildCapability.isWdBrwStartMaximize()) {
        LOG.debug("Maximizing the window");
        driver.manage().window().maximize();
      }
      if (build.isAetUpdateUrlBlank()) {
        LOG.debug("Setting up blank url to window");
        driver.get("about:blank"); // "about local scheme" can be given to 'get' per webdriver spec
      }
    }
    if (build.isAetDeleteAllCookies()) {
      LOG.debug("Deleting all cookies");
      driver.manage().deleteAllCookies(); // delete all cookies
    }
    if (build.isAetResetTimeouts()) {
      LOG.debug("Resetting timeouts");
      // reset build capability timeouts to the original values
      buildCapability.setWdTimeoutsElementAccess(storedElementAccessTimeout);
      buildCapability.setWdTimeoutsPageLoad(storedPageLoadTimeout);
      buildCapability.setWdTimeoutsScript(storedScriptTimeout);
      // reset driver timeouts to their default
      Configuration configuration = new Configuration();
      driver.manage().timeouts().pageLoadTimeout(
          configuration.getTimeouts(wdProps, buildCapability, TimeoutType.PAGE_LOAD),
          TimeUnit.MILLISECONDS);
      driver.manage().timeouts().setScriptTimeout(
          configuration.getTimeouts(wdProps, buildCapability, TimeoutType.JAVASCRIPT),
          TimeUnit.MILLISECONDS);
    }
  }
  
  // Order is precise, db interactions are on top so that if it fails, we don't mark the version
  // Running
  private void onTestVersionStart(TestVersion testVersion) {
    LOG.debug("onTestVersionStart invoked for testVersion {}",
        getTestVersionIdentifierShort(testVersion));
    
    validateSingleRowDbCommit(buildStatusProvider.saveOnStart(
        new BuildStatusSaveOnStart(build.getBuildId(), testVersion.getTestVersionId(),
            TestStatus.RUNNING, DateTimeUtil.getCurrent(clock), build.getUserId())));
  
    // set the line to 0 when a new version starts, we do this after test is sanitize and just
    // one window is there with blank url, thus it's safe to change the version. It's ok if a few
    // shots go with line 0 as the test has not really yet started, once it has started line would
    // already have changed.
    currentTestVersion.setTestVersionId(testVersion.getTestVersionId())
        .setControlAtLineInProgram(0);
  
    testVersionsStatus.put(testVersion.getTestVersionId(), TestStatus.RUNNING);
    
    printStream.println("Executing test " + getTestVersionIdentifierLong(testVersion));
  
    // assign an instant back in time so that first time line update go without any wait
    lastBuildStatusLineUpdateAt = clock.instant()
        .minusMillis(apiCoreProperties.getRunner().getUpdateLineBuildStatusAfter());
    
    LOG.debug("onTestVersionStart completed for testVersion {}",
        getTestVersionIdentifierShort(testVersion));
  }
  
  // do things that require only one time execution/invocation on build start
  private void onBuildStart() {
    LOG.debug("onBuildStart invoked");
    // maximize the driver window if user didn't say otherwise
    if (buildCapability.isWdBrwStartMaximize()) {
      LOG.debug("Maximizing browser window");
      driver.manage().window().maximize();
    }
    
    // begin capturing shot
    LOG.debug("Starting shots capture");
    captureShotHandler.startShot();
    
    // assign current instant to log capture instant, so that log capture waits for sometime
    // from now before trying capturing.
    lastLogCheckAt = clock.instant();
  }
  
  private void onTestVersionFailed(TestVersion testVersion, Throwable t) {
    LOG.debug("onTestVersionFailed invoked for testVersion {}, exception {}",
        getTestVersionIdentifierShort(testVersion),
        t.getMessage());
    // we do this to make sure the version we're marking error was first marked running and
    // actually had an entry in BuildStatus
    validateTestVersionRunning(testVersion.getTestVersionId());
  
    // once a version's execution is done, push a message, don't use printStream as we need to send
    // another argument.
    String outputMsg = "Exception occurred during execution of test " +
        getTestVersionIdentifierLong(testVersion);
    sendOutput(outputMsg, true); // don't add exMessage as that will be pushed in build status,
    // adding here would show that twice to user.
    
    // push all pending output and wait for it to finish so that once a version completes,
    // user has seen all it's output
    zwlProgramOutputProvider.processRemaining();
    
    String exMessage = exceptionTranslationProvider.get(t);
    LOG.debug("Translated error message is {}", exMessage);
    String fromPos = null;
    String toPos = null;
    if (t instanceof ZwlLangException) {
      ZwlLangException ex = (ZwlLangException) t; // all exceptions either derive from or wrap in ZwlLangException
      fromPos = ex.getFromPos();
      toPos = ex.getToPos();
    } else {
      LOG.warn("Expected " + ZwlLangException.class.getSimpleName() + " but was " +
          t.getClass().getSimpleName(), t);
    }
    
    // update build status
    updateBuildStatus(testVersion.getTestVersionId(), TestStatus.ERROR, exMessage, fromPos,
        toPos);
    
    // Now mark this test version as error
    testVersionsStatus.put(testVersion.getTestVersionId(), TestStatus.ERROR);
    LOG.debug("current testVersionStatus is {}", testVersionsStatus);
  }
  
  private void onTestVersionSuccess(TestVersion testVersion) {
    LOG.debug("onTestVersionSuccess invoked for testVersion {}",
        getTestVersionIdentifierShort(testVersion));
    // we do this to make sure the version we're marking success was first marked running and
    // actually had an entry in BuildStatus
    validateTestVersionRunning(testVersion.getTestVersionId());
  
    // once a version's execution is done, push a message, don't use printStream as we need to send
    // another argument.
    sendOutput("Completed execution for test " + getTestVersionIdentifierLong(testVersion),
        true);
    
    // push all pending output and wait for it to finish so that once a version completes,
    // user has seen all it's output
    zwlProgramOutputProvider.processRemaining();
    
    // update build status
    updateBuildStatus(testVersion.getTestVersionId(), TestStatus.SUCCESS);
    
    // Now mark this test version as completed
    testVersionsStatus.put(testVersion.getTestVersionId(), TestStatus.SUCCESS);
  }
  
  private void validateTestVersionRunning(int testVersionId) {
    LOG.debug("Validating testVersionId {} is actually Running", testVersionId);
    TestStatus currentStatus = testVersionsStatus.get(testVersionId);
    Preconditions.checkNotNull(currentStatus, "testVersionId " + testVersionId +
        " doesn't have a state right now");
    
    if (currentStatus != TestStatus.RUNNING) {
      throw new RuntimeException(String.format("testVersionId: %s is not in RUNNING status." +
          " testVersionsStatus: %s", testVersionId, testVersionsStatus));
    }
  }
  
  // none of the task should throw exception so that next one can run
  // all tasks should run independent of the result of any of previous task
  // !! the order of execution is precise and based on priority
  // The first priorities are shots and output because these are the things user may be watching
  // while test is running and should be committed asap. Capturing logs can wait as it's not needed
  // in real time. Quitting the driver can happen late as it doesn't matter even if the browser
  // window is left open, we're not running anything after reaching this step anyway.
  /*
  TODO: Sometimes when last test version finishes, it's very last commands might not have resulted
   in browser render yet, for example code tries to change something on page, webdriver detects the
   change but hasn't rendered the change yet. Post last version, shots are stopped immediately, thus
   the final render may have left from capturing. I feel we need to wait for sometime before
   stopping the shots so that any unrendered change has rendered and captured for user's eyes.
   Right now, I can only think of a raw wait for some random time because we can't know what needs
   to be rendered to wait for. For now, let's not add any wait, but if you notice this happening
   to user's production tests, add it just before stopping the shots, perhaps 2-5 seconds is enough.
   I could've also stopped the shots after
   */
  private void onBuildFinish(boolean stopOccurred) {
    LOG.debug("onBuildFinish was invoked");
    
    // update build, this marks finish of build but build tasks are not yet completed and that will
    // be marked separately just before vm is turned off or marked free.
    updateBuildOnFinish(stopOccurred);
    
    // if build source is IDE, we must mark build request record done here so that another IDE build
    // could start.
    if (build.getSourceType() == BuildSourceType.IDE) {
      markBuildRequestCompleted();
    }
    
    
    // flush program output, this shouldn't have anything to flush as we do it before the end of
    // each version.
    LOG.debug("pushing program output and waiting");
    zwlProgramOutputProvider.processRemainingAndTearDown();
  
    /*
    Reason why shots are stopped after pushing output and not in beginning:
    Sometimes when last test version finishes, it's very last commands might not have resulted
    in browser render yet, for example code tries to change something on page, webdriver detects the
    change but hasn't rendered the change yet. Post last version, if shots are stopped immediately,
    the final render may have left from capturing. I feel we need to wait for sometime before
    stopping the shots so that any unrendered change has rendered and captured for user's eyes.
    This may be a perfect place to do so, without having to put a raw wait, while program output is
    being saved into ESDB, shots may continue to capture the opened browser window. This may
    result in some redundant shots when program output saving takes more time than expected and
    shots continue to come of the same browser state due to everything already loaded. I am
    expecting ESDB output save to be quick thus preventing lot of redundant shots.
    TODO: Keep an eye here
     */
    // stop shots
    LOG.debug("Shots are going to stop");
    captureShotHandler.stopShot(); // takes no time
    
    // flush shots, may block long time.
    LOG.debug("pushing shots and waiting");
    captureShotHandler.blockUntilFinish();
    
    // capture logs final time before quit
    LOG.debug("capturing logs one last time");
    webdriverLogHandler.capture();
    
    // quit the driver.
    LOG.debug("Quitting the driver");
    driver.quit();
    
    // store logs
    LOG.debug("storing capture logs to cloud");
    localAssetsToCloudHandler.store();
    
    // all done, we can mark all build tasks completed
    updateOnAllTaskDone();
    
    // if source is other than IDE, complete build request here
    if (build.getSourceType() != BuildSourceType.IDE) {
      markBuildRequestCompleted();
    }
    
    // mark current build as completed
    buildRunStatus.put(build.getBuildId(), BuildRunStatus.COMPLETED);
    
    // update VM
    LOG.debug("updating the VM");
    vmUpdateHandler.update(build);
  }
  
  private void updateBuildOnFinish(boolean stopOccurred) {
    LOG.debug("updateBuildOnFinish was invoked");
    boolean isSuccess = false;
    String exMsg = null;
    boolean allSuccess = testVersionsStatus.values().stream()
        .allMatch(e -> e == TestStatus.SUCCESS);
    if (allSuccess && testVersionsStatus.keySet().containsAll(testVersions.stream()
        .map(TestVersion::getTestVersionId).collect(Collectors.toList()))) {
      isSuccess = true;
    } else {
      if (stopOccurred) {
        exMsg = "A STOP request was issued";
      } else if (currentTestVersion.getTestVersionId() == 0) {
        exMsg = "Unexpected error occurred before any test version could start running";
      } else {
        exMsg = "An exception occurred, check test version(s) of this build for details";
      }
    }
    LOG.debug("was the build succeeded? {}, if no, the derived error is {}", isSuccess, exMsg);
    // don't throw an exception from here
    try {
      validateSingleRowDbCommit(buildProvider.updateOnComplete(new BuildUpdateOnComplete(
          build.getBuildId(), DateTimeUtil.getCurrent(clock), isSuccess, exMsg)));
    } catch (Throwable t) {
      LOG.error(t.getMessage(), t);
    }
  }
  
  private void updateOnAllTaskDone() {
    // don't throw an exception from here
    try {
      validateSingleRowDbCommit(buildProvider.updateOnAllTasksDone(build.getBuildId(),
          DateTimeUtil.getCurrent(clock)));
      validateSingleRowDbCommit(quotaProvider.updateConsumed(build,
          DateTimeUtil.getCurrentLocal(clock)));
    } catch (Throwable t) {
      LOG.error(t.getMessage(), t);
    }
  }
  
  private void updateBuildStatusOnStop() {
    // it won't happen that stop occurs before any version could start because we check stop during
    // a version run.
    LOG.debug("updateBuildStatusOnStop was invoked");
    // first do everything we do when a version completes, i.e pushing a final output, waiting for
    // all output to commit, update build status, marking testVersionStatus.
    int testVersionId = currentTestVersion.getTestVersionId();
    validateTestVersionRunning(testVersionId);
    sendOutput("Stopping...", true);
    // push all pending output and wait for it to finish so that once a version completes,
    // user has seen all it's output
    zwlProgramOutputProvider.processRemaining();
    updateBuildStatus(testVersionId, TestStatus.STOPPED);
    testVersionsStatus.put(testVersionId, TestStatus.STOPPED);
    // Once done, save other versions that are stopped due to current version stopping without
    // starting.
    saveTestVersionsNotRun(TestStatus.STOPPED);
    // status is not aborted, because when stop was requested, all tests in queue were also forced
    // to stop, this is an explicit request rather than implicit error that causes abort.
  }
  
  private void updateBuildStatusOnError() {
    LOG.debug("updateBuildStatusOnError was invoked");
    /*
     Just save version those didn't run because the current version should already have been
     handled in onTestVersionFailed. There may be edge cases when current version is left
     unhandled, such as an exception occurred in onTestVersionStart and onBuildStart after marking
     version as 'Running' but it's very unlikely and not handled, if this happens we will know
     in logs.
     */
    saveTestVersionsNotRun(TestStatus.ABORTED);
  }
  
  // end date, start date, error are null for tests that couldn't run. status could be either
  // ABORTED or STOPPED
  private void saveTestVersionsNotRun(TestStatus status) {
    LOG.debug("Going to save testVersions couldn't run and assigning status {}", status);
    testVersions.forEach(t -> {
      if (!testVersionsStatus.containsKey(t.getTestVersionId())) {
        LOG.debug("testVersionId {} couldn't be run, saving", t.getTestVersionId());
        validateSingleRowDbCommit(buildStatusProvider.saveWontStart(
            new BuildStatusSaveWontStart(build.getBuildId(), t.getTestVersionId(), status,
                build.getUserId())));
      }
    });
  }
  
  private void updateBuildStatus(int testVersionId, TestStatus status, @Nullable String error,
                                 @Nullable String errorFromPos, @Nullable String errorToPos) {
    LOG.debug("Updating buildStatus for testVersionId {} to status {}, error {}", testVersionId,
        status, error);
    validateSingleRowDbCommit(buildStatusProvider.updateOnEnd(new BuildStatusUpdateOnEnd(
        build.getBuildId(), testVersionId, status, DateTimeUtil.getCurrent(clock), error,
        errorFromPos, errorToPos)));
  }
  
  private void updateBuildStatus(int testVersionId, TestStatus status) {
    updateBuildStatus(testVersionId, status, null, null, null);
  }
  
  private void sendOutput(String message) {
    sendOutput(message, false);
  }
  
  // Runner should push a message with versionEndedMessage=true when it has fully executed a test
  // version, something like "Completed execution for test version <name>"
  private void sendOutput(String message, boolean versionEndedMessage) {
    LOG.debug("Sending output message {}, last message for version? {}", message,
        versionEndedMessage);
    ZwlProgramOutput zwlProgramOutput = new ZwlProgramOutput()
        .setBuildId(build.getBuildId())
        .setTestVersionId(currentTestVersion.getTestVersionId())
        .setOutput(message)
        .setCreateDate(DateTimeUtil.getCurrent(clock))
        .setEnded(versionEndedMessage);
    zwlProgramOutputProvider.saveAsync(zwlProgramOutput);
  }
  
  private void validateSingleRowDbCommit(int result) {
    if (result != 1) {
      throw new RuntimeException("Expected one row to be affected but it was " + result);
    }
  }
  
  private String getTestVersionIdentifierShort(TestVersion testVersion) {
    return String.format("%s:%s:%s", testVersion.getFile().getFileId(),
        testVersion.getTest().getTestId(), testVersion.getTestVersionId());
  }
  
  private String getTestVersionIdentifierLong(TestVersion testVersion) {
    return String.format("%s>%s>%s", testVersion.getFile().getName(),
        testVersion.getTest().getName(), testVersion.getName());
  }
  
  private void markBuildRequestCompleted() {
    try {
      validateSingleRowDbCommit(buildRequestProvider.markBuildRequestCompleted(
          build.getBuildRequestId()));
    } catch (Throwable t) {
      LOG.error(t.getMessage(), t);
    }
  }
  
  static class Factory {
    
    BuildRunHandler create(APICoreProperties apiCoreProperties,
                           Storage storage,
                           BuildProvider buildProvider,
                           BuildRequestProvider buildRequestProvider,
                           BuildStatusProvider buildStatusProvider,
                           ImmutableMapProvider immutableMapProvider,
                           QuotaProvider quotaProvider,
                           ShotMetadataProvider shotMetadataProvider,
                           ZwlProgramOutputProvider zwlProgramOutputProvider,
                           VMUpdateHandler vmUpdateHandler,
                           Build build,
                           List<TestVersion> testVersions,
                           CaptureShotHandler.Factory captureShotHandlerFactory,
                           RemoteWebDriver driver,
                           Path buildDir,
                           Map<Integer, BuildRunStatus> buildRunStatus) {
      return new BuildRunHandler(apiCoreProperties,
          storage,
          buildProvider,
          buildRequestProvider,
          buildStatusProvider,
          immutableMapProvider,
          quotaProvider,
          shotMetadataProvider,
          zwlProgramOutputProvider,
          vmUpdateHandler,
          build,
          testVersions,
          captureShotHandlerFactory,
          driver,
          buildDir,
          buildRunStatus);
    }
  }
}
