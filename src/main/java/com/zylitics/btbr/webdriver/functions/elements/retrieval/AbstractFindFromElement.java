package com.zylitics.btbr.webdriver.functions.elements.retrieval;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractFindFromElement extends AbstractWebdriverFunction {
  
  public AbstractFindFromElement(APICoreProperties.Webdriver wdProps,
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
    return 3;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    writeCommandUpdate(withArgsCommandUpdateText(args));
    int argsCount = args.size();
    
    if (argsCount >= 2 && argsCount <= 3) {
      boolean noWait = argsCount == 3 ? parseBoolean(2, args.get(2)) : false;
      return handleWDExceptions(() -> find(
          getElement(tryCastString(0, args.get(0)), !noWait),
          tryCastString(1, args.get(1)), !noWait));
    }
    
    throw unexpectedEndOfFunctionOverload(argsCount);
  }
  
  protected abstract ZwlValue find(RemoteWebElement element, String selector, boolean wait);
}
