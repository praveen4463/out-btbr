package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.zwl.datatype.BooleanZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

abstract class AbstractTotalElements extends AbstractUntilExpectation {
  
  public AbstractTotalElements(APICoreProperties.Webdriver wdProps,
                           BuildCapability buildCapability,
                           RemoteWebDriver driver,
                           PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream, 2, 2);
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    if (args.size() != 2) {
      throw unexpectedEndOfFunctionOverload(args.size());
    }
    String selector = tryCastString(0, args.get(0));
    int total = parseDouble(1, args.get(1)).intValue();
    WebDriverWait wait = getWait(TimeoutType.ELEMENT_ACCESS);
    return handleWDExceptions(() ->
        wait.until(d -> {
          List<RemoteWebElement> e = findElements(driver, selector, false);
          if (e.size() == 0) {
            return null;
          }
          return desiredState(e.size(), total) ? convertIntoZwlElemIds(e) : null;
        }));
  }
  
  abstract boolean desiredState(int totalElementsFound, int givenTotal);
}
