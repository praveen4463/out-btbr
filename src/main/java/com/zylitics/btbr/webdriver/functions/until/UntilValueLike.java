package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
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
    Pattern p = getPattern(textOrValue);
    return p.matcher(element.getAttribute("value")).find();
  }
}