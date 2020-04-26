package com.zylitics.btbr.runner;

import com.google.cloud.storage.Option;
import com.google.cloud.storage.Storage;
import com.google.common.base.Strings;
import com.zylitics.btbr.SecretsManager;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.http.RequestBuildRun;
import com.zylitics.btbr.model.*;
import com.zylitics.btbr.runner.provider.*;
import com.zylitics.btbr.runner.task.LocalAssetsToCloud;
import com.zylitics.btbr.util.CallbackOnlyPrintStream;
import com.zylitics.btbr.util.DateTimeUtil;
import com.zylitics.btbr.webdriver.logs.WebdriverLogHandler;
import com.zylitics.zwl.antlr4.StoringErrorListener;
import com.zylitics.zwl.api.ZwlApi;
import com.zylitics.zwl.api.ZwlInterpreterVisitor;
import com.zylitics.zwl.exception.ZwlLangException;
import org.antlr.v4.runtime.RecognitionException;
import org.openqa.selenium.WebDriverException;
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
  
  private final RequestBuildRun requestBuildRun;
  private final APICoreProperties apiCoreProperties;
  private final APICoreProperties.Webdriver wdProps;
  private final Storage storage;
  private final CaptureShotHandler captureShotHandler;
  private final BuildProvider buildProvider;
  private final BuildStatusProvider buildStatusProvider;
  private final ImmutableMapProvider immutableMapProvider;
  private final ZwlProgramOutputProvider zwlProgramOutputProvider;
  private final RemoteWebDriver driver;
  private final Build build;
  private final BuildCapability buildCapability;
  private final List<TestVersion> testVersions;
  private final Path buildDir;
  private final PrintStream printStream;
  
  // keep timeouts for resetting later
  private final int storedPageLoadTimeout;
  private final int storedScriptTimeout;
  private final int storedElementAccessTimeout;
  private final WebdriverLogHandler webdriverLogHandler;
  private final VMDeleteHandler vmDeleteHandler;
  
  private final CurrentTestVersion currentTestVersion = new CurrentTestVersion();
  // it's fine to have a single storingErrorListener for all test versions, even if more than one
  // test version fails, fields gets reset with consecutive failure, this happens because we run
  // tests in sequence never parallel.
  private final StoringErrorListener storingErrorListener = new StoringErrorListener();
  private final Map<Integer, TestStatus> testVersionsStatus = new HashMap<>();
  
  private final Clock clock;
  
  private Instant lastLogCheckAt;
  
  private Instant lastBuildStatusLineUpdateAt;
  
  public BuildRunHandler(RequestBuildRun requestBuildRun,
                         APICoreProperties apiCoreProperties,
                         SecretsManager secretsManager,
                         Storage storage,
                         CaptureShotHandler.Factory captureShotHandlerFactory,
                         BuildProvider buildProvider,
                         BuildStatusProvider buildStatusProvider,
                         BuildVMProvider buildVMProvider,
                         ImmutableMapProvider immutableMapProvider,
                         ShotMetadataProvider shotMetadataProvider,
                         ZwlProgramOutputProvider zwlProgramOutputProvider,
                         RemoteWebDriver driver,
                         Build build,
                         List<TestVersion> testVersions,
                         Path buildDir) {
    this.requestBuildRun = requestBuildRun;
    this.apiCoreProperties = apiCoreProperties;
    wdProps = apiCoreProperties.getWebdriver();
    this.storage = storage;
    this.buildProvider = buildProvider;
    this.buildStatusProvider = buildStatusProvider;
    this.immutableMapProvider = immutableMapProvider;
    this.zwlProgramOutputProvider = zwlProgramOutputProvider;
    this.driver = driver;
    this.build = build;
    this.testVersions = testVersions;
    this.buildDir = buildDir;
    
    buildCapability = build.getBuildCapability();
    printStream = new CallbackOnlyPrintStream(this::sendOutput);
  
    captureShotHandler = captureShotHandlerFactory.create(apiCoreProperties,
        shotMetadataProvider,
        storage,
        build,
        driver.getSessionId().toString(),
        buildCapability.getShotBucketSessionStorage(),
        currentTestVersion);
    webdriverLogHandler = new WebdriverLogHandler(driver, wdProps, buildCapability, buildDir);
    vmDeleteHandler = new VMDeleteHandler(apiCoreProperties, secretsManager, storage,
        buildVMProvider);
    
    storedPageLoadTimeout = buildCapability.getWdTimeoutsPageLoad();
    storedScriptTimeout = buildCapability.getWdTimeoutsScript();
    storedElementAccessTimeout = buildCapability.getWdTimeoutsElementAccess();
    
    clock = Clock.systemUTC();
  }
  
  void handle() {
    boolean stopOccurred = false;
    try {
      run();
    } catch(Throwable t) {
      if (t instanceof StopRequestException) {
        stopOccurred = true;
        // a stop has arrived while the build was running
        updateBuildStatusOnStop();
      } else {
        String exMessage = translateExToUserReadableMsg(t);
        updateBuildStatusOnError(exMessage);
      }
      //ZwlLangException or WebdriverException should be relayed as is to user
      // handle exceptions, relay to user appropriate message by pushing message against the current
      // versionId, if no versionId is in current, means we got error before running test thus just
      // push 'didn't start due to error' message
      
      // interpreted exception will be written against the version that is currently running.
      
      // build will get just 'stop requested' or 'exception occurred' etc messages.
      LOG.error(t.getMessage(), t);
    } finally {
      onBuildFinish(stopOccurred);
    }
  }
  
  // none of the task should throw exception so that next one can run
  // all tasks should run independent of the result of any of previous task
  private void onBuildFinish(boolean stopOccurred) {
    // stop shots
    captureShotHandler.stopShot(); // takes not time
    
    // flush program output
    zwlProgramOutputProvider.processRemainingAndTearDown(); // blocks until all output is pushed
    
    // update build
    updateBuildOnFinish(stopOccurred);
    
    // capture logs final time before quit
    webdriverLogHandler.capture();
    
    // quit the driver
    driver.quit();
    
    // flush shots
    captureShotHandler.blockUntilFinish();
    
    // store logs
    new LocalAssetsToCloud(wdProps, storage, buildDir).store();
  
    // delete VM
    vmDeleteHandler.delete(build.getBuildVMId(), requestBuildRun.getVmDeleteUrl());
  }
  
  private void updateBuildOnFinish(boolean stopOccurred) {
    boolean isSuccess = false;
    String exMsg = "";
    boolean allSuccess = testVersionsStatus.values().stream()
        .allMatch(e -> e == TestStatus.SUCCESS);
    if (allSuccess && testVersionsStatus.keySet().containsAll(testVersions.stream()
        .map(TestVersion::getTestVersionId).collect(Collectors.toList()))) {
      isSuccess = true;
    } else {
      if (stopOccurred) {
        exMsg = "A STOP request was issued";
      } else if (currentTestVersion.getTestVersionId() == 0) {
        exMsg = "Unexpected exception occurred before initiating the build";
      } else {
        exMsg = "An exception occurred, check test versions of this build for detail";
      }
    }
    
    Build buildUpdate = new Build()
        .setBuildId(build.getBuildId())
        .setSuccess(isSuccess)
        .setError(exMsg)
        .setEndDate(DateTimeUtil.getCurrentUTC());
    // don't throw an exception from here
    try {
      validateSingleRowDbCommit(buildProvider.updateBuild(buildUpdate));
    } catch (Throwable t) {
      LOG.error(t.getMessage(), t);
    }
  }
  
  private void updateBuildStatusOnError(String error) {
    Optional<Integer> running = getRunningVersion();
    running.ifPresent(e -> {
      // I don't expect a Running version while an exception occurs because when it happens,
      // onTestVersionFailed runs that does all updates and makes the status ERROR, thus log this
      // to keep a watch if this happens.
      LOG.warn("A running test is found on exception whereas it should've already processed");
      updateBuildStatus(e, TestStatus.ERROR, error);
    });
    saveTestVersionsNotRun(TestStatus.ABORTED);
  }
  
  private void updateBuildStatusOnStop() {
    Optional<Integer> running = getRunningVersion();
    running.ifPresent(e -> updateBuildStatus(e, TestStatus.STOPPED,
        "Forcefully stopped while running"));
    saveTestVersionsNotRun(TestStatus.STOPPED);
    // status is not aborted, because when stop was requested, all tests in queue were also forced
    // to stop, this is an explicit request rather than implicit error that causes abort.
  }
  
  private Optional<Integer> getRunningVersion() {
    List<Integer> all = testVersionsStatus.entrySet().stream()
        .filter(e -> e.getValue() == TestStatus.RUNNING).map(Map.Entry::getKey)
        .collect(Collectors.toList());
    // shouldn't happen but still log
    if (all.size() > 1) {
      LOG.error("There are more than one running versions found, testVersionsStatus: "
          + testVersionsStatus);
    }
    return all.size() == 1 ? Optional.of(all.get(0)) : Optional.empty();
  }
  
  private void run() {
    // initialize things
    zwlProgramOutputProvider.setBuildCapability(buildCapability);
    
    // get Zwl interpreter visitor
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    ZwlInterpreterVisitor zwlInterpreterVisitor = new InterpreterVisitorProvider(
        wdProps,
        storage,
        this::onZwlProgramLineChanged,
        build,
        driver,
        printStream,
        immutableMapProvider.getMapFromTable("zwl_preferences").get(),
        buildDir,
        immutableMapProvider.getMapFromTable("zwl_globals").get())
        .get();
    
    // let's start the build
    boolean firstTest = true;
    for (TestVersion testVersion : testVersions) {
      if (!firstTest) {
        // sanitize only after the first version is completed
        sanitizeBetweenTests();
      }
      // keep it after 'sanitizeBetweenTests' cause it sets new test versions that need to be
      // set after extra windows are closed and blank url and page is shown.
      onTestVersionStart(testVersion);
      if (firstTest) {
        // run only for the first time, keep it after 'onTestVersionStart' as this starts shot
        // process that need test version detail.
        onBuildStart();
        firstTest = false;
      }
      String code = testVersion.getZwlProgram().getCode();
      ZwlApi zwlApi = new ZwlApi(code, Collections.singletonList(storingErrorListener));
      try {
        // handle exceptions only while reading the code, other exceptions will be relayed to
        // handle()
        zwlApi.interpret(zwlInterpreterVisitor);
      } catch (Throwable t) {
        onTestVersionFailed(testVersion, t);
        // try to run other versions only when the exception is a ZwlLangException, cause it's very
        // unlikely any other test will pass when there is a problem in our application that caused
        // an unknown exception.
        if (t instanceof ZwlLangException && !buildCapability.isBuildAbortOnFailure()) {
          // when we continue, log the exception.
          LOG.error(t.getMessage(), t);
          continue;
        }
        throw t; // handle() will catch it
      }
      onTestVersionSuccess(testVersion);
    }
    // once build is completed, even with errors, handle() will take care of it.
  }
  
  // order of actions matter, they are in priority
  private void onZwlProgramLineChanged(int currentLine) {
    // check if we can't move forward
    if (Thread.currentThread().getName().equals(
        RunnerController.STOPPED_BUILD_MAIN_THREAD_STARTS_WITH + build.getBuildId())) {
      // a stop request arrived, handle() will catch the thrown exception.
      throw new StopRequestException("A STOP was requested");
    }
    
    // set line to currentTestVersion so that shots process can take it.
    currentTestVersion.setControlAtLineInProgram(currentLine);
  
    // push build status line update after a delay
    if (ChronoUnit.MILLIS.between(lastBuildStatusLineUpdateAt, clock.instant()) >=
        apiCoreProperties.getRunner().getUpdateLineBuildStatusAfter()) {
      int result = buildStatusProvider.updateLine(new BuildStatus()
          .setBuildId(build.getBuildId())
          .setTestVersionId(currentTestVersion.getTestVersionId())
          .setZwlExecutingLine(currentLine));
      validateSingleRowDbCommit(result);
      lastBuildStatusLineUpdateAt = clock.instant();
    }
    
    // !! I don't think we need a line change message to be pushed as all webdriver functions
    // push a message on begin with line number, but if required do it from here.
    
    // for webdriver logs, check if sufficient time has been passed since we last captured logs, if
    // so capture them again
    if (ChronoUnit.MILLIS.between(lastLogCheckAt, clock.instant()) >=
        wdProps.getWaitBetweenLogsCapture()) {
      webdriverLogHandler.capture();
      // reset to current instant
      lastLogCheckAt = clock.instant();
    }
  }
  
  // do things that require only one time execution/invocation on build start
  private void onBuildStart() {
    // maximize the driver window if user didn't say otherwise
    if (buildCapability.isWdBrwStartMaximize()) {
      driver.manage().window().maximize();
    }
    
    // begin capturing shot
    captureShotHandler.startShot();
    
    // assign current instant to log capture instant, so that log capture waits for sometime
    // from now before trying capturing.
    lastLogCheckAt = clock.instant();
    
    // assign an instant so that first time line update go without any wait
    lastBuildStatusLineUpdateAt = clock.instant()
        .minusMillis(apiCoreProperties.getRunner().getUpdateLineBuildStatusAfter());
  }
  
  private void onTestVersionStart(TestVersion testVersion) {
    // set the line to 0 when a new version starts, we do this after test is sanitize and just
    // one window is there with blank url, thus it's safe to change the version. It's ok if a few
    // shots go with line 0 as the test has not really yet started, once it has started line would
    // already have changed.
    currentTestVersion.setTestVersionId(testVersion.getTestVersionId())
        .setControlAtLineInProgram(0);
    
    // put a record in build status
    BuildStatus buildStatus = new BuildStatus()
        .setBuildId(build.getBuildId())
        .setTestVersionId(testVersion.getTestVersionId())
        .setStatus(TestStatus.RUNNING)
        .setStartDate(DateTimeUtil.getCurrentUTC());
    validateSingleRowDbCommit(buildStatusProvider.save(buildStatus));
  
    printStream.println("Executing test version " + testVersion.getName());
    
    testVersionsStatus.put(testVersion.getTestVersionId(), TestStatus.RUNNING);
  }
  
  private void onTestVersionSuccess(TestVersion testVersion) {
    // we do this to make sure the version we're marking success was first marked running and
    // actually had an entry in BuildStatus
    validateTestVersionRunning(testVersion);
    
    // update build status
    updateBuildStatus(testVersion.getTestVersionId(), TestStatus.SUCCESS, null);
    
    // once a version's execution is done, push a message, don't use printStream as we need to send
    // another argument.
    sendOutput("Completed execution for test version " + testVersion.getName(), true);
  
    // Now mark this test version as completed
    testVersionsStatus.put(testVersion.getTestVersionId(), TestStatus.SUCCESS);
  }
  
  private void onTestVersionFailed(TestVersion testVersion, Throwable t) {
    // we do this to make sure the version we're marking error was first marked running and
    // actually had an entry in BuildStatus
    validateTestVersionRunning(testVersion);
  
    String exMessage = translateExToUserReadableMsg(t);
    // update build status
    updateBuildStatus(testVersion.getTestVersionId(), TestStatus.ERROR, exMessage);
  
    // once a version's execution is done, push a message, don't use printStream as we need to send
    // another argument.
    String outputMsg =
        "Exception occurred during execution of test version " + testVersion.getName();
    sendOutput(outputMsg + ":\n" + exMessage, true);
    
    // Now mark this test version as error
    testVersionsStatus.put(testVersion.getTestVersionId(), TestStatus.ERROR);
  }
  
  // end date, start date, error are null for tests that couldn't run. status could be either
  // ABORTED or STOPPED
  private void saveTestVersionsNotRun(TestStatus status) {
    testVersions.forEach(t -> {
      if (!testVersionsStatus.containsKey(t.getTestVersionId())) {
        BuildStatus buildStatus = new BuildStatus()
            .setBuildId(build.getBuildId())
            .setTestVersionId(t.getTestVersionId())
            .setStatus(status);
        validateSingleRowDbCommit(buildStatusProvider.save(buildStatus));
      }
    });
  }
  
  private void updateBuildStatus(int testVersionId, TestStatus status, @Nullable String error) {
    BuildStatus buildStatus = new BuildStatus()
        .setBuildId(build.getBuildId())
        .setTestVersionId(testVersionId)
        .setStatus(status)
        .setEndDate(DateTimeUtil.getCurrentUTC());
    if (!Strings.isNullOrEmpty(error)) {
      buildStatus.setError(error);
    }
    validateSingleRowDbCommit(buildStatusProvider.update(buildStatus));
  }
  
  private String translateExToUserReadableMsg(Throwable t) {
    StringBuilder msg = new StringBuilder();
    if (t instanceof WebDriverException) {
      // most likely an exception occurred during satinize between tests
      msg.append("A Webdriver exception occurred while cleaning up between each test. Details: ");
      msg.append(formatExClassAndMsg(t));
    } else if (t instanceof ZwlLangException) {
      if (t.getCause() == null) {
        // we can send the exception itself.
        msg.append(formatExClassAndMsg(t));
      } else if (t.getCause() instanceof RecognitionException) {
        // an exception occurred during parsing
        msg.append("An exception occurred during parsing: ");
        String parseError = "line " + storingErrorListener.getLine() + ":" +
            storingErrorListener.getCharPositionInLine() + " " + storingErrorListener.getMsg();
        msg.append(parseError);
        // no exception class like InputMismatch is written
      } else if (t.getCause() instanceof WebDriverException) {
        // when the cause is WebDriverException, it's most likely from our webdriver functions, and
        // the line and column information is the message.
        msg.append(composeWebdriverException(t.getMessage(), (WebDriverException) t.getCause()));
      }
    } else {
      msg.append("An unexpected internal exception has occurred.");
    }
    return msg.toString();
  }
  
  private String composeWebdriverException(String lineNColumn, WebDriverException wdEx) {
    StringBuilder msg = new StringBuilder();
    List<WebDriverException> exStack = new ArrayList<>();
    exStack.add(wdEx);
    while (wdEx.getCause() instanceof WebDriverException) {
      exStack.add((WebDriverException) wdEx.getCause());
    }
    int exStackSize = exStack.size();
    // from WebdriverException class, we need to strip the extra details added with the last
    // exception in cause chain because that includes server ip, internal class names
    // etc that may be irrelevant for user.
    WebDriverException lastEx = exStack.get(exStackSize - 1);
    String lastExMsg = lastEx.getMessage();
    // strip everything after a line break, which should be there per the code, but if it's not,
    // just get entire string.
    int indexOfLineBreak = lastExMsg.indexOf("\n");
    lastExMsg = lastExMsg.substring(0,
        indexOfLineBreak > -1 ? indexOfLineBreak : lastExMsg.length());
    // check whether the message still contains the extra details selenium added, this may happen
    // when the last exception did have an empty message.
    if (lastExMsg.contains("Build info:") || lastExMsg.contains("System info:")
        || lastExMsg.contains("Driver info:") || lastExMsg.contains("Element info:")
        || lastExMsg.length() == 0) {
      // make a generic message if this happens
      LOG.warn("Last webdriver exception message in cause chain is empty", wdEx);
      lastExMsg = "A Webdriver exception has occurred";
    }
    lastExMsg = formatExClassAndMsg(lastEx, lastExMsg + " at " + lineNColumn);
    
    // check whether the cause chain contain more than more than one exception
    if (exStackSize > 1) {
      msg.append("\nException stack trace:\n");
      for (WebDriverException ex : exStack.subList(0, exStackSize - 1)) {
        msg.append(formatExClassAndMsg(ex));
        msg.append("\n");
      }
    }
    msg.append(lastExMsg);
    return msg.toString();
  }
  
  private String formatExClassAndMsg(Throwable t, String message) {
    return String.format("%s: %s", t.getClass().getSimpleName(), message);
  }
  
  private String formatExClassAndMsg(Throwable t) {
    return formatExClassAndMsg(t, t.getMessage());
  }
  
  private void sanitizeBetweenTests() {
    if (buildCapability.isBuildAetKeepSingleWindow()) {
      // delete any open windows and leave just one with about:blank, delete all cookies before
      // reading new test
      List<String> winHandles = new ArrayList<>(driver.getWindowHandles());
      for (int i = 0; i < winHandles.size(); i++) {
        driver.switchTo().window(winHandles.get(i));
        if (i < winHandles.size() - 1) {
          driver.close();
        }
      }
      // maximizing and resetting url takes affect only when keep single window is true.
      if (buildCapability.isWdBrwStartMaximize()) {
        driver.manage().window().maximize();
      }
      if (buildCapability.isBuildAetUpdateUrlBlank()) {
        driver.get("about:blank"); // "about local scheme" can be given to 'get' per webdriver spec
      }
    }
    if (buildCapability.isBuildAetDeleteAllCookies()) {
      driver.manage().deleteAllCookies(); // delete all cookies
    }
    if (buildCapability.isBuildAetResetTimeouts()) {
      // rest driver timeouts to their default
      driver.manage().timeouts().pageLoadTimeout(wdProps.getDefaultTimeoutPageLoad(),
          TimeUnit.MILLISECONDS);
      driver.manage().timeouts().setScriptTimeout(wdProps.getDefaultTimeoutScript(),
          TimeUnit.MILLISECONDS);
      // reset build capability timeouts to the stored timeouts
      buildCapability.setWdTimeoutsElementAccess(storedElementAccessTimeout);
      buildCapability.setWdTimeoutsPageLoad(storedPageLoadTimeout);
      buildCapability.setWdTimeoutsScript(storedScriptTimeout);
    }
  }
  
  private void sendOutput(String message) {
    sendOutput(message, false);
  }
  
  // Runner should push a message with versionEndedMessage=true when it has fully executed a test
  // version, something like "Completed execution for test version <name>"
  private void sendOutput(String message, boolean versionEndedMessage) {
    ZwlProgramOutput zwlProgramOutput = new ZwlProgramOutput()
        .setBuildId(build.getBuildId())
        .setTestVersionId(currentTestVersion.getTestVersionId())
        .setOutput(message)
        .setCreateDate(DateTimeUtil.getCurrentUTC())
        .setEnded(versionEndedMessage);
    zwlProgramOutputProvider.saveAsync(zwlProgramOutput);
  }
  
  private void validateSingleRowDbCommit(int result) {
    if (result != 1) {
      throw new RuntimeException("Expected one row to be affected but it was " + result);
    }
  }
  
  private void validateTestVersionRunning(TestVersion testVersion) {
    // validate the version we're marking as success was actually RUNNING
    if (testVersionsStatus.get(testVersion.getTestVersionId()) != TestStatus.RUNNING) {
      throw new RuntimeException(String.format("Can't change state of testVersionId: %s because" +
              "  it's not in RUNNING status. testVersionsStatus: %s",
          testVersion.getTestVersionId(), testVersionsStatus));
    }
  }
  
  private TestVersion getTestVersionById(int testVersionId) {
    //noinspection OptionalGetWithoutIsPresent
    return testVersions.stream().filter(t -> t.getTestVersionId() == testVersionId).findFirst()
        .get();
  }
}
