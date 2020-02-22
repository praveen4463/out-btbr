package com.zylitics.btbr.webdriver.functions;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.zwl.function.AbstractFunction;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractWebdriverFunction extends AbstractFunction {
  
  private static final Logger LOG = LoggerFactory.getLogger(AbstractWebdriverFunction.class);
  
  final APICoreProperties.Webdriver wdProps;
  
  final BuildCapability buildCapability;
  
  final RemoteWebDriver driver;
  
  final WebDriver.Window window;
  
  final WebDriver.Options options;
  
  // Accepts everything needed to run the webdriver function.
  AbstractWebdriverFunction(APICoreProperties.Webdriver wdProps,
                            BuildCapability buildCapability,
                            RemoteWebDriver driver) {
    this.wdProps = wdProps;
    this.buildCapability = buildCapability;
    this.driver = driver;
    options = driver.manage();
    window = options.window();
  }
}
