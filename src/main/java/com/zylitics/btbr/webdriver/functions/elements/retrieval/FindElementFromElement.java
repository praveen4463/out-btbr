package com.zylitics.btbr.webdriver.functions.elements.retrieval;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;

public class FindElementFromElement extends AbstractFindFromElement {
  
  public FindElementFromElement(APICoreProperties.Webdriver wdProps,
                      BuildCapability buildCapability,
                      RemoteWebDriver driver,
                      PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "findElementFromElement";
  }
  
  @Override
  protected ZwlValue find(RemoteWebElement element, String using, ByType byType, boolean wait) {
    return convertIntoZwlElemId(findElement(element, getBy(byType, using), wait));
  }
}
