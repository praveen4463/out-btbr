package com.zylitics.btbr.webdriver.functions.select;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;

import java.io.PrintStream;
import java.util.List;

public class GetAllSelectedOptions extends AbstractGetOptions {
  
  public GetAllSelectedOptions(APICoreProperties.Webdriver wdProps,
                    BuildCapability buildCapability,
                    RemoteWebDriver driver,
                    PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "getAllSelectedOptions";
  }
  
  @Override
  List<WebElement> getOptions(Select select) {
    return select.getAllSelectedOptions();
  }
}
