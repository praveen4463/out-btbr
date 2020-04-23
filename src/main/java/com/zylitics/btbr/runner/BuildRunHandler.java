package com.zylitics.btbr.runner;

import com.google.cloud.storage.Storage;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.http.RequestBuildRun;
import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.model.TestVersion;
import com.zylitics.btbr.model.ZwlProgramOutput;
import com.zylitics.btbr.runner.provider.*;
import com.zylitics.btbr.util.CallbackOnlyPrintStream;
import com.zylitics.btbr.util.DateTimeUtil;
import com.zylitics.btbr.webdriver.logs.WebdriverLogHandler;
import com.zylitics.zwl.antlr4.StoringErrorListener;
import com.zylitics.zwl.api.ZwlApi;
import com.zylitics.zwl.api.ZwlInterpreterVisitor;
import com.zylitics.zwl.exception.ZwlLangException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BuildRunHandler {
  
  private static final Logger LOG = LoggerFactory.getLogger(BuildRunHandler.class);
  
  private final RequestBuildRun requestBuildRun;
  private final APICoreProperties apiCoreProperties;
  private final APICoreProperties.Webdriver wdProps;
  private final Storage storage;
  private final CaptureShotHandler captureShotHandler;
  private final BuildStatusProvider buildStatusProvider;
  private final BuildVMProvider buildVMProvider;
  private final ImmutableMapProvider immutableMapProvider;
  private final ShotMetadataProvider shotMetadataProvider;
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
  
  private final CurrentTestVersion currentTestVersion = new CurrentTestVersion();
  // it's fine to have a single storingErrorListener for all test versions, even if more than one
  // test version fails, fields gets reset with consecutive failure, this happens because we run
  // tests in sequence never parallel.
  private final StoringErrorListener storingErrorListener = new StoringErrorListener();
  private final Map<Integer, TestVersionRunDetail> testVersionRunState = new HashMap<>();
  
  private final Clock clock;
  
  private Instant lastLogCheckAt;
  
  public BuildRunHandler(RequestBuildRun requestBuildRun,
                         APICoreProperties apiCoreProperties,
                         Storage storage,
                         CaptureShotHandler.Factory captureShotHandlerFactory,
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
    this.buildStatusProvider = buildStatusProvider;
    this.buildVMProvider = buildVMProvider;
    this.immutableMapProvider = immutableMapProvider;
    this.shotMetadataProvider = shotMetadataProvider;
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
    
    storedPageLoadTimeout = buildCapability.getWdTimeoutsPageLoad();
    storedScriptTimeout = buildCapability.getWdTimeoutsScript();
    storedElementAccessTimeout = buildCapability.getWdTimeoutsElementAccess();
    
    clock = Clock.systemUTC();
  }
  
  void handle() {
    try {
      run();
    } finally {
      // delete VM here
    }
  }
  
  private void zwlProgramLineChanged(int currentLine) {
    // check if we're can't move forward
    if (Thread.interrupted()) {
      // a stop request must have arrived, we should halt everything.
      attemptGracefulStop();
      return;
    }
    
    // set line to currentTestVersion so that shots process can take it.
    currentTestVersion.setControlAtLineInProgram(currentLine);
    
    // for webdriver logs, check if sufficient time has been passed since we last captured logs, if
    // so capture them again
    if (ChronoUnit.MILLIS.between(lastLogCheckAt, clock.instant()) >=
        wdProps.getWaitBetweenLogsCapture()) {
      webdriverLogHandler.capture();
      // reset to current instant
      lastLogCheckAt = clock.instant();
    }
  }
  
  private void attemptGracefulStop() {}
  
  private void run() {
    // initialize things
    zwlProgramOutputProvider.setBuildCapability(buildCapability);
  
    // get Zwl interpreter visitor
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    ZwlInterpreterVisitor zwlInterpreterVisitor = new InterpreterVisitorProvider(
        wdProps,
        storage,
        this::zwlProgramLineChanged,
        build,
        driver,
        printStream,
        immutableMapProvider.getMapFromTable("zwl_preferences").get(),
        buildDir,
        immutableMapProvider.getMapFromTable("zwl_globals").get())
        .get();
    
    // let's start the build
    // maximize the driver window if user didn't say otherwise
    if (buildCapability.isWdBrwStartMaximize()) {
      driver.manage().window().maximize();
    }
    boolean firstTest = true;
    for (TestVersion testVersion : testVersions) {
      onTestVersionStart(testVersion);
      
      if (!firstTest) {
        // sanitize only after the first version is completed
        sanitizeBetweenTests();
      } else {
        // run only for the first time
        onBuildStart();
      }
      // firstTest's work done
      firstTest = false;
      
      String code = testVersion.getZwlProgram().getCode();
      printStream.println("Reading and executing test version " + testVersion.getName());
      ZwlApi zwlApi = new ZwlApi(code, Collections.singletonList(storingErrorListener));
      try {
        zwlApi.interpret(zwlInterpreterVisitor);
      } catch (Throwable t) {
        // try to run other versions only when the exception is a ZwlLangException, cause it's very
        // unlikely any other test will pass when there is a problem in our application that caused
        // an unknown exception.
        if (t instanceof ZwlLangException && !buildCapability.isBuildAbortOnFailure()) {
          onTestVersionFailed(testVersion, (ZwlLangException) t);
          continue;
        }
        // Depending on the throwable type, compose error for user, push all versions in build
        // status, mark ERROR to this one and ABORTED to others. Halt the entire build, and let the
        // currently captured stuff push, delete vm.
        
        // If any exception other than ZwlLangException has occurred, it's very unlikely that
        // any other test version will succeed cause it usually means we've a bug somewhere in the
        // application, thus we will send a meaningful exception to user only
        
        return; // return as we can't try more versions
      }
      onTestVersionComplete(testVersion);
    }
    // once build is completed, do a number of tasks
    onBuildComplete();
  }
  
  // Note: The order of build complete actions are very precise
  private void onBuildComplete() {
    // first let the shot stop
    captureShotHandler.stopShot();
    
    
  }
  
  // do things that require only one time execution/invocation on build start
  private void onBuildStart() {
    // begin capturing shot
    captureShotHandler.startShot();
    // assign current instant to log capture instant, so that log capture waits for sometime
    // from now before trying capturing.
    lastLogCheckAt = clock.instant();
  }
  
  private void onBuildFailed() {
  
  }
  
  private void onTestVersionStart(TestVersion testVersion) {
    currentTestVersion.setTestVersionId(testVersion.getTestVersionId())
        .setControlAtLineInProgram(0); // set the line to 0 when a new version starts
  }
  
  private void onTestVersionComplete(TestVersion testVersion) {
    // once a version's execution is done, push a message
    sendOutput("Completed execution for test version " + testVersion.getName(), true);
    // put an entry in testVersionRunState
    testVersionRunState.put(testVersion.getTestVersionId(), new TestVersionRunDetail().s)
  }
  
  private void onTestVersionFailed(TestVersion testVersion, ZwlLangException zwlLangException) {
    // get exact error for user, update build status, mark this version as 'ERROR' and move to
    // next version. Note that when it is one of RecognitionException, we will've to build the
    // error using storingErrorListeners stored fields.
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
  
  // on build failure and stop, we need to know what tests have been done. what were in process
  // and what threw error and based on that push the detail in build status.
  private static class TestVersionRunDetail {
    
    private int testVersionId;
    private TestStatus status;
    private Throwable exception; // present when the status is ERROR
  
    public int getTestVersionId() {
      return testVersionId;
    }
  
    public TestVersionRunDetail setTestVersionId(int testVersionId) {
      this.testVersionId = testVersionId;
      return this;
    }
  
    public TestStatus getStatus() {
      return status;
    }
  
    public TestVersionRunDetail setStatus(TestStatus status) {
      this.status = status;
      return this;
    }
  
    public Throwable getException() {
      return exception;
    }
  
    public TestVersionRunDetail setException(Throwable exception) {
      this.exception = exception;
      return this;
    }
  }
}
