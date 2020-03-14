package com.zylitics.btbr.webdriver.functions.context.resize;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;

public class SetWinSize extends SetWinRect {
  
  public SetWinSize(APICoreProperties.Webdriver wdProps,
                    BuildCapability buildCapability,
                    RemoteWebDriver driver,
                    PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "setWinSize";
  }
  
  @Override
  protected void set(int a, int b) {
    window.setSize(new Dimension(a, b));
  }
}
