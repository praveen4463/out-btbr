package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;

public class UntilTotalElementsGT extends AbstractTotalElements {
  
  public UntilTotalElementsGT(APICoreProperties.Webdriver wdProps,
                           BuildCapability buildCapability,
                           RemoteWebDriver driver,
                           PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "untilTotalElementsGT";
  }
  
  @Override
  boolean desiredState(int totalElementsFound, int givenTotal) {
    return totalElementsFound > givenTotal;
  }
}
