package com.zylitics.btbr.webdriver.functions.storage;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.SessionStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;

abstract class AbstractStorage extends AbstractWebdriverFunction {
  
  final LocalStorage localStorage;
  
  final SessionStorage sessionStorage;
  
  public AbstractStorage(APICoreProperties.Webdriver wdProps,
                       BuildCapability buildCapability,
                       RemoteWebDriver driver,
                       PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  
    WebStorage storage = null;
    if (driver instanceof WebStorage) {
      storage = (WebStorage) driver;
    }
    localStorage = storage != null ? storage.getLocalStorage() : null;
    sessionStorage = storage != null ? storage.getSessionStorage() : null;
  }
  
  void printNoStorageMsg() {
    writeBuildOutput(String.format("WARNING: %s doesn't support local/session storage," +
        "  function %s will not return/do anything.", buildCapability.getWdBrowserName(),
        getName()));
  }
}
