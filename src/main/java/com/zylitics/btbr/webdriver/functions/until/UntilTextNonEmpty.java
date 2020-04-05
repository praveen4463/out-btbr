package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.elasticsearch.common.Strings;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;

public class UntilTextNonEmpty extends AbstractTextValueNonEmpty {
  
  public UntilTextNonEmpty(APICoreProperties.Webdriver wdProps,
                     BuildCapability buildCapability,
                     RemoteWebDriver driver,
                     PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "untilTextNonEmpty";
  }
  
  @Override
  boolean desiredState(RemoteWebElement element) {
    String value = element.getText();
    return !Strings.isNullOrEmpty(value) && value.trim().length() > 0;
  }
}