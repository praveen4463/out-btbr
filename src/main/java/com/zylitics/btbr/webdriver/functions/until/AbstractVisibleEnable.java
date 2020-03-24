package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.zwl.datatype.StringZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

/**
 * Used in visible/invisible/enable/disable expectations. These should work is same manner because
 * here we want the element to become existent if not found, become non-stale and gone stale before
 * checking whether it's state matches the desired state. ALl of these require element to be on
 * page.
 */
public abstract class AbstractVisibleEnable extends AbstractUntilExpectation {
  
  public AbstractVisibleEnable(APICoreProperties.Webdriver wdProps,
                      BuildCapability buildCapability,
                      RemoteWebDriver driver,
                      PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream, 1, 1);
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    if (args.size() != 1) {
      throw unexpectedEndOfFunctionOverload(args.size());
    }
    String s = tryCastString(0, args.get(0));
    WebDriverWait wait = getWait(TimeoutType.ELEMENT_ACCESS);
    if (!isValidElemId(s)) {
      // when we're finding element, let's ignore stale exception and wait for element to appear.
      wait.ignoring(StaleElementReferenceException.class);
    }
    return handleWDExceptions(() ->
        new StringZwlValue(wait.until(d -> {
          // We will find element repeatedly until it's found, not stale, and in desiredState. When
          // argument is previously found elemId, repeatedly getting same element doesn't make sense
          // but to make code clean avoiding multiple conditions just to save a new instance
          // creation. Hopefully this is ok.
          // no wait as we're waiting here in this function.
          RemoteWebElement e = getElement(s, false);
          return desiredState(e) ? e.getId() : null;
        })));
  }
  
  abstract boolean desiredState(RemoteWebElement element);
}
