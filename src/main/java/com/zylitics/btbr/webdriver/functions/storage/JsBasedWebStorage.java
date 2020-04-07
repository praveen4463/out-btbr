package com.zylitics.btbr.webdriver.functions.storage;

import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.SessionStorage;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class JsBasedWebStorage implements LocalStorage, SessionStorage {
  
  private final String storageType;
  
  private final RemoteWebDriver driver;
  
  public JsBasedWebStorage(StorageType storageType, RemoteWebDriver driver) {
    this.storageType = storageType.getName();
    this.driver = driver;
  }
  
  @Override
  public String getItem(String key) {
    String script = String.format("return %s.getItem(\"%s\");", storageType, key);
    return (String) driver.executeScript(script);
  }
  
  @Override
  public Set<String> keySet() {
    String script = String.format("return Object.keys(%s);", storageType);
    @SuppressWarnings("unchecked")
    Collection<String> result = (Collection<String>) driver.executeScript(script);
    if (result == null) {
      return null;
    }
    return new HashSet<>(result);
  }
  
  @Override
  public void setItem(String key, String value) {
    String script = String.format("%s.setItem(\"%s\", \"%s\");", storageType, key, value);
    driver.executeScript(script);
  }
  
  @Override
  public String removeItem(String key) {
    String value = getItem(key);
    String script = String.format("%s.removeItem(\"%s\");", storageType, key);
    driver.executeScript(script);
    return value;
  }
  
  @Override
  public void clear() {
    String script = String.format("%s.clear();", storageType);
    driver.executeScript(script);
  }
  
  @Override
  public int size() {
    String script = String.format("return %s.length;", storageType);
    return Integer.parseInt(driver.executeScript(script).toString());
  }
}
