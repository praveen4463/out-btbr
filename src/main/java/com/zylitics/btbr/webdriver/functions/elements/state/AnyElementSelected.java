package com.zylitics.btbr.webdriver.functions.elements.state;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;
import java.util.List;

public class AnyElementSelected extends AbstractMultiElementState {
  
  public AnyElementSelected(APICoreProperties.Webdriver wdProps,
                                      BuildCapability buildCapability,
                                      RemoteWebDriver driver,
                                      PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "anyElementSelected";
  }
  
  @Override
  protected boolean stateCheck(List<RemoteWebElement> elements) {
    return elements.stream().anyMatch(RemoteWebElement::isSelected);
  }
}
