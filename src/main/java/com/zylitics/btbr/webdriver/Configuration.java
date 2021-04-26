package com.zylitics.btbr.webdriver;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.runner.provider.BrowserProvider;
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
      APICoreProperties.Webdriver wdProps, BuildCapability buildCapability, Path buildDir,
      BrowserProvider browserProvider) {
    AbstractDriverSessionProvider provider = null;
    switch (buildCapability.getWdBrowserName()) {
      case BrowserType.CHROME:
        provider = new ChromeDriverSessionProvider(wdProps, buildCapability, buildDir,
            browserProvider);
        break;
      case BrowserType.FIREFOX:
        provider = new FirefoxDriverSessionProvider(wdProps, buildCapability, buildDir,
            browserProvider);
        break;
      case "IE": // our value for IE in db is IE
        provider = new IEDriverSessionProvider(wdProps, buildCapability, buildDir, browserProvider);
        break;
    }
    return Optional.ofNullable(provider);
  }
  
  public int getTimeouts(APICoreProperties.Webdriver wdProps,
                         BuildCapability buildCapability,
                         TimeoutType timeoutType) {
    switch (timeoutType) {
      case PAGE_LOAD:
        return buildCapability.getWdTimeoutsPageLoad() >= 0
            ? buildCapability.getWdTimeoutsPageLoad()
            : wdProps.getDefaultTimeoutPageLoad();
      case JAVASCRIPT:
        return buildCapability.getWdTimeoutsScript() >= 0
            ? buildCapability.getWdTimeoutsScript()
            : wdProps.getDefaultTimeoutScript();
    }
    throw new RuntimeException("Unsupported timeout type " + timeoutType);
  }
}
