package com.zylitics.btbr.webdriver.session;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public abstract class AbstractDriverSessionProvider {
  
  private static final String BROWSER_BINARY_PATH_TEMPLATE_WIN =
      "C:\\ProgramData\\browsers\\BROWSER_NAME\\BROWSER_VERSION\\BROWSER_NAME.exe";
  
  final APICoreProperties.Webdriver wdProps;
  
  final BuildCapability buildCapability;
  
  // caps those are same for all browsers, maps buildCapability to selenium's capability.
  final Capabilities commonCapabilities;
  
  final Configuration configuration = new Configuration();
  
  private final Path buildDir;
  
  public AbstractDriverSessionProvider(APICoreProperties.Webdriver wdProps
      , BuildCapability buildCapability, Path buildDir) {
    this.wdProps = wdProps;
    this.buildCapability = buildCapability;
    commonCapabilities = getCommonCapabilities();
  
    this.buildDir = buildDir;
  }
  
  public abstract RemoteWebDriver createSession();
  
  private Capabilities getCommonCapabilities() {
    MutableCapabilities caps = new MutableCapabilities();
    
    // logging
    LoggingPreferences logs = new LoggingPreferences();
    Level clientLogLevel = Level.INFO;
    if (Boolean.getBoolean(wdProps.getVerboseClientLogsProp())) {
      clientLogLevel = Level.ALL;
    }
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
      throw new RuntimeException(buildDir.toAbsolutePath().toString() + " isn't a directory");
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
    if (buildCapability.getWdPlatformName().equals(Platform.MAC.name())) {
      return null;
    }
    String template = null;
    if (buildCapability.getWdPlatformName().equals(Platform.WINDOWS.name())) {
      template = BROWSER_BINARY_PATH_TEMPLATE_WIN;
    }
    
    return Preconditions.checkNotNull(template, "no browser binary template")
        .replace("BROWSER_NAME", buildCapability.getWdBrowserName())
        .replace("BROWSER_VERSION", buildCapability.getWdBrowserVersion());
  }
}
