package com.zylitics.btbr.webdriver.functions.elements.retrieval;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ListZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class FindElementsWithSelectors extends AbstractWebdriverFunction {
  
  public FindElementsWithSelectors(APICoreProperties.Webdriver wdProps,
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
  public String getName() {
    return "findElementsWithSelectors";
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
  
    if (args.size() == 0) {
      throw unexpectedEndOfFunctionOverload(args.size());
    }
    return handleWDExceptions(() -> {
      for (int i = 0; i < args.size(); i++) {
        String selector = tryCastString(i, args.get(i));
        List<RemoteWebElement> elements = findElements(driver, selector, false);
        if (elements.size() == 0) {
          continue;
        }
        return convertIntoZwlElemIds(elements);
      }
      return new ListZwlValue(Collections.emptyList());
    });
  }
}
