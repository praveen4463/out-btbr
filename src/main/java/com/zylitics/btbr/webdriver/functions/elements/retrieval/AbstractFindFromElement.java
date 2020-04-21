package com.zylitics.btbr.webdriver.functions.elements.retrieval;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import com.zylitics.zwl.exception.InvalidTypeException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

abstract class AbstractFindFromElement extends AbstractWebdriverFunction {
  
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
    return 4;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    int argsCount = args.size();
    
    if (argsCount < 2) {
      throw unexpectedEndOfFunctionOverload(argsCount);
    }
    return handleWDExceptions(() -> {
      boolean noWait = false;
      ByType byType = null;
      switch (argsCount) {
        case 3:
          // argument 3 could be either By or noWait
          try {
            // Most of the times, when 3 args are given, 3rd should be a 'By', that's why first try
            // parse it, if it can't be parsed, it should be noWait.
            byType = parseEnum(2, args.get(2), ByType.class);
          } catch (InvalidTypeException ignore) { }
          // When argument is not a string value or threw exception parsing to a 'By', parse it as
          // boolean.
          if (byType == null) {
            byType = ByType.CSS_SELECTOR; // our default By is a css selector.
            noWait = parseBoolean(2, args.get(2));
          }
          break;
        case 4:
          byType = parseEnum(2, args.get(2), ByType.class);
          noWait = parseBoolean(3, args.get(3));
          break;
        default:
          byType = ByType.CSS_SELECTOR;  // our default By is a css selector.
      }
      return find(getElement(tryCastString(0, args.get(0)), !noWait),
          tryCastString(1, args.get(1)),
          byType,
          !noWait);
    });
  }
  
  protected abstract ZwlValue find(RemoteWebElement element, String using, ByType byType,
                                   boolean wait);
}
