package com.zylitics.btbr.webdriver.functions.elements.state;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;
import java.util.List;

public class AllElementsDisplayed extends AbstractMultiElementState {
  
  public AllElementsDisplayed(APICoreProperties.Webdriver wdProps,
                             BuildCapability buildCapability,
                             RemoteWebDriver driver,
                             PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "allElementsDisplayed";
  }
  
  @Override
  protected boolean stateCheck(List<RemoteWebElement> elements) {
    return elements.stream().allMatch(RemoteWebElement::isDisplayed);
  }
}
