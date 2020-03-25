package com.zylitics.btbr.webdriver.functions.elements.interaction;

import com.google.common.collect.Sets;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
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

public class ClickSwitchNew extends AbstractWebdriverFunction {
  
  public ClickSwitchNew(APICoreProperties.Webdriver wdProps,
                    BuildCapability buildCapability,
                    RemoteWebDriver driver,
                    PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "clickSwitchNew";
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
    return handleWDExceptions(() -> {
      execute(getElement(tryCastString(0, args.get(0))));
      return _void;
    });
  }
  
  private void execute(RemoteWebElement element) {
    Set<String> previousHandles = driver.getWindowHandles();
    String previousHandle = driver.getWindowHandle();
    
    element.click();
    
    WebDriverWait wait =
        new WebDriverWait(driver, Duration.ofMillis(wdProps.getDefaultTimeoutNewWindow()));
    int desiredHandles = previousHandles.size() + 1;
    try {
      Set<String> currentHandles = wait.until(d -> {
        Set<String> h = driver.getWindowHandles();
        if (h.size() == desiredHandles) {
          return h;
        }
        return null;
      });
      Set<String> newHandles = Sets.symmetricDifference(currentHandles, previousHandles);
      // newHandles can't be 0.
      if (newHandles.size() > 1) {
        targetLocator.window(previousHandle);
        writeBuildOutput("WARNING: " + getName() + " couldn't switch to newly opened window" +
            " because there seems to have more than one new windows. I've switched browser" +
            " focus back to the window where you clicked.");
        return;
      }
      targetLocator.window(newHandles.iterator().next());
    } catch (TimeoutException ignore) {
      // even if a timeout occurs, we don't throw anything, assuming that the particular click
      // didn't open a new window and probably user has mistakenly invoked this function for an
      // element that doesn't open new window.
      // give a warning to user that no new window seemed to have opened.
      writeBuildOutput("WARNING: " + getName() + " couldn't find a newly opened window, and" +
          " didn't switch over. If you think it's a bug, please report us.");
    }
  }
}
