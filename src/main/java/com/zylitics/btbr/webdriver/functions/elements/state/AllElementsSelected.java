package com.zylitics.btbr.webdriver.functions.elements.state;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;
import java.util.List;

public class AllElementsSelected extends AbstractMultiElementState {
  
  public AllElementsSelected(APICoreProperties.Webdriver wdProps,
                           BuildCapability buildCapability,
                           RemoteWebDriver driver,
                           PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "allElementsSelected";
  }
  
  @Override
  protected boolean stateCheck(List<RemoteWebElement> elements) {
    return elements.stream().allMatch(RemoteWebElement::isSelected);
  }
}
