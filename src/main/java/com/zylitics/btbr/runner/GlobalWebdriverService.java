package com.zylitics.btbr.runner;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.SessionNotCreatedException;

/**
 * Webdriver service that can be used to run browser tests on any (supported) browser. This is the
 * only medium through which build runner should interact with webdriver test.
 */
public interface GlobalWebdriverService {
  
  String getSessionKey();
  
  interface Factory {
  
    /**
     * Creates a new instance of GlobalWebdriverService, together with creating new webdriver
     * session.
     * @param wdProps webdriver specific properties
     * @param buildCapability capability of the running build
     * @return new instance of {@link GlobalWebdriverService}
     * @throws Exception if there were problems creating new session.
     */
    GlobalWebdriverService createAndStartNewSession(APICoreProperties.Webdriver wdProps,
        BuildCapability buildCapability) throws SessionNotCreatedException;
  }
}
