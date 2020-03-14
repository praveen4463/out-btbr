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
    
    writeCommandUpdate(withArgsCommandUpdateText(args));
    int argsCount = args.size();
    
    if (argsCount == 2) {
      return execute(tryCastString(0, args.get(0)), tryCastString(1, args.get(1)));
    }
    
    throw unexpectedEndOfFunctionOverload(argsCount);
  }
  
  private ZwlValue execute(String winType, String url) {
    return handleWDExceptions(() -> {
      targetLocator.newWindow(parseWindowType(winType)); // control is returned after the switch
      driver.get(url); // we've switched, now open url
      return _void;
    });
  }
}
