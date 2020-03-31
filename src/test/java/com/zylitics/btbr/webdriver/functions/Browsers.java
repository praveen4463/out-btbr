package com.zylitics.btbr.webdriver.functions;

import org.openqa.selenium.Keys;

public enum Browsers {
  
  CHROME  ("chrome"),
  FIREFOX ("firefox");
  
  private final String name;
  
  Browsers(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
}
