package com.zylitics.btbr.webdriver.session;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;

public class IEDriverSessionProvider extends AbstractDriverSessionProvider {
  
  public IEDriverSessionProvider(APICoreProperties.Webdriver wdProps
      , BuildCapability buildCapability) {
    super(wdProps, buildCapability);
  }
  
  @Override
  public RemoteWebDriver createSession() {
    Preconditions.checkNotNull(
        System.getProperty(InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY),
            "ie driver exe path must be set as system property");
  
    InternetExplorerDriverService driverService = new InternetExplorerDriverService.Builder()
        .usingAnyFreePort()
        .withLogFile(new File(getDriverLogFilePath()))
        .build();
  
    InternetExplorerOptions ie = new InternetExplorerOptions();
    ie.merge(commonCapabilities);
    // add more browser specific options
    
    return new InternetExplorerDriver(driverService, ie);
  }
}
