package com.zylitics.btbr.webdriver;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.session.AbstractDriverSessionProvider;
import com.zylitics.btbr.webdriver.session.ChromeDriverSessionProvider;
import com.zylitics.btbr.webdriver.session.FirefoxDriverSessionProvider;
import com.zylitics.btbr.webdriver.session.IEDriverSessionProvider;

import org.openqa.selenium.remote.BrowserType;

import java.nio.file.Path;
import java.util.Optional;

public class Configuration {
  
  public static final String SYS_DEF_TEMP_DIR = System.getProperty("java.io.tmpdir");
  
  public Optional<AbstractDriverSessionProvider> getSessionProviderByBrowser(
      APICoreProperties.Webdriver wdProps, BuildCapability buildCapability, Path buildDir) {
    AbstractDriverSessionProvider provider = null;
    switch (buildCapability.getWdBrowserName()) {
      case BrowserType.CHROME:
        provider = new ChromeDriverSessionProvider(wdProps, buildCapability, buildDir);
        break;
      case BrowserType.FIREFOX:
        provider = new FirefoxDriverSessionProvider(wdProps, buildCapability, buildDir);
        break;
      case BrowserType.IE:
        provider = new IEDriverSessionProvider(wdProps, buildCapability, buildDir);
        break;
    }
    return Optional.ofNullable(provider);
  }
  
  public int getTimeouts(APICoreProperties.Webdriver wdProps,
                         BuildCapability buildCapability,
                         TimeoutType timeoutType) {
    switch (timeoutType) {
      case ELEMENT_ACCESS:
      default:
        return buildCapability.getWdTimeoutsElementAccess() >= 0
            ? buildCapability.getWdTimeoutsElementAccess()
            : wdProps.getDefaultTimeoutElementAccess();
      case PAGE_LOAD:
        return buildCapability.getWdTimeoutsPageLoad() >= 0
            ? buildCapability.getWdTimeoutsPageLoad()
            : wdProps.getDefaultTimeoutPageLoad();
      case JAVASCRIPT:
        return buildCapability.getWdTimeoutsScript() >= 0
            ? buildCapability.getWdTimeoutsScript()
            : wdProps.getDefaultTimeoutScript();
      case NEW_WINDOW:
        return wdProps.getDefaultTimeoutNewWindow();
      // currently new window timeout isn't accepted through build caps.
    }
  }
}
