package com.zylitics.btbr.webdriver.functions;

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
  
  public String getName() {
    return name;
  }
  
  public String getAlias() {
    return alias;
  }
}
