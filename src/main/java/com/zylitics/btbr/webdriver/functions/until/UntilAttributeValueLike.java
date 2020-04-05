package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.elasticsearch.common.Strings;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;
import java.util.Optional;
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
    String attributeValue = element.getAttribute(attribute);
    if (Strings.isNullOrEmpty(attributeValue)) {
      attributeValue = element.getCssValue(attribute);
    }
    return !Strings.isNullOrEmpty(attributeValue)
        && getPattern(value).matcher(attributeValue).find();
  }
}