package com.zylitics.btbr.webdriver.functions.timeout;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

public class SetScriptTimeout extends Timeouts {
  
  public SetScriptTimeout(APICoreProperties.Webdriver wdProps,
                          BuildCapability buildCapability,
                          RemoteWebDriver driver,
                          PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "setScriptTimeout";
  }
  
  @Override
  protected void setTimeout(int timeout) {
    options.timeouts().setScriptTimeout(timeout, TimeUnit.MILLISECONDS);
    // build caps are created per build and all functions are instantiated using the same instance,
    // when we overwrite a setting, it will be seen by all function instance thus, Rest of the build
    // will work using this timeout setting.
    buildCapability.setWdTimeoutsScript(timeout);
  }
}
