package com.zylitics.btbr.webdriver.functions.elements.retrieval;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.NothingZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractFindWithSelectors extends AbstractWebdriverFunction {
  
  public AbstractFindWithSelectors(APICoreProperties.Webdriver wdProps,
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
    return Integer.MAX_VALUE;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    return handleWDExceptions(() -> {
      if (args.size() == 0) {
        throw unexpectedEndOfFunctionOverload(args.size());
      }
      RuntimeException lastException = null;
      for (int i = 0; i < args.size(); i++) {
        String selector = tryCastString(i, args.get(i));
        try {
          return find(selector, false);
        } catch (NoSuchElementException ne) {
          lastException = ne;
        }
      }
      throw lastException;
    });
  }
  
  protected abstract ZwlValue find(String selector, boolean wait);
}
