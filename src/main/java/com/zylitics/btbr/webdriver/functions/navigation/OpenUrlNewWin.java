package com.zylitics.btbr.webdriver.functions.navigation;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public class OpenUrlNewWin extends AbstractWebdriverFunction {
  
  public OpenUrlNewWin(APICoreProperties.Webdriver wdProps,
                 BuildCapability buildCapability,
                 RemoteWebDriver driver,
                 PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "openUrlNewWin";
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
      // control is returned after the switch
      targetLocator.newWindow(parseWindowType(tryCastString(0, args.get(0))));
      driver.get(tryCastString(1, args.get(1))); // we've switched, now open url
      return _void;
    });
  }
}
