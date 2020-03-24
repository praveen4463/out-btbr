package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.PrintStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

public class UntilAnySelectionIs extends AbstractUntilExpectation {
  
  public UntilAnySelectionIs(APICoreProperties.Webdriver wdProps,
                                  BuildCapability buildCapability,
                                  RemoteWebDriver driver,
                                  PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream, 2, Integer.MAX_VALUE);
  }
  
  @Override
  public String getName() {
    return "untilAnySelectionIs";
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
  
    int argsCount = args.size();
    if (argsCount < 2) {
      throw unexpectedEndOfFunctionOverload(argsCount);
    }
    Boolean selectionToBe = parseBoolean(0, args.get(0));
    if (argsCount == 2) {
      String s = tryCastString(1, args.get(1));
      if (!isValidElemId(s)) {
        // we've got multi element selector.
        return withMultiSelector(s, selectionToBe);
      }
    }
  
    // finding any selected when a list of selectors/elemIds are given is a little tricky. Since we
    // need to find any selection, we shouldn't wait locating one of the element if that is not
    // immediately available because there is a chance some other was already available, thus we
    // will continue to next selectors/elemIds rather than waiting to locate.
    // If an element was available (whether elemId or selector), try to see if it's selection state
    // matches desired state, if yes, we will return from the loop and return the found element,
    // if not we will continue to next selectors/elemIds, if accessing one of located element
    // raised a stale exception we will just move over and won't bail out entirely cause one
    // element's stale doesn't mean all have gone stale. Knowing that if all the given elements
    // were previously located elements and all of them throws stale exception, we can't recover
    // from it and this function will end up waiting until timeout in that case but still catching
    // stale exception does more good than bad like having possibility of few element's staleness.
    // When loop end and no match was found a null is returned so that 'wait' will continue.
    WebDriverWait wait = getWait(TimeoutType.ELEMENT_ACCESS);
    return handleWDExceptions(() -> wait.until(d -> {
      for (int i = 1; i < argsCount; i++) {
        String s = tryCastString(i, args.get(i));
        RemoteWebElement e;
        try {
          e = getElement(s, false);
        } catch (NoSuchElementException ne) {
          continue;
        }
        try {
          if (e.isSelected() == selectionToBe) {
            return convertIntoZwlElemId(e);
          }
        } catch (StaleElementReferenceException ignore) { }
      }
      return null;
    }));
  }
  
  private ZwlValue withMultiSelector(String s, Boolean selectionToBe) {
    // when the child elements during desired state match become stale, ignore it so that the
    // parent element can be located again when it recovers form being stale.
    WebDriverWait wait = getWait(TimeoutType.ELEMENT_ACCESS);
    wait.ignoring(StaleElementReferenceException.class);
    return handleWDExceptions(() ->
        wait.until(d -> {
          List<RemoteWebElement> le = findElements(driver, s, false);
          if (le.size() == 0) {
            return null;
          }
          Optional<RemoteWebElement> matched = le.stream()
              .filter(e -> e.isSelected() == selectionToBe).findAny(); // will not evaluate all
          // elements in stream after the filter returns one.
          return matched.map(this::convertIntoZwlElemId).orElse(null);
        }));
  }
}
