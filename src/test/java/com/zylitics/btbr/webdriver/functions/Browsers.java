package com.zylitics.btbr.webdriver.functions;

import org.openqa.selenium.remote.BrowserType;

public enum Browsers {
  
  CHROME  (BrowserType.CHROME),
  FIREFOX (BrowserType.FIREFOX),
  IE (BrowserType.IE);
  
  private final String name;
  
  Browsers(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
}
