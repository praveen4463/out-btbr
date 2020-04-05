package com.zylitics.btbr.webdriver.functions.elements.state;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.elasticsearch.common.Strings;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;

public class GetElementAttributeOrCssValue extends AbstractElementNamedProperty {
  
  public GetElementAttributeOrCssValue(APICoreProperties.Webdriver wdProps,
                            BuildCapability buildCapability,
                            RemoteWebDriver driver,
                            PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "getElementAttributeOrCssValue";
  }
  
  @Override
  protected String get(RemoteWebElement element, String propertyName) {
    String value = element.getAttribute(propertyName);
    if (Strings.isNullOrEmpty(value)) {
      value = element.getCssValue(propertyName);
    }
    return value;
  }
}