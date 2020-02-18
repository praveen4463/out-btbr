package com.zylitics.btbr.webdriver;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.executor.AbstractBrwExtCmdExecutor;
import com.zylitics.btbr.webdriver.executor.ChromeBrwExtCmdExecutor;
import com.zylitics.btbr.webdriver.executor.FirefoxBrwExtCmdExecutor;
import com.zylitics.btbr.webdriver.executor.IEBrwExtCmdExecutor;
import com.zylitics.btbr.webdriver.session.AbstractDriverSessionProvider;
import com.zylitics.btbr.webdriver.session.ChromeDriverSessionProvider;
import com.zylitics.btbr.webdriver.session.FirefoxDriverSessionProvider;
import com.zylitics.btbr.webdriver.session.IEDriverSessionProvider;

import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.RemoteWebDriver;

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
        case BrowserType.FIREFOX:
          provider = new FirefoxDriverSessionProvider(wdProps, buildCapability);
        case BrowserType.IE:
          provider = new IEDriverSessionProvider(wdProps, buildCapability);
      }
      if (provider == null) {
        return Optional.empty();
      }
      return Optional.of(provider);
    };
  }
  
  Function<String, Optional<AbstractBrwExtCmdExecutor>> getBrwExtCmdExecutorByBrowser(
      APICoreProperties.Webdriver wdProps, RemoteWebDriver driver) {
    return browser -> {
      AbstractBrwExtCmdExecutor executor = null;
      switch (browser) {
        case BrowserType.CHROME:
          executor = new ChromeBrwExtCmdExecutor(wdProps, driver);
        case BrowserType.FIREFOX:
          executor = new FirefoxBrwExtCmdExecutor(wdProps, driver);
        case BrowserType.IE:
          executor = new IEBrwExtCmdExecutor(wdProps, driver);
      }
      if (executor == null) {
        return Optional.empty();
      }
      return Optional.of(executor);
    };
  }
  
  
}
