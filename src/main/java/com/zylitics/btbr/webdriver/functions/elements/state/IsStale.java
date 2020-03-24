package com.zylitics.btbr.webdriver.functions.elements.state;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.*;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public class IsStale extends AbstractWebdriverFunction {
  
  public IsStale(APICoreProperties.Webdriver wdProps,
                        BuildCapability buildCapability,
                        RemoteWebDriver driver,
                        PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "isStale";
  }
  
  @Override
  public int minParamsCount() {
    return 1;
  }
  
  @Override
  public int maxParamsCount() {
    return 1;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    if (args.size() == 0) {
      throw unexpectedEndOfFunctionOverload(args.size());
    }
    String elemId = tryCastString(0, args.get(0));
    return handleWDExceptions(() -> {
      boolean stale = false;
      try {
        getWebElementUsingElemId(elemId).isEnabled();
      } catch (StaleElementReferenceException s) {
        stale = true;
      }
      return new BooleanZwlValue(stale);
    });
  }
}
