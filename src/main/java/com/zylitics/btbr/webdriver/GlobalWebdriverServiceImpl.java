package com.zylitics.btbr.webdriver;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.runner.GlobalWebdriverService;
import com.zylitics.btbr.webdriver.executor.AbstractBrwExtCmdExecutor;
import com.zylitics.btbr.webdriver.logs.WebdriverLogs;
import com.zylitics.btbr.webdriver.session.AbstractDriverSessionProvider;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Optional;

public class GlobalWebdriverServiceImpl implements GlobalWebdriverService {
  
  private final APICoreProperties.Webdriver wdProps;
  
  private final BuildCapability buildCapability;
  
  private final Configuration configuration;
  
  private final RemoteWebDriver driver;
  
  private final AbstractBrwExtCmdExecutor brwExtExecutor;
  
  private final WebdriverLogs webdriverLogs;
  
  GlobalWebdriverServiceImpl(APICoreProperties.Webdriver wdProps,
      BuildCapability buildCapability) throws SessionNotCreatedException {
    this.wdProps = wdProps;
    this.buildCapability = buildCapability;
    configuration = new Configuration();
    driver = startSession();
    // after session is created, driver is ready to take commands, the availability wait is done
    // while it starts.
    brwExtExecutor = getBrwExtExecutor();
    webdriverLogs = new WebdriverLogs(driver, wdProps, buildCapability);
  }
  
  GlobalWebdriverServiceImpl(APICoreProperties.Webdriver wdProps,
                             BuildCapability buildCapability,
                             Configuration configuration,
                             RemoteWebDriver driver,
                             WebdriverLogs webdriverLogs,
                             AbstractBrwExtCmdExecutor brwExtExecutor) {
    this.wdProps = wdProps;
    this.buildCapability = buildCapability;
    this.configuration = configuration;
    this.driver = driver;
    this.webdriverLogs = webdriverLogs;
    this.brwExtExecutor = brwExtExecutor;
  }
  
  @Override
  public String getSessionKey() {
    return driver.getSessionId().toString();
  }
  
  @Override
  public BrwExtCmdResult runCommand(BrwExtCmdDef command) {
    BrwExtCmdResult result = brwExtExecutor.submitCmdDef(command);
    
    if (command.getIndex() % wdProps.getRetrieveLogsUponCmd() == 0) {
      webdriverLogs.collectStoreLogsAsync();
    }
    return result;
  }
  
  private RemoteWebDriver startSession() {
    Optional<AbstractDriverSessionProvider> driverSession =
        configuration.getSessionProviderByBrowser(wdProps, buildCapability)
            .apply(buildCapability.getWdBrowserName());
    if (!driverSession.isPresent()) {
      throw new SessionNotCreatedException("Couldn't find any associated session provider for" +
          " browser: " + buildCapability.getWdBrowserName());
    }
    return driverSession.get().createSession();
  }
  
  private AbstractBrwExtCmdExecutor getBrwExtExecutor() {
    Optional<AbstractBrwExtCmdExecutor> optionalExecutor =
        configuration.getBrwExtCmdExecutorByBrowser(wdProps, driver)
            .apply(buildCapability.getWdBrowserName());
    if (!optionalExecutor.isPresent()) {
      throw new RuntimeException("no command executor found");
    }
    return optionalExecutor.get();
  }
  
  public static class Factory implements GlobalWebdriverService.Factory {
  
    @Override
    public GlobalWebdriverService createAndStartNewSession(APICoreProperties.Webdriver wdProps,
        BuildCapability buildCapability) throws SessionNotCreatedException {
      return new GlobalWebdriverServiceImpl(wdProps, buildCapability);
    }
  }
}
