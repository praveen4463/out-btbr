package com.zylitics.btbr.webdriver.session;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.runner.provider.BrowserProvider;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.nio.file.Path;

public class FirefoxDriverSessionProvider extends AbstractDriverSessionProvider {
  
  public FirefoxDriverSessionProvider(APICoreProperties.Webdriver wdProps
      , BuildCapability buildCapability, Path buildDir, BrowserProvider browserProvider) {
    super(wdProps, buildCapability, buildDir, browserProvider);
  }
  
  @Override
  public RemoteWebDriver createSession() {
    setDriverExe();
    Preconditions.checkNotNull(System.getProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY),
        "gecko driver exe path must be set as system property");
  
    GeckoDriverService driverService = new GeckoDriverService.Builder()
        .usingAnyFreePort()
        .withLogFile(getDriverLogFile())
        .build();
  
    FirefoxOptions firefox = new FirefoxOptions();
    firefox.merge(commonCapabilities);
    String browserBinary = getBrowserBinaryPath();
    if (browserBinary != null) {
      firefox.setBinary(browserBinary);
    }
    FirefoxDriverLogLevel logLevel =
        FirefoxDriverLogLevel.fromString(buildCapability.getWdFirefoxLogLevel());
    if (logLevel != null) {
      firefox.setLogLevel(logLevel);
    }
    // add more browser specific arguments
    
    return new FirefoxDriver(driverService, firefox);
  }
  
  @Override
  protected String getDriverExeSysProp() {
    return GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY;
  }
  
  @Override
  protected String getDriverWinExeName() {
    return "geckodriver.exe";
  }
}
