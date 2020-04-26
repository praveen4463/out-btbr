package com.zylitics.btbr.webdriver.session;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.ie.*;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class IEDriverSessionProvider extends AbstractDriverSessionProvider {
  
  private final File driverLogFile;
  
  public IEDriverSessionProvider(APICoreProperties.Webdriver wdProps
      , BuildCapability buildCapability, Path buildDir) {
    super(wdProps, buildCapability, buildDir);
    this.driverLogFile = getDriverLogFile();
  }
  
  IEDriverSessionProvider(APICoreProperties.Webdriver wdProps
      , BuildCapability buildCapability, Path buildDir, File driverLogFile) {
    super(wdProps, buildCapability, Paths.get(""));
    this.driverLogFile = driverLogFile;
  }
  
  @Override
  public RemoteWebDriver createSession() {
    Preconditions.checkNotNull(
        System.getProperty(InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY),
            "ie driver exe path must be set as system property");
  
    InternetExplorerDriverService driverService = new InternetExplorerDriverService.Builder()
        .usingAnyFreePort()
        .withLogFile(driverLogFile)
        .build();
    
    InternetExplorerOptions ie = new InternetExplorerOptions();
    ie.merge(commonCapabilities);
    InternetExplorerDriverLogLevel logLevel = null;
    if (buildCapability.getWdIeLogLevel() != null) {
      for (InternetExplorerDriverLogLevel b : InternetExplorerDriverLogLevel.values()) {
        if (buildCapability.getWdIeLogLevel().equalsIgnoreCase(b.toString())) {
          logLevel = b;
          break;
        }
      }
    }
    if (logLevel != null) {
      ie.addCommandSwitches(String.format("--log-level=%s", logLevel.name())); // use name()
      // method rather than toString(), cause IE driver requires exact name as enum identifier.
    }
    ie.withAttachTimeout(Duration.ofMillis(wdProps.getIeDefaultBrowserAttachTimeout()));
    ie.waitForUploadDialogUpTo(Duration.ofMillis(wdProps.getIeDefaultFileUploadDialogTimeout()));
    ElementScrollBehavior scrollBehavior =
        ElementScrollBehavior.fromString(buildCapability.getWdIeElementScrollBehavior());
    if (scrollBehavior != null) {
      ie.elementScrollTo(scrollBehavior);
    }
    if (buildCapability.isWdIeEnablePersistentHovering()) {
      ie.enablePersistentHovering();
    }
    if (buildCapability.isWdIeIntroduceFlakinessByIgnoringSecurityDomains()) {
      ie.introduceFlakinessByIgnoringSecurityDomains();
    }
    if (buildCapability.isWdIeRequireWindowFocus()) {
      ie.requireWindowFocus();
    }
    if (buildCapability.isWdIeDisableNativeEvents()) {
      ie.disableNativeEvents();
    }
    
    return new InternetExplorerDriver(driverService, ie);
  }
}
