package com.zylitics.btbr.webdriver.functions.document;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.Arrays;

public class ExecuteScript extends AbstractExecuteScript {
  
  public ExecuteScript(APICoreProperties.Webdriver wdProps,
                               BuildCapability buildCapability,
                               RemoteWebDriver driver,
                               PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "executeScript";
  }
  
  @Override
  protected Object execute(String script, Object... args) {
    System.out.println("script is " + script);
    System.out.println("args is " + Arrays.toString(args));
    return driver.executeScript(script, args);
  }
}
