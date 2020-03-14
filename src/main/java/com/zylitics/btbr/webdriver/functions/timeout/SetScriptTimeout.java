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
  }
}
