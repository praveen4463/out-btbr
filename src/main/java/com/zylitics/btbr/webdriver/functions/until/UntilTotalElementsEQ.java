package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;

public class UntilTotalElementsEQ extends AbstractTotalElements {
  
  public UntilTotalElementsEQ(APICoreProperties.Webdriver wdProps,
                              BuildCapability buildCapability,
                              RemoteWebDriver driver,
                              PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "untilTotalElementsEQ";
  }
  
  @Override
  boolean desiredState(int totalElementsFound, int givenTotal) {
    return totalElementsFound == givenTotal;
  }
}