package com.zylitics.btbr.runner;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.BrwExtCmdResult;
import com.zylitics.btbr.webdriver.BrwExtCmdDef;
import org.openqa.selenium.SessionNotCreatedException;

/**
 * Webdriver service that can be used to run browser tests on any (supported) browser. This is the
 * only medium through which build runner should interact with webdriver test.
 */
public interface GlobalWebdriverService {
  
  String getSessionKey();
  
  /**
   * Runs a browser extension command and returns it's result, this method is guaranteed to not
   * throw any exception and will translate all exceptions to meaningful error messages.
   * @param command a single brw ext command to run with webdriver
   * @return result of command run
   */
  BrwExtCmdResult runCommand(BrwExtCmdDef command);
  
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
