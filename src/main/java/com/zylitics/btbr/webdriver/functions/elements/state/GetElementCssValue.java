package com.zylitics.btbr.webdriver.functions.elements.state;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;

public class GetElementCssValue extends AbstractElementNamedProperty {
  
  public GetElementCssValue(APICoreProperties.Webdriver wdProps,
                             BuildCapability buildCapability,
                             RemoteWebDriver driver,
                             PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "getElementCssValue";
  }
  
  @Override
  protected String get(RemoteWebElement element, String propertyName) {
    return element.getCssValue(propertyName);
  }
}