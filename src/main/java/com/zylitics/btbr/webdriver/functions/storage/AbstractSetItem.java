package com.zylitics.btbr.webdriver.functions.storage;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.zwl.datatype.NothingZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

abstract class AbstractSetItem extends AbstractStorage {
  
  public AbstractSetItem(APICoreProperties.Webdriver wdProps,
                           BuildCapability buildCapability,
                           RemoteWebDriver driver,
                           PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public int minParamsCount() {
    return 2;
  }
  
  @Override
  public int maxParamsCount() {
    return 2;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    if (args.size() < 2) {
      throw unexpectedEndOfFunctionOverload(args.size());
    }
    return handleWDExceptions(() -> {
      set(tryCastString(0, args.get(0)), tryCastString(1, args.get(1)));
      return _void;
    });
  }
  
  protected abstract void set(String key, String value);
}
