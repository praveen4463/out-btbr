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
    // build caps are created per build and all functions are instantiated using the same instance,
    // when we overwrite a setting, it will be seen by all function instance thus, Rest of the build
    // will work using this timeout setting.
    buildCapability.setWdTimeoutsPageLoad(timeout);
  }
}
