package com.zylitics.btbr.webdriver.functions.elements.retrieval;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;

public class FindElementsFromElement extends AbstractFindFromElement {
  
  public FindElementsFromElement(APICoreProperties.Webdriver wdProps,
                                BuildCapability buildCapability,
                                RemoteWebDriver driver,
                                PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "findElementsFromElement";
  }
  
  @Override
  protected ZwlValue find(RemoteWebElement element, String using, ByType byType, boolean wait) {
    return convertIntoZwlElemIds(findElements(element, getBy(byType, using), wait));
  }
}
