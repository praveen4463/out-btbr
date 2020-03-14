package com.zylitics.btbr.webdriver.session;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ChromeDriverSessionProvider extends AbstractDriverSessionProvider {
  
  public ChromeDriverSessionProvider(APICoreProperties.Webdriver wdProps
      , BuildCapability buildCapability) {
    super(wdProps, buildCapability);
  }
  
  @Override
  public RemoteWebDriver createSession() {
    Preconditions.checkNotNull(System.getProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY),
        "chrome driver exe path must be set as system property");
    
    ChromeDriverService driverService = new ChromeDriverService.Builder()
        .usingAnyFreePort()
        .withLogFile(new File(getDriverLogFilePath()))
        .withAppendLog(true)
        .build();
  
    ChromeOptions chrome = new ChromeOptions();
    chrome.merge(commonCapabilities);
    chrome.setBinary(getBrowserBinaryPath());
    // chrome.addArguments("start-maximized"); don't use this for now as other browsers don't have
    // this option, also sometimes chrome doesn't start maximized even with this argument, it's
    // safe to explicitly send maximize every time.
    
    // add more browser specific arguments
    
    // add performance logging if asked to
    if (buildCapability.isChromeEnableNetwork() || buildCapability.isChromeEnablePage()) {
      LoggingPreferences loggingPreferences =
          (LoggingPreferences) chrome.getCapability(CapabilityType.LOGGING_PREFS);
      loggingPreferences.enable(LogType.PERFORMANCE, Level.ALL);
      Map<String, Object> perfLogPrefs = new HashMap<>(3);
      perfLogPrefs.put("enableNetwork", buildCapability.isChromeEnableNetwork());
      perfLogPrefs.put("enablePage", buildCapability.isChromeEnablePage());
      chrome.setExperimentalOption("perfLoggingPrefs", perfLogPrefs);
    }
    
    return new ChromeDriver(driverService, chrome);
  }
}
