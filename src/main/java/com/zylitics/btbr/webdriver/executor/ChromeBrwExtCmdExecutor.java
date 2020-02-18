package com.zylitics.btbr.webdriver.executor;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.webdriver.Configuration;
import org.openqa.selenium.remote.RemoteWebDriver;

public class ChromeBrwExtCmdExecutor extends AbstractBrwExtCmdExecutor {
  
  public ChromeBrwExtCmdExecutor(APICoreProperties.Webdriver wdProps,
                                 RemoteWebDriver driver) {
    super(wdProps, driver);
  }
}
