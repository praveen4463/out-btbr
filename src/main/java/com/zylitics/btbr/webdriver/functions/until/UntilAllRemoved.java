package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.zwl.datatype.BooleanZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.PrintStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

public class UntilAllRemoved extends AbstractUntilExpectation {
  
  public UntilAllRemoved(APICoreProperties.Webdriver wdProps,
                      BuildCapability buildCapability,
                      RemoteWebDriver driver,
                      PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream, 1, Integer.MAX_VALUE);
  }
  
  @Override
  public String getName() {
    return "untilAllRemoved";
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    int argsCount = args.size();
    if (argsCount == 0) {
      throw unexpectedEndOfFunctionOverload(argsCount);
    }
    if (argsCount == 1) {
      String s = tryCastString(0, args.get(0));
      return withMultiSelector(s);
    }
    
    boolean result = false;
    for (int i = 0; i < argsCount; i++) {
      String s = tryCastString(i, args.get(i));
      WebDriverWait wait = getWait(TimeoutType.ELEMENT_ACCESS);
      
      result = handleWDExceptions(() ->
          wait.until(d -> {
            try {
              getElement(s, false);
              return false; // when successfully found, we need to find again.
            } catch (NoSuchElementException n) {
              return true;
            }
          }));
    }
    return new BooleanZwlValue(result);
  }
  
  // When a multi element selector is given, the wait time is equal to just single element access
  // timeout.
  private ZwlValue withMultiSelector(String s) {
    WebDriverWait wait = getWait(TimeoutType.ELEMENT_ACCESS);
    return handleWDExceptions(() ->
        new BooleanZwlValue(wait.until(d -> {
          List<RemoteWebElement> le = findElements(driver, s, false);
          return le.size() == 0; // when size is 0, all element are removed otherwise find again.
        })));
  }
}
