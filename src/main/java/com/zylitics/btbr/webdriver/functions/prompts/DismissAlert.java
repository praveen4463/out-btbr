package com.zylitics.btbr.webdriver.functions.prompts;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;

public class DismissAlert extends DismissAccept {
  
  public DismissAlert(APICoreProperties.Webdriver wdProps,
                          BuildCapability buildCapability,
                          RemoteWebDriver driver,
                          PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "dismissAlert";
  }
  
  @Override
  protected void alertOperation() {
    targetLocator.alert().dismiss();
  }
}
