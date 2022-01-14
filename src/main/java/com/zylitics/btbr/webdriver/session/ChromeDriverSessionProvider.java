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
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    
    ChromeDriverService.Builder driverServiceBuilder = new ChromeDriverService.Builder()
        .usingAnyFreePort();
    if (build.isCaptureDriverLogs()) {
      driverServiceBuilder
          .withLogFile(getDriverLogFile())
          .withAppendLog(true)
          .withVerbose(buildCapability.isWdChromeVerboseLogging())
          .withSilent(buildCapability.isWdChromeSilentOutput());
    } else {
      driverServiceBuilder.withSilent(true);
    }
  
    ChromeOptions chrome = new ChromeOptions();
    chrome.merge(commonCapabilities);
    String browserBinary = getBrowserBinaryPath();
    if (browserBinary != null) {
      chrome.setBinary(browserBinary);
    }
  
    Optional<List<String>> optionalMobileDeviceDims = getMobileDeviceDimensions();
    if (optionalMobileDeviceDims.isPresent()) {
      List<String> mobileDeviceDims = optionalMobileDeviceDims.get();
      Map<String, Object> deviceMetrics = new HashMap<>(CollectionUtil.getInitialCapacity(3));
      deviceMetrics.put("width", mobileDeviceDims.get(0));
      deviceMetrics.put("height", mobileDeviceDims.get(1));
      deviceMetrics.put("pixelRatio", 3.0);
      
      Map<String, Object> mobileEmulation = new HashMap<>(CollectionUtil.getInitialCapacity(2));
      mobileEmulation.put("deviceMetrics", deviceMetrics);
      mobileEmulation.put("userAgent",
          "Mozilla/5.0 (Linux; Android 4.2.1; en-us; Nexus 5 Build/JOP40D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19"
      );
      chrome.setExperimentalOption("mobileEmulation", mobileEmulation);
    }
    
    // chrome.addArguments("start-maximized"); don't use this for now as other browsers don't have
    // this option, also sometimes chrome doesn't start maximized even with this argument, it's
    // safe to explicitly send maximize every time.
    
    // add more browser specific arguments
    
    // add performance logging if asked to
    if (build.isCaptureDriverLogs() &&
        (buildCapability.isWdChromeEnableNetwork() || buildCapability.isWdChromeEnablePage())) {
      LoggingPreferences loggingPreferences =
          (LoggingPreferences) chrome.getCapability(CapabilityType.LOGGING_PREFS);
      loggingPreferences.enable(LogType.PERFORMANCE, Level.ALL);
      Map<String, Object> perfLogPrefs = new HashMap<>(CollectionUtil.getInitialCapacity(2));
      perfLogPrefs.put("enableNetwork", buildCapability.isWdChromeEnableNetwork());
      perfLogPrefs.put("enablePage", buildCapability.isWdChromeEnablePage());
      chrome.setExperimentalOption("perfLoggingPrefs", perfLogPrefs);
    }
    
    return new ChromeDriver(driverServiceBuilder.build(), chrome);
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
