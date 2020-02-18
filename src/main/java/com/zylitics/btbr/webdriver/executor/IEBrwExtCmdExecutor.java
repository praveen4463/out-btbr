package com.zylitics.btbr.webdriver.executor;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.webdriver.Configuration;
import org.openqa.selenium.remote.RemoteWebDriver;

public class IEBrwExtCmdExecutor extends AbstractBrwExtCmdExecutor {
  
  public IEBrwExtCmdExecutor(APICoreProperties.Webdriver wdProps,
                                  RemoteWebDriver driver) {
    super(wdProps, driver);
  }
}
