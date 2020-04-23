package com.zylitics.btbr.webdriver.constants;

import org.openqa.selenium.devtools.browser.Browser;
import org.openqa.selenium.remote.BrowserType;

public enum Browsers {
  
  CHROME  (BrowserType.CHROME, BrowserType.CHROME),
  FIREFOX (BrowserType.FIREFOX, BrowserType.FIREFOX),
  IE (BrowserType.IE, "IE");
  
  private final String name;
  private final String alias;
  
  Browsers(String name, String alias) {
    this.name = name;
    this.alias = alias;
  }
  
  public static Browsers valueByName(String name) {
    for (Browsers browser : Browsers.values()) {
      if (browser.getName().equalsIgnoreCase(name)) {
        return browser;
      }
    }
    throw new IllegalArgumentException("No instance of Browsers matched the given name " + name);
  }
  
  public String getName() {
    return name;
  }
  
  public String getAlias() {
    return alias;
  }
}
