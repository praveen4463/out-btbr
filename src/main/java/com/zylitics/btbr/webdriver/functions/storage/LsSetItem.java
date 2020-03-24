package com.zylitics.btbr.webdriver.functions.storage;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;

public class LsSetItem extends AbstractSetItem {
  
  public LsSetItem(APICoreProperties.Webdriver wdProps,
                       BuildCapability buildCapability,
                       RemoteWebDriver driver,
                       PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "lsSetItem";
  }
  
  @Override
  protected void set(String key, String value) {
    localStorage.setItem(key, value);
  }
}
