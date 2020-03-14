package com.zylitics.btbr.webdriver.functions.context.resize;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import org.openqa.selenium.Point;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;

public class SetWinPosition extends SetWinRect {
  
  public SetWinPosition(APICoreProperties.Webdriver wdProps,
                    BuildCapability buildCapability,
                    RemoteWebDriver driver,
                    PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "setWinPosition";
  }
  
  @Override
  protected void set(int a, int b) {
    window.setPosition(new Point(a, b));
  }
}
