package com.zylitics.btbr.webdriver.session;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FirefoxDriverSessionProvider extends AbstractDriverSessionProvider {
  
  private final File driverLogFile;
  
  public FirefoxDriverSessionProvider(APICoreProperties.Webdriver wdProps
      , BuildCapability buildCapability, Path buildDir) {
    super(wdProps, buildCapability, buildDir);
    this.driverLogFile = getDriverLogFile();
  }
  
  FirefoxDriverSessionProvider(APICoreProperties.Webdriver wdProps
      , BuildCapability buildCapability, File driverLogFile) {
    super(wdProps, buildCapability, Paths.get(""));
    this.driverLogFile = driverLogFile;
  }
  
  @Override
  public RemoteWebDriver createSession() {
    Preconditions.checkNotNull(System.getProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY),
        "gecko driver exe path must be set as system property");
  
    GeckoDriverService driverService = new GeckoDriverService.Builder()
        .usingAnyFreePort()
        .withLogFile(driverLogFile)
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
}
