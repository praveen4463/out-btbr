package com.zylitics.btbr.webdriver.session;

import static com.zylitics.btbr.webdriver.Configuration.USER_HOME;
import static com.zylitics.btbr.webdriver.Configuration.PATH_SEPARATOR;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public abstract class AbstractDriverSessionProvider {
  
  private static final Json JSON = new Json();
  
  private static final String BROWSER_BINARY_PATH_TEMPLATE_WIN =
      "C:\\ProgramData\\browsers\\BROWSER_NAME\\BROWSER_VERSION\\BROWSER_NAME.exe";
  
  final APICoreProperties.Webdriver wdProps;
  
  final BuildCapability buildCapability;
  
  final Capabilities commonCapabilities;
  
  public AbstractDriverSessionProvider(APICoreProperties.Webdriver wdProps
      , BuildCapability buildCapability) {
    this.wdProps = wdProps;
    this.buildCapability = buildCapability;
    commonCapabilities = getCommonCapabilities();
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
    
    caps.setCapability(CapabilityType.BROWSER_VERSION, buildCapability.getWdBrowserName());
  
    Preconditions.checkArgument(!Strings.isNullOrEmpty(buildCapability.getWdPlatformName()),
        "platformName capability can't be empty");
    caps.setCapability(CapabilityType.PLATFORM_NAME, buildCapability.getWdPlatformName());
  
    caps.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS,
        buildCapability.isWdAcceptInsecureCerts());
    
    caps.setCapability(CapabilityType.PAGE_LOAD_STRATEGY
        , Strings.isNullOrEmpty(buildCapability.getWdPageLoadStrategy())
            ? wdProps.getDefaultPageLoadStrategy()
            : buildCapability.getWdPageLoadStrategy());
    
    caps.setCapability("setWindowRect", buildCapability.isWdSetWindowRect());
    
    // timeouts
    Map<String, Object> timeouts = new HashMap<>(4);
    String timeoutsJson = null;
    if (buildCapability.getWdTimeoutsScript() > 0) {
      timeouts.put("script", buildCapability.getWdTimeoutsScript());
    }
    if (buildCapability.getWdTimeoutsPageLoad() > 0) {
      timeouts.put("pageLoad", buildCapability.getWdTimeoutsPageLoad());
    }
    if (buildCapability.getWdTimeoutsImplicit() > 0) {
      timeouts.put("implicit", buildCapability.getWdTimeoutsImplicit());
    }
    
    if (timeouts.size() > 0) {
      timeoutsJson = JSON.toJson(timeouts);
    }
    if (timeoutsJson != null) {
      caps.setCapability("timeouts", timeoutsJson);
    }
    
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
  
  String getDriverLogFilePath() {
    String internalLogsDir = wdProps.getInternalLogsDir();
    String driverLogsFile = wdProps.getDriverLogsFile();
    
    return String.format("%s%s%s%s%s", USER_HOME, PATH_SEPARATOR, internalLogsDir, PATH_SEPARATOR,
        driverLogsFile);
  }
  
  String getBrowserBinaryPath() {
    String template = null;
    if (buildCapability.getWdPlatformName().equals(Platform.WINDOWS.name())) {
      template = BROWSER_BINARY_PATH_TEMPLATE_WIN;
    }
    
    return Preconditions.checkNotNull(template, "no browser binary template")
        .replace("BROWSER_NAME", buildCapability.getWdBrowserName())
        .replace("BROWSER_VERSION", buildCapability.getWdBrowserVersion());
  }
}
