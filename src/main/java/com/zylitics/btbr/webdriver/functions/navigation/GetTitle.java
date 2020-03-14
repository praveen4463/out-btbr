package com.zylitics.btbr.webdriver.functions.navigation;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;

public class GetTitle extends GetFromPage {
  
  public GetTitle(APICoreProperties.Webdriver wdProps,
                       BuildCapability buildCapability,
                       RemoteWebDriver driver,
                       PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "getTitle";
  }
  
  @Override
  protected String get() {
    return driver.getTitle();
  }
}
