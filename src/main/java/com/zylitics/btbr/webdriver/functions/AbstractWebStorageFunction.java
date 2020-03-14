package com.zylitics.btbr.webdriver.functions;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.SessionStorage;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;

/**
 * Webdriver functions are instantiated on a new build request, runner checks which browser is
 * requested and gets specific driver. Using the driver we instantiate the functions. These
 * webstorage function will be instantiated for all browsers irrespective of whether they support
 * accessing webstorage. All functions should check whether Local and Session storage instances are
 * not null and go ahead only if that is true, when they are null, they should do nothing.
 */
public abstract class AbstractWebStorageFunction extends AbstractWebdriverFunction {
  
  protected final LocalStorage localStorage;
  
  protected final SessionStorage sessionStorage;
  
  protected AbstractWebStorageFunction(APICoreProperties.Webdriver wdProps,
                                      BuildCapability buildCapability,
                                      RemoteWebDriver driver,
                                      LocalStorage localStorage,
                                      SessionStorage sessionStorage,
                                      PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
    
    this.localStorage = localStorage;
    this.sessionStorage = sessionStorage;
  }
  
  // Use this for browsers that don't support webstorage access.
  protected AbstractWebStorageFunction(APICoreProperties.Webdriver wdProps,
                                       BuildCapability buildCapability,
                                       RemoteWebDriver driver,
                                       PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
    
    this.localStorage = null;
    this.sessionStorage = null;
  }
}
