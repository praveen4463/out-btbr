package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.io.PrintStream;
import java.util.regex.Pattern;

public class UntilUrlLike extends AbstractTitleUrl {
  
  public UntilUrlLike(APICoreProperties.Webdriver wdProps,
                          BuildCapability buildCapability,
                          RemoteWebDriver driver,
                          PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "untilUrlLike";
  }
  
  @Override
  ExpectedCondition<Boolean> condition(String s) {
    return d -> {
      Pattern p = getPattern(s);
      return p.matcher(driver.getCurrentUrl()).find();
    };
  }
}
