package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;

public class UntilAttributeValueIs extends AbstractAttribute {
  
  public UntilAttributeValueIs(APICoreProperties.Webdriver wdProps,
                     BuildCapability buildCapability,
                     RemoteWebDriver driver,
                     PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "untilAttributeValueIs";
  }
  
  @Override
  boolean desiredState(RemoteWebElement element, String attribute, String value) {
    return element.getAttribute(attribute).equals(value);
  }
}
