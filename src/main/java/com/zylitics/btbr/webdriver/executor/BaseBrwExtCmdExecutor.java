package com.zylitics.btbr.webdriver.executor;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.webdriver.BrwExtCmdResult;
import org.openqa.selenium.remote.RemoteWebDriver;

public abstract class BaseBrwExtCmdExecutor {
  
  final APICoreProperties.Webdriver wdProps;
  
  final RemoteWebDriver driver;
  
  public BaseBrwExtCmdExecutor(APICoreProperties.Webdriver wdProps,
                               RemoteWebDriver driver) {
    this.wdProps = wdProps;
    this.driver = driver;
  }
  
  BrwExtCmdResult buildErrorResult(String error) {
    return buildResult(0, false, error, false);
  }
  
  BrwExtCmdResult buildResult(long tookMillis, boolean success, String error,
                                      boolean doNotAbortBuild) {
    return new BrwExtCmdResult()
        .setTookMillis(tookMillis)
        .setSuccess(success)
        .setError(error)
        .setDoNotAbortBuild(doNotAbortBuild);
  }
}
