package com.zylitics.btbr.webdriver.functions.elements.retrieval;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import com.zylitics.zwl.exception.InvalidTypeException;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

abstract class AbstractFindElement extends AbstractWebdriverFunction {
  
  public AbstractFindElement(APICoreProperties.Webdriver wdProps,
                             BuildCapability buildCapability,
                             RemoteWebDriver driver,
                             PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public int minParamsCount() {
    return 1;
  }
  
  @Override
  public int maxParamsCount() {
    return 3;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    int argsCount = args.size();
    if (argsCount == 0) {
      throw unexpectedEndOfFunctionOverload(argsCount);
    }
  
    return handleWDExceptions(() -> {
      boolean noWait = false;
      ByType byType = null;
      switch (argsCount) {
        case 2:
          // argument 2 could be either By or noWait
          try {
            // Most of the times, when 2 args are given, 2nd should be a 'By', that's why first try
            // parse it, if it can't be parsed, it should be noWait.
            byType = parseEnum(1, args.get(1), ByType.class);
          } catch (InvalidTypeException ignore) { }
          // When argument is not a string value or threw exception parsing to a 'By', parse it as
          // boolean.
          if (byType == null) {
            byType = ByType.CSS_SELECTOR; // our default By is a css selector.
            noWait = parseBoolean(1, args.get(1));
          }
          break;
        case 3:
          byType = parseEnum(1, args.get(1), ByType.class);
          noWait = parseBoolean(2, args.get(2));
          break;
        default:
          byType = ByType.CSS_SELECTOR;  // our default By is a css selector.
      }
      return find(tryCastString(0, args.get(0)), byType, !noWait);
    });
  }
  
  protected abstract ZwlValue find(String using, ByType byType, boolean wait);
}
