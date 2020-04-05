package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.elasticsearch.common.Strings;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.PrintStream;

public class UntilAttributeValueContains extends AbstractAttribute {
  
  public UntilAttributeValueContains(APICoreProperties.Webdriver wdProps,
                               BuildCapability buildCapability,
                               RemoteWebDriver driver,
                               PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "untilAttributeValueContains";
  }
  
  @Override
  boolean desiredState(RemoteWebElement element, String attribute, String value) {
    Boolean res = ExpectedConditions.attributeContains(element, attribute, value).apply(driver);
    return res == null ? false : res;
  }
}