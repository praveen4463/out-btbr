package com.zylitics.btbr.runner;

import com.google.cloud.storage.Storage;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.*;
import com.zylitics.btbr.runner.provider.*;
import com.zylitics.btbr.util.DateTimeUtil;
import com.zylitics.btbr.webdriver.logs.WebdriverLogHandler;
import com.zylitics.zwl.api.*;
import com.zylitics.zwl.exception.ZwlLangException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.SessionStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness= Strictness.STRICT_STUBS)
public class BuildRunHandlerTest {
  
  @DisplayName("Validate on line change, line updates push to build status, ")
  @Test
  @Tag("linechange")
  void validateLineChangeListener() {
    int updateLineMillis = 20;
    int changeLineMillis = updateLineMillis + updateLineMillis / 2;
    int captureLogsMillis = changeLineMillis + changeLineMillis / 2;
    int buildId = 1;
    int fileId = 1;
    int testId1 = 1;
    int testId2 = 2;
    int testVersionId1 = 1;
    int testVersionId2 = 2;
    
    BuildStatusProvider buildStatusProvider = getBuildStatusProvider();
    when(buildStatusProvider.updateLine(any(BuildStatusUpdateLine.class))).thenReturn(1);
    
    ZwlApi zwlApi = mock(ZwlApi.class);
    ZwlInterpreter zwlInterpreter = mock(ZwlInterpreter.class);
    doAnswer(i -> {
      InterpreterLineChangeListener listener = i.getArgument(0);
      listener.onLineChange(1);
      Thread.sleep(changeLineMillis);
      listener.onLineChange(2);
      Thread.sleep(changeLineMillis);
      listener.onLineChange(3);
      return null;
    }).doAnswer(i -> {
      InterpreterLineChangeListener listener = i.getArgument(0);
      listener.onLineChange(1);
      Thread.sleep(changeLineMillis);
      listener.onLineChange(2);
      Thread.sleep(changeLineMillis);
      listener.onLineChange(3);
      return null;
    }).when(zwlInterpreter).setLineChangeListener(any(InterpreterLineChangeListener.class));
    
    doAnswer(i -> {
      ZwlInterpreterVisitor visitor = i.getArgument(1);
      visitor.visit(zwlInterpreter);
      return null;
    }).when(zwlApi).interpret(any(ZwlWdTestProperties.class), any(ZwlInterpreterVisitor.class));
    
    TestVersion testVersion1 = new TestVersion().setTestVersionId(testVersionId1).setName("v1")
        .setCode("a = 1")
        .setTest(new com.zylitics.btbr.model.Test().setTestId(testId1).setName("t1"))
        .setFile(new File().setFileId(fileId).setName("UntilTests"));
    TestVersion testVersion2 = new TestVersion().setTestVersionId(testVersionId2).setName("v1")
        .setCode("b = 1")
        .setTest(new com.zylitics.btbr.model.Test().setTestId(testId2).setName("t2"))
        .setFile(new File().setFileId(fileId).setName("UntilTests"));
    
    RemoteWebDriver driver = getRemoteWebDriver();
    addTimeoutMock(driver);
    addDefaultWinHandle(driver);
    
    WebdriverLogHandler webdriverLogHandler = getWebdriverLogHandler();
    
    new Builder()
        .withBuildId(buildId)
        .withUpdateLineMillis(updateLineMillis)
        .withCaptureLogsMillis(captureLogsMillis)
        .withZwlApi(zwlApi)
        .withTestVersions(ImmutableList.of(testVersion1, testVersion2))
        .withBuildStatusProvider(buildStatusProvider)
        .withDriver(driver)
        .withWebdriverLogHandler(webdriverLogHandler)
        .build().handle();
  
    BuildStatusUpdateLine line11 = new BuildStatusUpdateLine(buildId, testVersionId1, 1);
    BuildStatusUpdateLine line12 = new BuildStatusUpdateLine(buildId, testVersionId1, 2);
    BuildStatusUpdateLine line13 = new BuildStatusUpdateLine(buildId, testVersionId1, 3);
    BuildStatusUpdateLine line21 = new BuildStatusUpdateLine(buildId, testVersionId2, 1);
    BuildStatusUpdateLine line22 = new BuildStatusUpdateLine(buildId, testVersionId2, 2);
    BuildStatusUpdateLine line23 = new BuildStatusUpdateLine(buildId, testVersionId2, 3);
  
    InOrder inOrder = inOrder(buildStatusProvider);
    inOrder.verify(buildStatusProvider).updateLine(line11);
    inOrder.verify(buildStatusProvider).updateLine(line12);
    inOrder.verify(buildStatusProvider).updateLine(line13);
    inOrder.verify(buildStatusProvider).updateLine(line21);
    inOrder.verify(buildStatusProvider).updateLine(line22);
    inOrder.verify(buildStatusProvider).updateLine(line23);
    
    // logs get captured one last time on build finish
    verify(webdriverLogHandler, times(3)).capture();
  }
  
  @DisplayName("Validate everything during sanitize step")
  @Test
  @Tag("sanitize")
  void validateSanitize() {
    TestVersion testVersion1 = new TestVersion().setTestVersionId(1).setName("v1")
        .setCode("a = 1")
        .setTest(new com.zylitics.btbr.model.Test().setTestId(1).setName("t1"))
        .setFile(new File().setFileId(1).setName("UT"));
    TestVersion testVersion2 = new TestVersion().setTestVersionId(2).setName("v1")
        .setCode("b = 1")
        .setTest(new com.zylitics.btbr.model.Test().setTestId(2).setName("t2"))
        .setFile(new File().setFileId(1).setName("UT"));
    TestVersion testVersion3 = new TestVersion().setTestVersionId(3).setName("v1")
        .setCode("c = 1")
        .setTest(new com.zylitics.btbr.model.Test().setTestId(3).setName("t3"))
        .setFile(new File().setFileId(1).setName("UT"));
    List<TestVersion> versions = ImmutableList.of(testVersion1, testVersion2, testVersion3);
    
    //test with sanitize on
    RemoteWebDriver driver = getRemoteWebDriver();
    addTimeoutMock(driver);
    addDefaultWinHandle(driver);
    addTargetLocatorMock(driver);
    
    new Builder()
        .withTestVersions(versions)
        .withDriver(driver)
        .build().handle();
    
    verify(driver, times(2)).getWindowHandles();
    verify(driver.switchTo(), never()).window(anyString());
    verify(driver, never()).close();
    verify(driver.manage().window(), times(3)).maximize();
    verify(driver, times(2)).get("about:blank");
    verify(driver.manage(), times(3)).deleteAllCookies();
    verify(driver.manage().timeouts(), times(2)).pageLoadTimeout(anyLong(),
        eq(TimeUnit.MILLISECONDS));
    verify(driver.manage().timeouts(), times(2)).setScriptTimeout(anyLong(),
        eq(TimeUnit.MILLISECONDS));
  
    // test with sanitize off
    driver = getRemoteWebDriver();
    addTimeoutMock(driver);
    addTargetLocatorMock(driver);
  
    Build build = getBuild(1, BuildSourceType.NOT_IDE);
    build.setAetKeepSingleWindow(false);
    build.setAetDeleteAllCookies(false);
    build.setAetResetTimeouts(false);
    
    new Builder()
        .withTestVersions(versions)
        .withBuild(build)
        .withDriver(driver)
        .build().handle();
    
    verify(driver, never()).getWindowHandles();
    verify(driver.switchTo(), never()).window(anyString());
    verify(driver, never()).close();
    verify(driver.manage().window(), times(1)).maximize();
    verify(driver, never()).get("about:blank");
    // we delete all cookies once after all tests done before close.
    verify(driver.manage(), times(1)).deleteAllCookies();
    verify(driver.manage().timeouts(), never()).pageLoadTimeout(anyLong(),
        eq(TimeUnit.MILLISECONDS));
    verify(driver.manage().timeouts(), never()).setScriptTimeout(anyLong(),
        eq(TimeUnit.MILLISECONDS));
  
    // test with more than one window opened during sanitize, note we're not using 3 versions but
    // the default 2 so sanitize occurs just once
    driver = getRemoteWebDriver();
    addTimeoutMock(driver);
    addTargetLocatorMock(driver);
    when(driver.getWindowHandles()).thenReturn(ImmutableSet.of("a-3", "a-2", "a-1"));
  
    new Builder()
        .withDriver(driver)
        .build().handle();
  
    verify(driver.switchTo()).window("a-3");
    verify(driver.switchTo()).window("a-2");
    verify(driver.switchTo()).window("a-1");
    verify(driver, times(2)).close();
  }
  
  @DisplayName("Validate entire flow including preferred order when there is no exception")
  @Test
  void validateNormalFlow() {
    int buildId = 1;
    int testVersionId1 = 1;
    int testVersionId2 = 2;
    Clock clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));
    OffsetDateTime currentDT = DateTimeUtil.getCurrent(clock);
    LocalDateTime currentDtLocal = DateTimeUtil.getCurrentLocal(clock);
    TestVersion testVersion1 = new TestVersion().setTestVersionId(testVersionId1).setName("v1")
        .setCode("a = 1")
        .setTest(new com.zylitics.btbr.model.Test().setTestId(1).setName("t1"))
        .setFile(new File().setFileId(1).setName("UT"));
    TestVersion testVersion2 = new TestVersion().setTestVersionId(testVersionId2).setName("v1")
        .setCode("b = 1")
        .setTest(new com.zylitics.btbr.model.Test().setTestId(2).setName("t2"))
        .setFile(new File().setFileId(1).setName("UT"));
    List<TestVersion> versions = ImmutableList.of(testVersion1, testVersion2);
    Build build = getBuild(buildId, BuildSourceType.IDE);
    RemoteWebDriver driver = getWebStorageEnabledWebDriver();
    addTimeoutMock(driver);
    addDefaultWinHandle(driver);
    WebDriver.Options options = driver.manage();
    WebStorage storage = (WebStorage) driver;
    LocalStorage localStorage = storage.getLocalStorage();
    SessionStorage sessionStorage = storage.getSessionStorage();
    
    BuildStatusProvider buildStatusProvider = getBuildStatusProvider();
    BuildProvider buildProvider = getBuildProvider(buildId);
    BuildRequestProvider buildRequestProvider = getBuildRequestProvider(build.getBuildRequestId());
    QuotaProvider quotaProvider = getQuotaProvider(build);
    BuildOutputProvider buildOutputProvider = getBuildOutputProvider();
    CaptureShotHandler captureShotHandler = mock(CaptureShotHandler.class);
    WebdriverLogHandler webdriverLogHandler = getWebdriverLogHandler();
    LocalAssetsToCloudHandler localAssetsToCloudHandler = getLocalAssetsToCloudHandler();
    VMUpdateHandler vmUpdateHandler = getVMDeleteHandler();
    Map<Integer, BuildRunStatus> buildRunStatus = new HashMap<>();
    buildRunStatus.put(buildId, BuildRunStatus.RUNNING);
    new Builder()
        .withBuildStatusProvider(buildStatusProvider)
        .withBuildProvider(buildProvider)
        .withBuildRequestProvider(buildRequestProvider)
        .withBuild(build)
        .withQuotaProvider(quotaProvider)
        .withBuildOutputProvider(buildOutputProvider)
        .withTestVersions(versions)
        .withClock(clock)
        .withCaptureShotHandlerFactory(getCaptureShotHandlerFactory(captureShotHandler))
        .withDriver(driver)
        .withWebdriverLogHandler(webdriverLogHandler)
        .withLocalAssetsToCloudHandler(localAssetsToCloudHandler)
        .withVmDeleteHandler(vmUpdateHandler)
        .withBuildRunStatus(buildRunStatus)
        .build().handle();
    assertEquals(BuildRunStatus.COMPLETED, buildRunStatus.get(buildId));
    
    InOrder inOrder = inOrder(buildStatusProvider, buildProvider,
        buildRequestProvider, quotaProvider, buildOutputProvider,
        captureShotHandler, driver, options, localStorage, sessionStorage, webdriverLogHandler,
        localAssetsToCloudHandler, vmUpdateHandler);
    
    inOrder.verify(buildStatusProvider).saveOnStart(new BuildStatusSaveOnStart(buildId,
        testVersionId1, TestStatus.RUNNING, currentDT, build.getUserId()));
    inOrder.verify(buildOutputProvider).newBuildOutput(argThat(matchBuildOutput(
        new BuildOutput().setBuildId(buildId).setTestVersionId(testVersionId1)
            .setOutput("Executing").setEnded(false).setCreateDate(currentDT))));
    inOrder.verify(captureShotHandler).startShot();
    inOrder.verify(buildOutputProvider).newBuildOutput(argThat(matchBuildOutput(
        new BuildOutput().setBuildId(buildId).setTestVersionId(testVersionId1)
            .setOutput("Completed").setEnded(true).setCreateDate(currentDT))));
    inOrder.verify(buildStatusProvider).updateOnEnd(new BuildStatusUpdateOnEnd(buildId,
        testVersionId1, TestStatus.SUCCESS, currentDT));
    // first version done, second started
    inOrder.verify(driver).getWindowHandles();
    inOrder.verify(buildStatusProvider).saveOnStart(new BuildStatusSaveOnStart(buildId,
        testVersionId2, TestStatus.RUNNING, currentDT, build.getUserId()));
    inOrder.verify(buildOutputProvider).newBuildOutput(argThat(matchBuildOutput(
        new BuildOutput().setBuildId(buildId).setTestVersionId(testVersionId2)
            .setOutput("Executing").setEnded(false).setCreateDate(currentDT))));
    inOrder.verify(buildOutputProvider).newBuildOutput(argThat(matchBuildOutput(
        new BuildOutput().setBuildId(buildId).setTestVersionId(testVersionId2)
            .setOutput("Completed").setEnded(true).setCreateDate(currentDT))));
    inOrder.verify(buildStatusProvider).updateOnEnd(new BuildStatusUpdateOnEnd(buildId,
        testVersionId2, TestStatus.SUCCESS, currentDT));
    // second version done, build complete steps will follow
    inOrder.verify(buildProvider).updateOnComplete(new BuildUpdateOnComplete(buildId, currentDT,
        TestStatus.SUCCESS, null));
    // as we've given source IDE
    inOrder.verify(captureShotHandler).stopShot();
    inOrder.verify(captureShotHandler).blockUntilFinish();
    inOrder.verify(webdriverLogHandler).capture();
    inOrder.verify(options).deleteAllCookies();
    inOrder.verify(localStorage).clear();
    inOrder.verify(sessionStorage).clear();
    inOrder.verify(driver).quit();
    inOrder.verify(localAssetsToCloudHandler).store();
    inOrder.verify(buildProvider).updateOnAllTasksDone(buildId, currentDT);
    inOrder.verify(quotaProvider).updateConsumed(build, currentDtLocal);
    inOrder.verify(buildRequestProvider).markBuildRequestCompleted(build.getBuildRequestId());
    inOrder.verify(vmUpdateHandler).update(argThat(b -> b.getBuildId() == buildId));
  }
  
  @Test
  void validateOnInterpretZwlLangException() {
    int buildId = 1;
    int testVersionId = 1;
    String error = "invalid identifier";
    BuildStatusProvider buildStatusProvider = getBuildStatusProvider();
    Clock clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));
    BuildProvider buildProvider = getBuildProvider(buildId);
    BuildOutputProvider buildOutputProvider = getBuildOutputProvider();
    OffsetDateTime currentDT = DateTimeUtil.getCurrent(clock);
    ZwlLangException zwEx = new ZwlLangException(null, null, error);
    // when single version is run
    TestVersion testVersion1 = new TestVersion().setTestVersionId(testVersionId).setName("v1")
        .setCode("a = 1")
        .setTest(new com.zylitics.btbr.model.Test().setTestId(1).setName("t1"))
        .setFile(new File().setFileId(1).setName("UT"));
    ZwlApi zwlApi = mock(ZwlApi.class);
    doAnswer(i -> {
      throw zwEx;
    }).when(zwlApi).interpret(any(ZwlWdTestProperties.class), any(ZwlInterpreterVisitor.class));
    new Builder()
        .withBuildId(buildId)
        .withBuildStatusProvider(buildStatusProvider)
        .withClock(clock)
        .withBuildProvider(buildProvider)
        .withBuildOutputProvider(buildOutputProvider)
        .withTestVersions(ImmutableList.of(testVersion1))
        .withZwlApi(zwlApi).build().handle();
    
    InOrder inOrder = inOrder(buildStatusProvider, buildProvider, buildOutputProvider);
    inOrder.verify(buildOutputProvider).newBuildOutput(argThat(matchBuildOutput(
        new BuildOutput().setBuildId(buildId).setTestVersionId(testVersionId)
            .setOutput("Exception").setEnded(true).setCreateDate(currentDT))));
    inOrder.verify(buildStatusProvider).updateOnEnd(argThat(matchBuildStatusUpdateOnEnd(
        new BuildStatusUpdateOnEnd(buildId, testVersionId, TestStatus.ERROR, currentDT
            , new ExceptionTranslationProvider().get(zwEx), "0:0",
            "0:1", null))));
    inOrder.verify(buildProvider).updateOnComplete(argThat(matchBuildUpdateOnComplete(
        new BuildUpdateOnComplete(buildId, currentDT, TestStatus.ERROR, "An exception"))));
    // when multiple version is run
  }
  
  // TODO: Write more tests later on, for now this much including the E2E should be enough
  
  // the 'output' field of BuildOutput is what causes this matcher to be created because I
  // don't want to match on exact term but partial, I didn't want to create an 'equals' in
  // BuildOutput that matches on partial 'output' string as it's a test requirement only.
  private ArgumentMatcher<BuildOutput> matchBuildOutput(BuildOutput expected) {
    return z -> z.getBuildId() == expected.getBuildId()
        && z.getTestVersionId() == expected.getTestVersionId()
        && z.getOutput().startsWith(expected.getOutput())
        && z.isEnded() == expected.isEnded()
        && z.getCreateDate().equals(expected.getCreateDate());
  }
  
  private ArgumentMatcher<BuildStatusUpdateOnEnd> matchBuildStatusUpdateOnEnd(
      BuildStatusUpdateOnEnd expected) {
    return z -> z.getBuildId() == expected.getBuildId()
        && z.getTestVersionId() == expected.getTestVersionId()
        && z.getStatus() == expected.getStatus()
        && z.getError().startsWith(expected.getError())
        && z.getEndDate().equals(expected.getEndDate());
  }
  
  private ArgumentMatcher<BuildUpdateOnComplete> matchBuildUpdateOnComplete(
      BuildUpdateOnComplete expected) {
    return z -> z.getBuildId() == expected.getBuildId()
        && z.getFinalStatus() == expected.getFinalStatus()
        && z.getError().startsWith(expected.getError())
        && z.getEndDate().equals(expected.getEndDate());
  }
  
  private APICoreProperties getAPICoreProperties(int updateLineMillis, int captureLogsMillis) {
    APICoreProperties apiCoreProperties = new APICoreProperties();
    APICoreProperties.Runner runner = apiCoreProperties.getRunner();
    runner.setUpdateLineBuildStatusAfter(updateLineMillis);
    APICoreProperties.Webdriver webdriver = apiCoreProperties.getWebdriver();
    webdriver.setWaitBetweenLogsCapture(captureLogsMillis);
    webdriver.setDefaultTimeoutElementAccess(100);
    webdriver.setDefaultTimeoutPageLoad(100);
    webdriver.setDefaultTimeoutScript(100);
    return apiCoreProperties;
  }
  
  private Storage getStorage() {
    return mock(Storage.class);
  }
  
  private BuildProvider getBuildProvider(int buildId) {
    BuildProvider buildProvider = mock(BuildProvider.class);
    when(buildProvider.updateOnStart(eq(buildId), any(OffsetDateTime.class))).thenReturn(1);
    when(buildProvider.updateOnComplete(any(BuildUpdateOnComplete.class))).thenReturn(1);
    when(buildProvider.updateOnAllTasksDone(eq(buildId), any(OffsetDateTime.class))).thenReturn(1);
    return buildProvider;
  }
  
  private BuildRequestProvider getBuildRequestProvider(long buildRequestId) {
    BuildRequestProvider buildRequestProvider = mock(BuildRequestProvider.class);
    when(buildRequestProvider.markBuildRequestCompleted(buildRequestId)).thenReturn(1);
    return buildRequestProvider;
  }
  
  private BuildStatusProvider getBuildStatusProvider() {
    BuildStatusProvider buildStatusProvider = mock(BuildStatusProvider.class);
    when(buildStatusProvider.saveOnStart(any(BuildStatusSaveOnStart.class))).thenReturn(1);
    when(buildStatusProvider.updateOnEnd(any(BuildStatusUpdateOnEnd.class))).thenReturn(1);
    return buildStatusProvider;
  }
  
  private QuotaProvider getQuotaProvider(Build build) {
    QuotaProvider quotaProvider = mock(QuotaProvider.class);
    when(quotaProvider.updateConsumed(eq(build), any(LocalDateTime.class))).thenReturn(1);
    return quotaProvider;
  }
  
  private BuildOutputProvider getBuildOutputProvider() {
    BuildOutputProvider buildOutputProvider = mock(BuildOutputProvider.class);
    when(buildOutputProvider.newBuildOutput(any(BuildOutput.class))).thenReturn(1);
    return buildOutputProvider;
  }
  
  private TestVersionProvider getTestVersionProvider() {
    return mock(TestVersionProvider.class);
  }
  
  private ImmutableMapProvider getImmutableMapProvider() {
    ImmutableMapProvider immutableMapProvider = mock(ImmutableMapProvider.class);
    when(immutableMapProvider.getMapFromTableByBuild(anyInt(), anyString()))
        .thenReturn(Optional.empty());
    return immutableMapProvider;
  }
  
  private ShotMetadataProvider getShotMetadataProvider() {
    return mock(ShotMetadataProvider.class);
  }
  
  private Build getBuild(int buildId, BuildSourceType buildSourceType) {
    Build build = new Build();
    build.setUserId(1);
    build.setBuildId(buildId);
    build.setBuildVMId(1);
    build.setCreateDateUTC(DateTimeUtil.getCurrentLocal(Clock.systemUTC()));
    build.setAetDeleteAllCookies(true);
    build.setAetResetTimeouts(true);
    build.setAetKeepSingleWindow(true);
    build.setAetUpdateUrlBlank(true);
    build.setBuildRequestId(1);
    build.setSourceType(buildSourceType);
    build.setShotBucketSessionStorage("shot-bucket");
  
    BuildCapability buildCapability = new BuildCapability();
    buildCapability.setWdTimeoutsScript(-1);
    buildCapability.setWdTimeoutsPageLoad(-1);
    buildCapability.setWdTimeoutsElementAccess(-1);
    buildCapability.setWdBrwStartMaximize(true);
    build.setBuildCapability(buildCapability);
    return build;
  }
  
  private List<TestVersion> getVersions() {
    List<TestVersion> versions = new ArrayList<>();
    TestVersion testVersion1 = new TestVersion().setTestVersionId(1).setName("v1")
        .setCode("a = 1")
        .setTest(new com.zylitics.btbr.model.Test().setTestId(1).setName("t1"))
        .setFile(new File().setFileId(1).setName("UT"));
    versions.add(testVersion1);
  
    TestVersion testVersion2 = new TestVersion().setTestVersionId(2).setName("v1")
        .setCode("b = 1")
        .setTest(new com.zylitics.btbr.model.Test().setTestId(2).setName("t2"))
        .setFile(new File().setFileId(1).setName("UT"));
    versions.add(testVersion2);
    return versions;
  }
  
  private CaptureShotHandler.Factory getCaptureShotHandlerFactory(
      CaptureShotHandler captureShotHandler) {
    CaptureShotHandler.Factory factory = mock(CaptureShotHandler.Factory.class);
    when(factory.create(any(APICoreProperties.Shot.class), any(ShotMetadataProvider.class),
        any(Storage.class), any(Build.class), anyString(),
        any(CurrentTestVersion.class))).thenReturn(captureShotHandler);
    return factory;
  }
  
  private VMUpdateHandler getVMDeleteHandler() {
    return mock(VMUpdateHandler.class);
  }
  
  private BuildCompletionEmailHandler getBuildCompletionEmailHandler() {
    return mock(BuildCompletionEmailHandler.class);
  }
  
  private WebdriverLogHandler getWebdriverLogHandler() {
    return mock(WebdriverLogHandler.class);
  }
  
  private LocalAssetsToCloudHandler getLocalAssetsToCloudHandler() {
    return mock(LocalAssetsToCloudHandler.class);
  }
  
  private RemoteWebDriver getRemoteWebDriver() {
    RemoteWebDriver driver = mock(RemoteWebDriver.class);
    WebDriver.Options options = mock(WebDriver.Options.class);
    WebDriver.Window window = mock(WebDriver.Window.class);
    
    when(driver.manage()).thenReturn(options);
    when(options.window()).thenReturn(window);
    when(driver.getSessionId()).thenReturn(new SessionId("some-session"));
    return driver;
  }
  
  private RemoteWebDriver getWebStorageEnabledWebDriver() {
    ChromeDriver driver = mock(ChromeDriver.class);
    
    LocalStorage localStorage = mock(LocalStorage.class);
    SessionStorage sessionStorage = mock(SessionStorage.class);
    
    when(driver.getLocalStorage()).thenReturn(localStorage);
    when(driver.getSessionStorage()).thenReturn(sessionStorage);
    
    WebDriver.Options options = mock(WebDriver.Options.class);
    WebDriver.Window window = mock(WebDriver.Window.class);
    
    when(driver.manage()).thenReturn(options);
    when(options.window()).thenReturn(window);
    when(driver.getSessionId()).thenReturn(new SessionId("some-session"));
    return driver;
  }
  
  private void addTimeoutMock(RemoteWebDriver mockDriver) {
    WebDriver.Timeouts timeouts = mock(WebDriver.Timeouts.class);
    when(mockDriver.manage().timeouts()).thenReturn(timeouts);
  }
  
  private void addTargetLocatorMock(RemoteWebDriver mockDriver) {
    WebDriver.TargetLocator targetLocator = mock(WebDriver.TargetLocator.class);
    when(mockDriver.switchTo()).thenReturn(targetLocator);
  }
  
  private void addDefaultWinHandle(RemoteWebDriver mockDriver) {
    when(mockDriver.getWindowHandles()).thenReturn(ImmutableSet.of("a-3"));
  }
  
  private Path getBuildDir() {
    return Paths.get("/user/build-1");
  }
  
  private ZwlApiSupplier getZwlApiSupplier(ZwlApi zwlApi) {
    ZwlApiSupplier supplier = mock(ZwlApiSupplier.class);
    when(supplier.get(anyString(), anyList())).thenReturn(zwlApi);
    return supplier;
  }
  
  private class Builder {
    
    private int buildId = -1;
    private int updateLineMillis = -1;
    private int captureLogsMillis = -1;
    private ZwlApi zwlApi = null;
    private BuildProvider buildProvider = null;
    private BuildRequestProvider buildRequestProvider = null;
    private BuildStatusProvider buildStatusProvider = null;
    private QuotaProvider quotaProvider = null;
    private BuildOutputProvider buildOutputProvider = null;
    private TestVersionProvider testVersionProvider = null;
    private Build build = null;
    private RemoteWebDriver driver = null;
    private List<TestVersion> testVersions = null;
    private Clock clock = null;
    private CaptureShotHandler.Factory captureShotHandlerFactory = null;
    private WebdriverLogHandler webdriverLogHandler = null;
    private LocalAssetsToCloudHandler localAssetsToCloudHandler = null;
    private VMUpdateHandler vmUpdateHandler = null;
    private BuildCompletionEmailHandler buildCompletionEmailHandler = null;
    private Map<Integer, BuildRunStatus> buildRunStatus = null;
  
    Builder withBuildId(int buildId) {
      this.buildId = buildId;
      return this;
    }
  
    Builder withUpdateLineMillis(int updateLineMillis) {
      this.updateLineMillis = updateLineMillis;
      return this;
    }
  
    Builder withCaptureLogsMillis(int captureLogsMillis) {
      this.captureLogsMillis = captureLogsMillis;
      return this;
    }
  
    Builder withZwlApi(ZwlApi zwlApi) {
      this.zwlApi = zwlApi;
      return this;
    }
  
    Builder withBuildProvider(BuildProvider buildProvider) {
      this.buildProvider = buildProvider;
      return this;
    }
    
    Builder withBuildRequestProvider(BuildRequestProvider buildRequestProvider) {
      this.buildRequestProvider = buildRequestProvider;
      return this;
    }
  
    Builder withBuildStatusProvider(BuildStatusProvider buildStatusProvider) {
      this.buildStatusProvider = buildStatusProvider;
      return this;
    }
  
    Builder withQuotaProvider(QuotaProvider quotaProvider) {
      this.quotaProvider = quotaProvider;
      return this;
    }
    
    Builder withBuildOutputProvider(BuildOutputProvider buildOutputProvider) {
      this.buildOutputProvider = buildOutputProvider;
      return this;
    }
  
    Builder withBuild(Build build) {
      this.build = build;
      return this;
    }
  
    Builder withDriver(RemoteWebDriver driver) {
      this.driver = driver;
      return this;
    }
  
    Builder withTestVersions(List<TestVersion> testVersions) {
      this.testVersions = testVersions;
      return this;
    }
    
    Builder withClock(Clock clock) {
      this.clock = clock;
      return this;
    }
  
    public Builder withCaptureShotHandlerFactory(
        CaptureShotHandler.Factory captureShotHandlerFactory) {
      this.captureShotHandlerFactory = captureShotHandlerFactory;
      return this;
    }
  
    public Builder withWebdriverLogHandler(WebdriverLogHandler webdriverLogHandler) {
      this.webdriverLogHandler = webdriverLogHandler;
      return this;
    }
  
    public Builder withLocalAssetsToCloudHandler(
        LocalAssetsToCloudHandler localAssetsToCloudHandler) {
      this.localAssetsToCloudHandler = localAssetsToCloudHandler;
      return this;
    }
  
    public Builder withVmDeleteHandler(VMUpdateHandler vmUpdateHandler) {
      this.vmUpdateHandler = vmUpdateHandler;
      return this;
    }
  
    public Builder withBuildRunStatus(Map<Integer, BuildRunStatus> buildRunStatus) {
      this.buildRunStatus = buildRunStatus;
      return this;
    }
  
    BuildRunHandler build() {
      if (buildId < 0) {
        buildId = 1;
      }
      if (updateLineMillis < 0) {
        updateLineMillis = 100;
      }
      if (captureLogsMillis < 0) {
        captureLogsMillis = 100;
      }
      if (zwlApi == null) {
        zwlApi = mock(ZwlApi.class);
      }
      if (buildProvider == null) {
        buildProvider = getBuildProvider(buildId);
      }
      if (build == null) {
        build = getBuild(buildId, BuildSourceType.NOT_IDE);
      }
      if (buildRequestProvider == null) {
        buildRequestProvider = getBuildRequestProvider(build.getBuildRequestId());
      }
      if (buildStatusProvider == null) {
        buildStatusProvider = getBuildStatusProvider();
      }
      if (quotaProvider == null) {
        quotaProvider = getQuotaProvider(build);
      }
      if (buildOutputProvider == null) {
        buildOutputProvider = getBuildOutputProvider();
      }
      if (testVersionProvider == null) {
        testVersionProvider = getTestVersionProvider();
      }
      if (driver == null) {
        driver = getRemoteWebDriver();
      }
      if (testVersions == null) {
        testVersions = getVersions();
      }
      if (clock == null) {
        clock = Clock.systemUTC();
      }
      if (captureShotHandlerFactory == null) {
        captureShotHandlerFactory = getCaptureShotHandlerFactory(mock(CaptureShotHandler.class));
      }
      if (webdriverLogHandler == null) {
        webdriverLogHandler = getWebdriverLogHandler();
      }
      if (localAssetsToCloudHandler == null) {
        localAssetsToCloudHandler = getLocalAssetsToCloudHandler();
      }
      if (vmUpdateHandler == null) {
        vmUpdateHandler = getVMDeleteHandler();
      }
      if (buildCompletionEmailHandler == null) {
        buildCompletionEmailHandler = getBuildCompletionEmailHandler();
      }
      if (buildRunStatus == null) {
        buildRunStatus = new HashMap<>();
        buildRunStatus.put(buildId, BuildRunStatus.RUNNING);
      }
      
      return new BuildRunHandler(getAPICoreProperties(updateLineMillis, captureLogsMillis),
          getStorage(), buildProvider, buildRequestProvider, buildStatusProvider,
          getImmutableMapProvider(), quotaProvider, buildOutputProvider, testVersionProvider,
          getShotMetadataProvider(), build, testVersions, captureShotHandlerFactory,
          vmUpdateHandler, buildCompletionEmailHandler,
          webdriverLogHandler, localAssetsToCloudHandler, driver, getBuildDir(),
          clock, buildRunStatus, getZwlApiSupplier(zwlApi));
    }
  }
}
