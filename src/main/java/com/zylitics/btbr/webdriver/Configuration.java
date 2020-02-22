package com.zylitics.btbr.webdriver;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.session.AbstractDriverSessionProvider;
import com.zylitics.btbr.webdriver.session.ChromeDriverSessionProvider;
import com.zylitics.btbr.webdriver.session.FirefoxDriverSessionProvider;
import com.zylitics.btbr.webdriver.session.IEDriverSessionProvider;

import org.openqa.selenium.remote.BrowserType;

import java.util.Optional;
import java.util.function.Function;

public class Configuration {
  
  public static final String USER_HOME = System.getProperty("user.home");
  
  public static final String PATH_SEPARATOR = System.getProperty("file.separator");
  
  Function<String, Optional<AbstractDriverSessionProvider>> getSessionProviderByBrowser(
      APICoreProperties.Webdriver wdProps, BuildCapability buildCapability) {
    return browser -> {
      AbstractDriverSessionProvider provider = null;
      switch (browser) {
        case BrowserType.CHROME:
          provider = new ChromeDriverSessionProvider(wdProps, buildCapability);
          break;
        case BrowserType.FIREFOX:
          provider = new FirefoxDriverSessionProvider(wdProps, buildCapability);
          break;
        case BrowserType.IE:
          provider = new IEDriverSessionProvider(wdProps, buildCapability);
          break;
      }
      if (provider == null) {
        return Optional.empty();
      }
      return Optional.of(provider);
    };
  }
}
