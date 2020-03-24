package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;
import java.util.regex.Pattern;

public class UntilAttributeValueLike extends AbstractAttribute {
  
  public UntilAttributeValueLike(APICoreProperties.Webdriver wdProps,
                                 BuildCapability buildCapability,
                                 RemoteWebDriver driver,
                                 PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "untilAttributeValueLike";
  }
  
  @Override
  boolean desiredState(RemoteWebElement element, String attribute, String value) {
    Pattern pattern = getPattern(value);
    return pattern.matcher(element.getAttribute(attribute)).find();
  }
}