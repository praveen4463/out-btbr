package com.zylitics.btbr.webdriver.functions.elements.state;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;

public class IsElementSelected extends AbstractElementState {
  
  public IsElementSelected(APICoreProperties.Webdriver wdProps,
                                   BuildCapability buildCapability,
                                   RemoteWebDriver driver,
                                   PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "isElementSelected";
  }
  
  @Override
  protected boolean checkState(RemoteWebElement element) {
    return element.isSelected();
  }
}
