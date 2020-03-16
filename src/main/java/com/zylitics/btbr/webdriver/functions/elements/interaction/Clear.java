package com.zylitics.btbr.webdriver.functions.elements.interaction;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;

public class Clear extends ClickClear {
  
  public Clear(APICoreProperties.Webdriver wdProps,
               BuildCapability buildCapability,
               RemoteWebDriver driver,
               PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "clear";
  }
  
  @Override
  protected void perform(RemoteWebElement element) {
    element.clear();
  }
}
