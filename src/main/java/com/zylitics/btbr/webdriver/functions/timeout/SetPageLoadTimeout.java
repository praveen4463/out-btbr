package com.zylitics.btbr.webdriver.functions.timeout;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

public class SetPageLoadTimeout extends Timeouts {
  
  public SetPageLoadTimeout(APICoreProperties.Webdriver wdProps,
                          BuildCapability buildCapability,
                          RemoteWebDriver driver,
                          PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "setPageLoadTimeout";
  }
  
  @Override
  protected void setTimeout(int timeout) {
    options.timeouts().pageLoadTimeout(timeout, TimeUnit.MILLISECONDS);
    // build caps are created per build, thus we can overwrite directly. Rest of the build will
    // work using this timeout setting.
    buildCapability.setWdTimeoutsPageLoad(timeout);
  }
}
