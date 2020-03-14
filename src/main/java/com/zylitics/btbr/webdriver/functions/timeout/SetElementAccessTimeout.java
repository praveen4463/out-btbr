package com.zylitics.btbr.webdriver.functions.timeout;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;

public class SetElementAccessTimeout extends Timeouts {
  
  public SetElementAccessTimeout(APICoreProperties.Webdriver wdProps,
                            BuildCapability buildCapability,
                            RemoteWebDriver driver,
                            PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "setElementAccessTimeout";
  }
  
  @Override
  protected void setTimeout(int timeout) {
    // build caps are created per build, thus we can overwrite directly. Rest of the build will
    // work using this timeout setting.
    buildCapability.setWdTimeoutsElementAccess(timeout);
  }
}
