package com.zylitics.btbr.runner;

import com.google.cloud.storage.Storage;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.http.RequestBuildRun;
import com.zylitics.btbr.model.*;
import com.zylitics.btbr.runner.provider.*;
import com.zylitics.btbr.util.DateTimeUtil;
import com.zylitics.btbr.webdriver.logs.WebdriverLogHandler;
import com.zylitics.zwl.antlr4.StoringErrorListener;
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
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
    
    TestVersion testVersion1 = new TestVersion().setTestVersionId(testVersionId1).setName("v-1")
        .setZwlProgram(new ZwlProgram().setCode("a = 1"));
    TestVersion testVersion2 = new TestVersion().setTestVersionId(testVersionId2).setName("v-1")
        .setZwlProgram(new ZwlProgram().setCode("b = 1"));
    
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
    TestVersion testVersion1 = new TestVersion().setTestVersionId(1).setName("v-1")
        .setZwlProgram(new ZwlProgram().setCode("a = 1"));
    TestVersion testVersion2 = new TestVersion().setTestVersionId(2).setName("v-1")
        .setZwlProgram(new ZwlProgram().setCode("b = 1"));
    TestVersion testVersion3 = new TestVersion().setTestVersionId(3).setName("v-1")
        .setZwlProgram(new ZwlProgram().setCode("c = 1"));
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
    verify(driver.manage(), times(2)).deleteAllCookies();
    verify(driver.manage().timeouts(), times(2)).pageLoadTimeout(anyLong(),
        eq(TimeUnit.MILLISECONDS));
    verify(driver.manage().timeouts(), times(2)).setScriptTimeout(anyLong(),
        eq(TimeUnit.MILLISECONDS));
  
    // test with sanitize off
    driver = getRemoteWebDriver();
    addTimeoutMock(driver);
    addTargetLocatorMock(driver);
  
    Build build = getBuild(1);
    BuildCapability buildCapability = build.getBuildCapability();
    buildCapability.setBuildAetKeepSingleWindow(false);
    buildCapability.setBuildAetDeleteAllCookies(false);
    buildCapability.setBuildAetResetTimeouts(false);
    
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
    verify(driver.manage(), never()).deleteAllCookies();
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
    TestVersion testVersion1 = new TestVersion().setTestVersionId(1).setName("v-1")
        .setZwlProgram(new ZwlProgram().setCode("a = 1"));
    TestVersion testVersion2 = new TestVersion().setTestVersionId(2).setName("v-1")
        .setZwlProgram(new ZwlProgram().setCode("b = 1"));
    List<TestVersion> versions = ImmutableList.of(testVersion1, testVersion2);
    Build build = getBuild(buildId);
    RemoteWebDriver driver = getRemoteWebDriver();
    addTimeoutMock(driver);
    addDefaultWinHandle(driver);
    
    BuildStatusProvider buildStatusProvider = getBuildStatusProvider();
    BuildProvider buildProvider = getBuildProvider();
    ZwlProgramOutputProvider zwlProgramOutputProvider = getZwlProgramOutputProvider();
    CaptureShotHandler captureShotHandler = mock(CaptureShotHandler.class);
    WebdriverLogHandler webdriverLogHandler = getWebdriverLogHandler();
    LocalAssetsToCloudHandler localAssetsToCloudHandler = getLocalAssetsToCloudHandler();
    VMDeleteHandler vmDeleteHandler = getVMDeleteHandler();
    Map<Integer, BuildRunStatus> buildRunStatus = new HashMap<>();
    buildRunStatus.put(buildId, BuildRunStatus.RUNNING);
    new Builder()
        .withBuildStatusProvider(buildStatusProvider)
        .withBuildProvider(buildProvider)
        .withZwlProgramOutputProvider(zwlProgramOutputProvider)
        .withBuild(build)
        .withTestVersions(versions)
        .withClock(clock)
        .withCaptureShotHandlerFactory(getCaptureShotHandlerFactory(captureShotHandler))
        .withDriver(driver)
        .withWebdriverLogHandler(webdriverLogHandler)
        .withLocalAssetsToCloudHandler(localAssetsToCloudHandler)
        .withVmDeleteHandler(vmDeleteHandler)
        .withBuildRunStatus(buildRunStatus)
        .build().handle();
    assertEquals(BuildRunStatus.COMPLETED, buildRunStatus.get(buildId));
    
    InOrder inOrder = inOrder(buildStatusProvider, buildProvider, zwlProgramOutputProvider,
        captureShotHandler, driver, webdriverLogHandler, localAssetsToCloudHandler,
        vmDeleteHandler);
    
    inOrder.verify(buildStatusProvider).saveOnStart(new BuildStatusSaveOnStart(buildId,
        testVersionId1, TestStatus.RUNNING, currentDT));
    inOrder.verify(zwlProgramOutputProvider).saveAsync(argThat(matchZwlProgramOutput(
        new ZwlProgramOutput().setBuildId(buildId).setTestVersionId(testVersionId1)
            .setOutput("Executing").setEnded(false).setCreateDate(currentDT))));
    inOrder.verify(captureShotHandler).startShot();
    inOrder.verify(buildStatusProvider).updateOnEnd(new BuildStatusUpdateOnEnd(buildId,
        testVersionId1, TestStatus.SUCCESS, currentDT, null));
    inOrder.verify(zwlProgramOutputProvider).saveAsync(argThat(matchZwlProgramOutput(
        new ZwlProgramOutput().setBuildId(buildId).setTestVersionId(testVersionId1)
            .setOutput("Completed").setEnded(true).setCreateDate(currentDT))));
    // first version done, second started
    inOrder.verify(driver).getWindowHandles();
    inOrder.verify(buildStatusProvider).saveOnStart(new BuildStatusSaveOnStart(buildId,
        testVersionId2, TestStatus.RUNNING, currentDT));
    inOrder.verify(zwlProgramOutputProvider).saveAsync(argThat(matchZwlProgramOutput(
        new ZwlProgramOutput().setBuildId(buildId).setTestVersionId(testVersionId2)
            .setOutput("Executing").setEnded(false).setCreateDate(currentDT))));
    inOrder.verify(buildStatusProvider).updateOnEnd(new BuildStatusUpdateOnEnd(buildId,
        testVersionId2, TestStatus.SUCCESS, currentDT, null));
    inOrder.verify(zwlProgramOutputProvider).saveAsync(argThat(matchZwlProgramOutput(
        new ZwlProgramOutput().setBuildId(buildId).setTestVersionId(testVersionId2)
            .setOutput("Completed").setEnded(true).setCreateDate(currentDT))));
    // second version done, build complete steps will follow
    inOrder.verify(buildProvider).updateOnComplete(new BuildUpdateOnComplete(buildId, currentDT,
        true, null));
    inOrder.verify(zwlProgramOutputProvider).processRemainingAndTearDown();
    inOrder.verify(captureShotHandler).stopShot();
    inOrder.verify(captureShotHandler).blockUntilFinish();
    inOrder.verify(webdriverLogHandler).capture();
    inOrder.verify(driver).quit();
    inOrder.verify(localAssetsToCloudHandler).store();
    inOrder.verify(vmDeleteHandler).delete(eq(buildId), anyString());
  }
  
  @Test
  void validateOnInterpretZwlLangException() {
    int buildId = 1;
    int testVersionId = 1;
    String error = "invalid identifier";
    BuildStatusProvider buildStatusProvider = getBuildStatusProvider();
    BuildProvider buildProvider = getBuildProvider();
    Clock clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));
    OffsetDateTime currentDT = DateTimeUtil.getCurrent(clock);
    ZwlProgramOutputProvider zwlProgramOutputProvider = getZwlProgramOutputProvider();
    ZwlLangException zwEx = new ZwlLangException(error);
    // when single version is run
    TestVersion testVersion1 = new TestVersion().setTestVersionId(testVersionId).setName("v-1")
        .setZwlProgram(new ZwlProgram().setCode("a = 1"));
    ZwlApi zwlApi = mock(ZwlApi.class);
    doAnswer(i -> {
      throw zwEx;
    }).when(zwlApi).interpret(any(ZwlWdTestProperties.class), any(ZwlInterpreterVisitor.class));
    new Builder()
        .withBuildId(buildId)
        .withBuildStatusProvider(buildStatusProvider)
        .withClock(clock)
        .withBuildProvider(buildProvider)
        .withZwlProgramOutputProvider(zwlProgramOutputProvider)
        .withTestVersions(ImmutableList.of(testVersion1))
        .withZwlApi(zwlApi).build().handle();
    
    InOrder inOrder = inOrder(buildStatusProvider, buildProvider, zwlProgramOutputProvider);
    inOrder.verify(buildStatusProvider).updateOnEnd(argThat(matchBuildStatusUpdateOnEnd(
        new BuildStatusUpdateOnEnd(buildId, testVersionId, TestStatus.ERROR, currentDT
            , new ExceptionTranslationProvider(new StoringErrorListener()).get(zwEx)))));
    inOrder.verify(zwlProgramOutputProvider).saveAsync(argThat(matchZwlProgramOutput(
        new ZwlProgramOutput().setBuildId(buildId).setTestVersionId(testVersionId)
            .setOutput("Exception").setEnded(true).setCreateDate(currentDT))));
    inOrder.verify(buildProvider).updateOnComplete(argThat(matchBuildUpdateOnComplete(
        new BuildUpdateOnComplete(buildId, currentDT, false, "An exception"))));
    // when multiple version is run
  }
  
  // TODO: Write more tests later on, for now this much including the E2E should be enough
  
  // the 'output' field of ZwlProgramOutput is what causes this matcher to be created because I
  // don't want to match on exact term but partial, I didn't want to create an 'equals' in
  // ZwlProgramOutput that matches on partial 'output' string as it's a test requirement only.
  private ArgumentMatcher<ZwlProgramOutput> matchZwlProgramOutput(ZwlProgramOutput expected) {
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
        && z.isSuccess() == expected.isSuccess()
        && z.getError().startsWith(expected.getError())
        && z.getEndDate().equals(expected.getEndDate());
  }
  
  private RequestBuildRun getRequestBuildRun(int buildId) {
    RequestBuildRun request = new RequestBuildRun();
    request.setBuildId(buildId);
    request.setVmDeleteUrl("http://10.10.2.3");
    return request;
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
  
  private BuildProvider getBuildProvider() {
    BuildProvider buildProvider = mock(BuildProvider.class);
    when(buildProvider.updateOnComplete(any(BuildUpdateOnComplete.class))).thenReturn(1);
    return buildProvider;
  }
  
  private BuildStatusProvider getBuildStatusProvider() {
    BuildStatusProvider buildStatusProvider = mock(BuildStatusProvider.class);
    when(buildStatusProvider.saveOnStart(any(BuildStatusSaveOnStart.class))).thenReturn(1);
    when(buildStatusProvider.updateOnEnd(any(BuildStatusUpdateOnEnd.class))).thenReturn(1);
    return buildStatusProvider;
  }
  
  private ImmutableMapProvider getImmutableMapProvider() {
    ImmutableMapProvider immutableMapProvider = mock(ImmutableMapProvider.class);
    when(immutableMapProvider.getMapFromTableByUser(anyInt(), anyString()))
        .thenReturn(Optional.empty());
    when(immutableMapProvider.getMapFromTableByBuild(anyInt(), anyString()))
        .thenReturn(Optional.empty());
    return immutableMapProvider;
  }
  
  private ShotMetadataProvider getShotMetadataProvider() {
    return mock(ShotMetadataProvider.class);
  }
  
  private ZwlProgramOutputProvider getZwlProgramOutputProvider() {
    return mock(ZwlProgramOutputProvider.class);
  }
  
  private Build getBuild(int buildId) {
    Build build = new Build();
    build.setUserId(1);
    build.setBuildId(buildId);
    build.setBuildVMId(1);
  
    BuildCapability buildCapability = new BuildCapability();
    buildCapability.setWdTimeoutsScript(-1);
    buildCapability.setWdTimeoutsPageLoad(-1);
    buildCapability.setWdTimeoutsElementAccess(-1);
    buildCapability.setBuildAetDeleteAllCookies(true);
    buildCapability.setBuildAetResetTimeouts(true);
    buildCapability.setBuildAetKeepSingleWindow(true);
    buildCapability.setBuildAetUpdateUrlBlank(true);
    buildCapability.setWdBrwStartMaximize(true);
    buildCapability.setShotBucketSessionStorage("shot-bucket");
    build.setBuildCapability(buildCapability);
    return build;
  }
  
  private List<TestVersion> getVersions() {
    List<TestVersion> versions = new ArrayList<>();
    TestVersion version = new TestVersion();
    version.setTestVersionId(1);
    version.setName("v-1");
    version.setZwlProgram(new ZwlProgram().setCode("a = 1"));
    versions.add(version);
  
    version = new TestVersion();
    version.setTestVersionId(2);
    version.setName("v-1");
    version.setZwlProgram(new ZwlProgram().setCode("a = 1"));
    versions.add(version);
    return versions;
  }
  
  private CaptureShotHandler.Factory getCaptureShotHandlerFactory(
      CaptureShotHandler captureShotHandler) {
    CaptureShotHandler.Factory factory = mock(CaptureShotHandler.Factory.class);
    when(factory.create(any(APICoreProperties.Shot.class), any(ShotMetadataProvider.class),
        any(Storage.class), any(Build.class), anyString(), anyString(),
        any(CurrentTestVersion.class))).thenReturn(captureShotHandler);
    return factory;
  }
  
  private VMDeleteHandler getVMDeleteHandler() {
    return mock(VMDeleteHandler.class);
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
    private BuildStatusProvider buildStatusProvider = null;
    private ZwlProgramOutputProvider zwlProgramOutputProvider = null;
    private Build build = null;
    private RemoteWebDriver driver = null;
    private List<TestVersion> testVersions = null;
    private Clock clock = null;
    private CaptureShotHandler.Factory captureShotHandlerFactory = null;
    private WebdriverLogHandler webdriverLogHandler = null;
    private LocalAssetsToCloudHandler localAssetsToCloudHandler = null;
    private VMDeleteHandler vmDeleteHandler = null;
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
  
    Builder withBuildStatusProvider(BuildStatusProvider buildStatusProvider) {
      this.buildStatusProvider = buildStatusProvider;
      return this;
    }
  
    Builder withZwlProgramOutputProvider(ZwlProgramOutputProvider zwlProgramOutputProvider) {
      this.zwlProgramOutputProvider = zwlProgramOutputProvider;
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
  
    public Builder withVmDeleteHandler(VMDeleteHandler vmDeleteHandler) {
      this.vmDeleteHandler = vmDeleteHandler;
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
        buildProvider = getBuildProvider();
      }
      if (buildStatusProvider == null) {
        buildStatusProvider = getBuildStatusProvider();
      }
      if (zwlProgramOutputProvider == null) {
        zwlProgramOutputProvider = getZwlProgramOutputProvider();
      }
      if (build == null) {
        build = getBuild(buildId);
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
      if (vmDeleteHandler == null) {
        vmDeleteHandler = getVMDeleteHandler();
      }
      if (buildRunStatus == null) {
        buildRunStatus = new HashMap<>();
        buildRunStatus.put(buildId, BuildRunStatus.RUNNING);
      }
      
      return new BuildRunHandler(getRequestBuildRun(buildId),
          getAPICoreProperties(updateLineMillis, captureLogsMillis),
          getStorage(), buildProvider, buildStatusProvider,
          getImmutableMapProvider(), getShotMetadataProvider(), zwlProgramOutputProvider, build,
          testVersions, captureShotHandlerFactory, vmDeleteHandler,
          webdriverLogHandler, localAssetsToCloudHandler, driver, getBuildDir(),
          clock, buildRunStatus, getZwlApiSupplier(zwlApi));
    }
  }
}
