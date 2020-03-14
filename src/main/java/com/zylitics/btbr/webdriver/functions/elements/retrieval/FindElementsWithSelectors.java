package com.zylitics.btbr.webdriver.functions.elements.retrieval;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;

public class FindElementsWithSelectors extends AbstractFindWithSelectors {
  
  public FindElementsWithSelectors(APICoreProperties.Webdriver wdProps,
                                  BuildCapability buildCapability,
                                  RemoteWebDriver driver,
                                  PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "findElementsWithSelectors";
  }
  
  @Override
  protected ZwlValue find(String selector, boolean wait) {
    return convertIntoZwlElemIds(findElements(driver, selector, wait));
  }
}
