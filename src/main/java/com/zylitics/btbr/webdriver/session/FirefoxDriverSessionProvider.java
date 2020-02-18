package com.zylitics.btbr.webdriver.session;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;

public class FirefoxDriverSessionProvider extends AbstractDriverSessionProvider {
  
  public FirefoxDriverSessionProvider(APICoreProperties.Webdriver wdProps
      , BuildCapability buildCapability) {
    super(wdProps, buildCapability);
  }
  
  @Override
  public RemoteWebDriver createSession() {
    Preconditions.checkNotNull(System.getProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY),
        "gecko driver exe path must be set as system property");
  
    GeckoDriverService driverService = new GeckoDriverService.Builder()
        .usingAnyFreePort()
        .withLogFile(new File(getDriverLogFilePath()))
        .build();
  
    FirefoxOptions firefox = new FirefoxOptions();
    firefox.merge(commonCapabilities);
    firefox.setBinary(getBrowserBinaryPath());
    // add more browser specific arguments
    
    return new FirefoxDriver(driverService, firefox);
  }
}
