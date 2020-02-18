package com.zylitics.btbr.webdriver.logs;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
TODO: Write docs about changes to be done in server script
 */
public class WebdriverLogs {
  
  private static final Logger LOG = LoggerFactory.getLogger(WebdriverLogs.class);
  
  private final RemoteWebDriver driver;
  
  private final APICoreProperties.Webdriver wdProps;
  
  private final BuildCapability buildCapability;
  
  public WebdriverLogs(RemoteWebDriver driver,
                       APICoreProperties.Webdriver wdProps,
                       BuildCapability buildCapability) {
    this.driver = driver;
    this.wdProps = wdProps;
    this.buildCapability = buildCapability;
  }
  
  /**
   * Does work in a separate thread and returns immediately, get buildId from context and assign
   * a name to thread with buildId.
   */
  public void collectStoreLogsAsync() {
    try {
      // collect and store all types of supported logs.
    } catch (Throwable t) {
      LOG.error(t.getMessage(), t);
    }
  }
}
