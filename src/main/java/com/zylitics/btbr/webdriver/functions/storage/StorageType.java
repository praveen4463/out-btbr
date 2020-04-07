package com.zylitics.btbr.webdriver.functions.storage;

public enum StorageType {
  
  LOCAL ("localStorage"),
  SESSION ("sessionStorage");
  
  private final String name;
  
  StorageType(String name) { this.name = name; }
  
  public String getName() {
    return name;
  }
}