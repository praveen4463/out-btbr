package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.util.CollectionUtil;
import com.zylitics.zwl.datatype.ListZwlValue;
import com.zylitics.zwl.datatype.StringZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Multi element variant of {@link AbstractVisibleEnable}
 */
public abstract class AbstractVisibleEnableMulti extends AbstractUntilExpectation {
  
  public AbstractVisibleEnableMulti(APICoreProperties.Webdriver wdProps,
                                    BuildCapability buildCapability,
                                    RemoteWebDriver driver,
                                    PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream, 1, Integer.MAX_VALUE);
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
      if (!isValidElemId(s)) {
        // we've got multi element selector.
        return withMultiSelector(s);
      }
    }
    // when multiple elemIds/selectors are given, separate wait is used for everyone so that the
    // element access timeout doesn't exhaust when working with many elemIds/selectors.
    List<ZwlValue> elemIds = new ArrayList<>(CollectionUtil.getInitialCapacity(argsCount));
    for (int i = 0; i < argsCount; i++) {
      String s = tryCastString(i, args.get(i));
      WebDriverWait wait = getWait(TimeoutType.ELEMENT_ACCESS);
      if (!isValidElemId(s)) {
        wait.ignoring(StaleElementReferenceException.class);
      }
      elemIds.add(handleWDExceptions(() ->
          new StringZwlValue(wait.until(d -> {
            RemoteWebElement e = getElement(s, false);
            return desiredState(e) ? e.getId() : null;
          }))));
    }
    return new ListZwlValue(elemIds);
  }
  
  // When a multi element selector is given, the wait time is equal to just single element access
  // timeout.
  private ZwlValue withMultiSelector(String s) {
    WebDriverWait wait = getWait(TimeoutType.ELEMENT_ACCESS);
    wait.ignoring(StaleElementReferenceException.class);
    // when the child elements during desired state match become stale, ignore it so that the
    // parent element can be located again when it recovers form being stale.
    return handleWDExceptions(() ->
        wait.until(d -> {
          List<RemoteWebElement> le = findElements(driver, s, false);
          if (le.size() == 0) {
            return null;
          }
          return le.stream().allMatch(this::desiredState) ? convertIntoZwlElemIds(le) : null;
        }));
  }
  
  abstract boolean desiredState(RemoteWebElement element);
}
