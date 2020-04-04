package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.util.CollectionUtil;
import com.zylitics.btbr.webdriver.TimeoutType;
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

public class UntilAllSelectionsAre extends AbstractUntilExpectation {
  
  public UntilAllSelectionsAre(APICoreProperties.Webdriver wdProps,
                             BuildCapability buildCapability,
                             RemoteWebDriver driver,
                             PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream, 2, Integer.MAX_VALUE);
  }
  
  @Override
  public String getName() {
    return "untilAllSelectionsAre";
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
  
    List<ZwlValue> elemIds = new ArrayList<>(CollectionUtil.getInitialCapacity(argsCount));
    for (int i = 1; i < argsCount; i++) {
      String s = tryCastString(i, args.get(i));
      WebDriverWait wait = getWait(TimeoutType.ELEMENT_ACCESS);
      if (!isValidElemId(s)) {
        wait.ignoring(StaleElementReferenceException.class);
      }
      elemIds.add(handleWDExceptions(() ->
          new StringZwlValue(wait.until(d -> {
            RemoteWebElement e = getElement(s, false);
            return e.isSelected() == selectionToBe ? e.getId() : null;
          }))));
    }
    return new ListZwlValue(elemIds);
  }
  
  private ZwlValue withMultiSelector(String s, Boolean selectionToBe) {
    WebDriverWait wait = getWait(TimeoutType.ELEMENT_ACCESS);
    wait.ignoring(StaleElementReferenceException.class);
    return handleWDExceptions(() ->
        wait.until(d -> {
          List<RemoteWebElement> le = findElements(driver, s, false);
          if (le.size() == 0) {
            return null;
          }
          return le.stream().allMatch(e -> e.isSelected() == selectionToBe)
              ? convertIntoZwlElemIds(le) : null;
        }));
  }
}