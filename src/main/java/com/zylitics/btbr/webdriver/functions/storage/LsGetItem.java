package com.zylitics.btbr.webdriver.functions.storage;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;

public class LsGetItem extends AbstractGetItem {
  
  public LsGetItem(APICoreProperties.Webdriver wdProps,
                         BuildCapability buildCapability,
                         RemoteWebDriver driver,
                         PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "lsGetItem";
  }
  
  @Override
  protected String get(String key) {
    return localStorage.getItem(key);
  }
}
