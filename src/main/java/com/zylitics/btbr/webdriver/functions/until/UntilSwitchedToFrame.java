package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.zwl.datatype.BooleanZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import com.zylitics.zwl.exception.InvalidTypeException;
import com.zylitics.zwl.util.ParseUtil;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public class UntilSwitchedToFrame extends AbstractUntilExpectation {
  
  public UntilSwitchedToFrame(APICoreProperties.Webdriver wdProps,
                           BuildCapability buildCapability,
                           RemoteWebDriver driver,
                           PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream, 1, 1);
  }
  
  @Override
  public String getName() {
    return "untilSwitchedToFrame";
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    if (args.size() != 1) {
      throw unexpectedEndOfFunctionOverload(args.size());
    }
    WebDriverWait wait = getWait(TimeoutType.ELEMENT_ACCESS);
    wait.ignoring(NoSuchFrameException.class);
    
    try {
      int index = ParseUtil.parseDouble(args.get(0), new InvalidTypeException("")).intValue();
      return processWithIndex(wait, index);
    } catch (InvalidTypeException ignore) {}
    
    return processWithSelector(wait, tryCastString(0, args.get(0)));
  }
  
  private ZwlValue processWithIndex(WebDriverWait wait, int index) {
    return handleWDExceptions(() ->
        new BooleanZwlValue(wait.until(d -> {
          targetLocator.frame(index);
          return true;
        })));
  }
  
  private ZwlValue processWithSelector(WebDriverWait wait, String selector) {
    return handleWDExceptions(() ->
        new BooleanZwlValue(wait.until(d -> {
          WebElement e = findElement(driver, selector, false);
          targetLocator.frame(e);
          return true;
        })));
  }
}
