package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.elasticsearch.common.Strings;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;
import java.util.regex.Pattern;

public class UntilValueLike extends AbstractTextValue {
  
  public UntilValueLike(APICoreProperties.Webdriver wdProps,
                      BuildCapability buildCapability,
                      RemoteWebDriver driver,
                      PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "untilValueLike";
  }
  
  @Override
  boolean desiredState(RemoteWebElement element, String textOrValue) {
    String value = element.getAttribute("value");
    return !Strings.isNullOrEmpty(value) && getPattern(textOrValue).matcher(value).find();
  }
}