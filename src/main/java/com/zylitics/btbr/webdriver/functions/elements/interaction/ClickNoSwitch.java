package com.zylitics.btbr.webdriver.functions.elements.interaction;

import com.google.common.collect.Sets;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.TimeoutType;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.NothingZwlValue;
import com.zylitics.zwl.datatype.StringZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.PrintStream;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ClickNoSwitch extends AbstractWebdriverFunction {
  
  public ClickNoSwitch(APICoreProperties.Webdriver wdProps,
                        BuildCapability buildCapability,
                        RemoteWebDriver driver,
                        PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "clickNoSwitch";
  }
  
  @Override
  public int minParamsCount() {
    return 1;
  }
  
  @Override
  public int maxParamsCount() {
    return 1;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    if (args.size() != 1) {
      throw unexpectedEndOfFunctionOverload(args.size());
    }
    return handleWDExceptions(() -> execute(getElement(tryCastString(0, args.get(0)))));
  }
  
  private ZwlValue execute(RemoteWebElement element) {
    Set<String> previousHandles = driver.getWindowHandles();
    String previousHandle = driver.getWindowHandle();
  
    element.click();
  
    int desiredHandles = previousHandles.size() + 1;
    WebDriverWait wait =
        getWait(TimeoutType.NEW_WINDOW, "waiting for total windows to be " + desiredHandles);
    try {
      Set<String> currentHandles = wait.until(d -> {
        Set<String> h = driver.getWindowHandles();
        if (h.size() == desiredHandles) {
          return h;
        }
        return null;
      });
      // new window opened, switch the browser focus to previous window. Although webdriver is still
      // seeing the previous window but user agent may be switched already to new window.
      targetLocator.window(previousHandle);
      
      Set<String> newHandles = Sets.symmetricDifference(currentHandles, previousHandles);
      // newHandles can't be 0. Just send back the first handle in set even if there are more
      // (probably the click opened more than one window)
      return new StringZwlValue(newHandles.iterator().next());
    } catch (TimeoutException ignore) {
      // a timeout means no other window was opened, thus we've nothing to do here, don't show a
      // warning, just don't do anything.
    }
    return new NothingZwlValue();
  }
}
