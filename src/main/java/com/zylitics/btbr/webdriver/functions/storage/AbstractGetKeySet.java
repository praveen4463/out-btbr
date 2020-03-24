package com.zylitics.btbr.webdriver.functions.storage;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.zwl.datatype.NothingZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public abstract class AbstractGetKeySet extends AbstractStorage {
  
  public AbstractGetKeySet(APICoreProperties.Webdriver wdProps,
                           BuildCapability buildCapability,
                           RemoteWebDriver driver,
                           PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public int minParamsCount() {
    return 0;
  }
  
  @Override
  public int maxParamsCount() {
    return 0;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    if (localStorage == null || sessionStorage == null) {
      printNoStorageMsg();
      return new NothingZwlValue();
    }
    
    Set<String> keySet = handleWDExceptions(this::get);
    if (keySet == null) {
      return new NothingZwlValue();
    }
    return tryGetStringZwlValues(keySet);
  }
  
  protected abstract Set<String> get();
}
