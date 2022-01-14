package com.zylitics.btbr.webdriver.session;

import com.google.cloud.Tuple;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.runner.provider.BrowserProvider;
import com.zylitics.btbr.util.IOUtil;
import com.zylitics.btbr.webdriver.Configuration;
import com.zylitics.btbr.webdriver.TimeoutType;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.swing.text.html.Option;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

/*
TODO: We should send browser specific switches so that whenever browser starts, we clear all
 type of storage, caches, cookies and whatever else saved by browser from last session. This
 should be done from here before a session is created.
 */
public abstract class AbstractDriverSessionProvider {
  
  private static final String BROWSER_BINARY_PATH_TEMPLATE_WIN =
      "C:\\ProgramData\\browsers\\BROWSER_NAME\\BROWSER_VERSION\\BROWSER_NAME.exe";
  
  private static final String BROWSER_DRIVER_EXE_PATH_TEMPLATE_WIN =
      "C:\\ProgramData\\webdrivers\\BROWSER_NAME\\DRIVER_VERSION\\DRIVER_EXE";
  
  final Build build;
  
  final APICoreProperties.Webdriver wdProps;
  
  final BuildCapability buildCapability;
  
  // caps those are same for all browsers, maps buildCapability to selenium's capability.
  final Capabilities commonCapabilities;
  
  final Configuration configuration = new Configuration();
  
  private final Path buildDir;
  
  private final BrowserProvider browserProvider;
  
  public AbstractDriverSessionProvider(Build build, APICoreProperties.Webdriver wdProps
      , BuildCapability buildCapability, Path buildDir, BrowserProvider browserProvider) {
    this.build = build;
    this.wdProps = wdProps;
    this.buildCapability = buildCapability;
    commonCapabilities = getCommonCapabilities();
  
    this.buildDir = buildDir;
    this.browserProvider = browserProvider;
  }
  
  public abstract RemoteWebDriver createSession();
  
  // Just set sys property to the dir path, path should already exist and there is no need to download
  // driver when it's not available on machine. The script we run before starting session takes care
  // of figuring out whether the driver exist on machine, if not downloads and puts in place. This is
  // not done from here as that should be faster from script and code for that is already written. We
  // could later change that and do it from here too. I could have done setting sys prop from script
  // but a script can't set sys prop to a running jvm.
  protected void setDriverExe() {
    if (Platform.fromString(buildCapability.getWdPlatformName()).is(Platform.MAC)) {
      return;
    }
    Optional<String> driverVersion = browserProvider.getDriverVersion(
        buildCapability.getWdBrowserName(),
        buildCapability.getWdBrowserVersion());
    if (!driverVersion.isPresent()) {
      throw new RuntimeException("Couldn't get driver for browser " +
          buildCapability.getWdBrowserName());
    }
    System.setProperty(getDriverExeSysProp(), BROWSER_DRIVER_EXE_PATH_TEMPLATE_WIN
        .replace("BROWSER_NAME", buildCapability.getWdBrowserName())
        .replace("DRIVER_VERSION", driverVersion.get())
        .replace("DRIVER_EXE", getDriverWinExeName()));
  }
  
  private Capabilities getCommonCapabilities() {
    MutableCapabilities caps = new MutableCapabilities();
    
    // logging
    LoggingPreferences logs = new LoggingPreferences();
    Level clientLogLevel = Level.INFO;
    if (Boolean.getBoolean(wdProps.getVerboseClientLogsProp())) {
      clientLogLevel = Level.ALL;
    }
    // TODO: for some reason client logs aren't being captured, currently I don't want to
    //  investigate as they aren't of much use, just putting a note for future.
    logs.enable(LogType.CLIENT, clientLogLevel);
  
    if (Boolean.getBoolean(wdProps.getEnableProfilerLogsProp())) {
      logs.enable(LogType.PROFILER, Level.ALL);
      caps.setCapability(CapabilityType.ENABLE_PROFILING_CAPABILITY, true);
    }
    
    caps.setCapability(CapabilityType.LOGGING_PREFS, logs);
    
    caps.setCapability(CapabilityType.BROWSER_VERSION, buildCapability.getWdBrowserVersion());
  
    Preconditions.checkArgument(!Strings.isNullOrEmpty(buildCapability.getWdPlatformName()),
        "platformName capability can't be empty");
    caps.setCapability(CapabilityType.PLATFORM_NAME, buildCapability.getWdPlatformName());
  
    caps.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS,
        buildCapability.isWdAcceptInsecureCerts());
    
    caps.setCapability(CapabilityType.PAGE_LOAD_STRATEGY
        , Strings.isNullOrEmpty(buildCapability.getWdPageLoadStrategy())
            ? wdProps.getDefaultPageLoadStrategy()
            : buildCapability.getWdPageLoadStrategy());
    
    // Looks like this capability isn't supported by chrome, let's not use it for now.
    //caps.setCapability("setWindowRect", buildCapability.isWdSetWindowRect());
    
    // timeouts, if user sent timeout, use that otherwise use our own defaults.
    Map<String, Object> timeouts = new HashMap<>(3);
    timeouts.put("script",
        configuration.getTimeouts(wdProps, buildCapability, TimeoutType.JAVASCRIPT));
    timeouts.put("pageLoad",
        configuration.getTimeouts(wdProps, buildCapability, TimeoutType.PAGE_LOAD));
    caps.setCapability("timeouts", timeouts); // no need to convert timeout to json from here, every
    // map will be converted internally.
    
    caps.setCapability(CapabilityType.STRICT_FILE_INTERACTABILITY,
        buildCapability.isWdStrictFileInteractability());
  
    Preconditions.checkArgument(
        !Strings.isNullOrEmpty(buildCapability.getWdUnhandledPromptBehavior()),
            "unhandled prompt behaviour capability can't be empty");
    caps.setCapability(CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR,
        buildCapability.getWdUnhandledPromptBehavior());
    caps.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR,
        buildCapability.getWdUnhandledPromptBehavior());
    
    return caps;
  }
  
  File getDriverLogFile() {
    if (!Files.isDirectory(buildDir)) {
      throw new RuntimeException(buildDir.toAbsolutePath() + " isn't a directory");
    }
    Path driverLogsDir = buildDir.resolve(wdProps.getDriverLogsDir());
    IOUtil.createDir(driverLogsDir);
    Path driverLogsFile = driverLogsDir.resolve(wdProps.getDriverLogsFile());
    // just send absolute path in string and don't create file, drivers should create the file if
    // it doesn't exist.
    return new File(driverLogsFile.toAbsolutePath().toString());
  }
  
  // Note that similar to browser binary, we didn't give driver binary path here in java because the
  // driver version info is in db and batch script was already fetching and preparing that. We just
  // need to use that with the new application rather than grid.
  String getBrowserBinaryPath() {
    if (Platform.fromString(buildCapability.getWdPlatformName()).is(Platform.MAC)) {
      return null;
    }
    String template = null;
    if (Platform.fromString(buildCapability.getWdPlatformName()).is(Platform.WINDOWS)) {
      template = BROWSER_BINARY_PATH_TEMPLATE_WIN;
    }
  
    //noinspection ConstantConditions
    return Preconditions.checkNotNull(template, "no browser binary template")
        .replace("BROWSER_NAME", buildCapability.getWdBrowserName())
        .replace("BROWSER_VERSION", buildCapability.getWdBrowserVersion());
  }
  
  Optional<List<String>> getMobileDeviceDimensions() {
    String mobileRes = buildCapability.getWdMeDeviceResolution();
    if (mobileRes == null) {
      return Optional.empty();
    }
    List<String> dims =
        Splitter.on('x').omitEmptyStrings().splitToList(mobileRes);
    if (dims.size() != 2) {
      throw new RuntimeException("Unexpected device dimensions " + mobileRes);
    }
    return Optional.of(dims);
  }
  
  protected abstract String getDriverExeSysProp();
  
  protected abstract String getDriverWinExeName();
}
