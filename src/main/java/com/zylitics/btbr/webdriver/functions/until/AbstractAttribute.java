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

public abstract class AbstractAttribute extends AbstractUntilExpectation {
  
  public AbstractAttribute(APICoreProperties.Webdriver wdProps,
                           BuildCapability buildCapability,
                           RemoteWebDriver driver,
                           PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream, 3, 3);
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    if (args.size() != 3) {
      throw unexpectedEndOfFunctionOverload(args.size());
    }
    String elemOrSelector = tryCastString(0, args.get(0));
    String attribute = tryCastString(1, args.get(1));
    String value = tryCastString(2, args.get(2));
    WebDriverWait wait = getWait(TimeoutType.ELEMENT_ACCESS);
    if (!isValidElemId(elemOrSelector)) {
      // ignore stale exception default so that even if element goes stale intermittent we can
      // locate it and match text/value.
      wait.ignoring(StaleElementReferenceException.class);
    }
    return handleWDExceptions(() ->
        new BooleanZwlValue(wait.until(d -> {
          RemoteWebElement e = getElement(elemOrSelector, false);
          return desiredState(e, attribute, value);
        })));
  }
  
  abstract boolean desiredState(RemoteWebElement element, String attribute, String value);
}
