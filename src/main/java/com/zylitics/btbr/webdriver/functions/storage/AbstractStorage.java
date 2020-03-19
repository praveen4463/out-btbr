package com.zylitics.btbr.webdriver.functions.storage;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.SessionStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractStorage extends AbstractWebdriverFunction {
  
  public AbstractStorage(APICoreProperties.Webdriver wdProps,
                       BuildCapability buildCapability,
                       RemoteWebDriver driver,
                       PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  LocalStorage localStorage;
  
  SessionStorage sessionStorage;
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    if (driver instanceof WebStorage) {
      WebStorage storage = (WebStorage) driver;
      localStorage = storage.getLocalStorage();
      sessionStorage = storage.getSessionStorage();
    }
    return _void;
  }
  
  void printNoStorageMsg() {
    writeCommandUpdate(String.format("WARNING: %s doesn't support local/session storage," +
        "  function %s will not return/do anything.", buildCapability.getWdBrowserName(),
        getName()));
  }
}
