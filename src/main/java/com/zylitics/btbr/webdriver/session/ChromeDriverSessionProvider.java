package com.zylitics.btbr.webdriver.session;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.runner.provider.BrowserProvider;
import com.zylitics.btbr.util.CollectionUtil;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ChromeDriverSessionProvider extends AbstractDriverSessionProvider {
  
  public ChromeDriverSessionProvider(Build build, APICoreProperties.Webdriver wdProps
      , BuildCapability buildCapability, Path buildDir, BrowserProvider browserProvider) {
    super(build, wdProps, buildCapability, buildDir, browserProvider);
  }
  
  @Override
  public RemoteWebDriver createSession() {
    setDriverExe();
    // still check if exe available in case we didn't set for some OS
    Preconditions.checkNotNull(System.getProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY),
        "chrome driver exe path must be set as system property");
    
    ChromeDriverService driverService = new ChromeDriverService.Builder()
        .usingAnyFreePort()
        .withLogFile(getDriverLogFile())
        .withAppendLog(true)
        .withVerbose(buildCapability.isWdChromeVerboseLogging())
        .withSilent(!build.isCaptureDriverLogs() || buildCapability.isWdChromeSilentOutput())
        .build();
  
    ChromeOptions chrome = new ChromeOptions();
    chrome.merge(commonCapabilities);
    String browserBinary = getBrowserBinaryPath();
    if (browserBinary != null) {
      chrome.setBinary(browserBinary);
    }
    // chrome.addArguments("start-maximized"); don't use this for now as other browsers don't have
    // this option, also sometimes chrome doesn't start maximized even with this argument, it's
    // safe to explicitly send maximize every time.
    
    // add more browser specific arguments
    
    // add performance logging if asked to
    if (buildCapability.isWdChromeEnableNetwork() || buildCapability.isWdChromeEnablePage()) {
      LoggingPreferences loggingPreferences =
          (LoggingPreferences) chrome.getCapability(CapabilityType.LOGGING_PREFS);
      loggingPreferences.enable(LogType.PERFORMANCE, Level.ALL);
      Map<String, Object> perfLogPrefs = new HashMap<>(CollectionUtil.getInitialCapacity(2));
      perfLogPrefs.put("enableNetwork", buildCapability.isWdChromeEnableNetwork());
      perfLogPrefs.put("enablePage", buildCapability.isWdChromeEnablePage());
      chrome.setExperimentalOption("perfLoggingPrefs", perfLogPrefs);
    }
    
    return new ChromeDriver(driverService, chrome);
  }
  
  @Override
  protected String getDriverExeSysProp() {
    return ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY;
  }
  
  @Override
  protected String getDriverWinExeName() {
    return "chromedriver.exe";
  }
}
