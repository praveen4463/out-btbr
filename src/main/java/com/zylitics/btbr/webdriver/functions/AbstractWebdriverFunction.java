package com.zylitics.btbr.webdriver.functions;

import com.google.api.client.util.Preconditions;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.zwl.datatype.ListZwlValue;
import com.zylitics.zwl.datatype.NothingZwlValue;
import com.zylitics.zwl.datatype.StringZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import com.zylitics.zwl.exception.ZwlLangException;
import com.zylitics.zwl.function.AbstractFunction;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/*
 * Important consideration for webdriver functions:
 * 1. each function sends command run update to user, every function should invoke
 *    writeCommandUpdate method and use the appropriate method for sending a text. For example
 *    If the arguments accepted by function are not too big, we should print the argument else
 *    we should just let the function name printed for user, in some situations a custom message
 *    can also be printed, such as showing warning.
 * 2. each function should invoke webdriver related methods within handleWDExceptions context so
 *    that any webdriver exception could be wrapped inside ZwlLangException which is what Zwl
 *    expect, all other exceptions are treated as system related exceptions and not relayed to user.
 */
public abstract class AbstractWebdriverFunction extends AbstractFunction {
  
  protected final APICoreProperties.Webdriver wdProps;
  
  protected final BuildCapability buildCapability;
  
  protected final RemoteWebDriver driver;
  
  protected final PrintStream printStream;
  
  protected final WebDriver.Window window;
  
  protected final WebDriver.Options options;
  
  protected final WebDriver.TargetLocator targetLocator;
  
  // Accepts everything needed to run the webdriver function.
  // In this abstract class we accept RemoteWebDriver rather than WebDriver so that we could
  // access other interfaces that RemoteWebDriver implements like JavascriptExecutor, Interactive,
  // TakesScreenshot etc. There will be another abstract classes for role interfaces like WebStorage
  // so that functions those need to access them can extend those abstract classes rather than this
  // one.
  protected AbstractWebdriverFunction(APICoreProperties.Webdriver wdProps,
                            BuildCapability buildCapability,
                            RemoteWebDriver driver,
                            PrintStream printStream) {
    this.wdProps = wdProps;
    this.buildCapability = buildCapability;
    this.driver = driver;
    this.printStream = printStream;
    options = driver.manage();
    window = options.window();
    targetLocator = driver.switchTo();
  }
  
  protected <V> V handleWDExceptions(Callable<V> code) {
    try {
      return code.call();
    } catch (WebDriverException wdEx) {
      // wrap webdriver exceptions and throw.
      throw new ZwlLangException(wdEx, withLineNCol(wdEx.getMessage()));
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
  
  protected void writeCommandUpdate(String text) {
    printStream.println(withLineNCol(text));
  }
  
  protected String withArgsCommandUpdateText(List<ZwlValue> args) {
    if (args.size() == 0) {
      return onlyCommandUpdateText();
    }
    return String.format("Executing command %s with arguments %s", getName(),
        args.stream().map(Objects::toString).collect(Collectors.joining(",")));
  }
  
  protected String onlyCommandUpdateText() {
    return "Executing command " + getName();
  }
  
  protected ZwlValue tryGetStringZwlValue(String val) {
    return val == null ? new NothingZwlValue() : new StringZwlValue(val);
  }
  
  protected ZwlValue tryGetStringZwlValues(Collection<String> values) {
    return new ListZwlValue(values.stream()
        .map(s -> s == null ? new NothingZwlValue() : new StringZwlValue(s))
        .collect(Collectors.toList()));
  }
  
  protected WindowType parseWindowType(String winType) {
    try {
      return Enum.valueOf(WindowType.class, winType);
    } catch (IllegalArgumentException ae) {
      // if user gives an invalid type, don't throw exception but choose a type that suits most.
      return WindowType.TAB;
    }
  }
  
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  protected boolean isValidElemId(String elemId) {
    return elemId
        .matches("[a-zA-Z0-9]{8}-?[a-zA-Z0-9]{4}-?[a-zA-Z0-9]{4}-?[a-zA-Z0-9]{4}-?[a-zA-Z0-9]{12}");
  }
  
  protected RemoteWebElement getElement(String elemIdOrSelector) {
    return getElement(elemIdOrSelector, true);
  }
  
  protected RemoteWebElement getElement(String elemIdOrSelector,
                                        @SuppressWarnings("SameParameterValue") boolean wait) {
    if (!isValidElemId(elemIdOrSelector)) {
      return findElement(driver, elemIdOrSelector, wait);
    }
    return getWebElementUsingElemId(elemIdOrSelector);
  }
  
  protected List<RemoteWebElement> getElements(List<String> elemIdsOrSelectors) {
    return getElements(elemIdsOrSelectors, true);
  }
  
  protected List<RemoteWebElement> getElements(List<String> elemIdsOrSelectors,
                                               @SuppressWarnings("SameParameterValue") boolean wait) {
    return elemIdsOrSelectors.stream().map(s ->
        !isValidElemId(s) ? findElement(driver, s, wait) : getWebElementUsingElemId(s))
        .collect(Collectors.toList());
  }
  
  /**
   * Should be used by functions that expect more than one element. User can either send multiple
   * elemIds or selectors or just one selector that is meant to fetch multiple elements. If
   * succeeded, this method is guaranteed to return some element(s).
   * <p>This method throws {@link NoSuchElementException} if argument list has just one item,
   * the selector, and it couldn't retrieve any element.</p>
   * @param args The raw arguments received by function.
   * @return List of {@link RemoteWebElement}s
   */
  protected List<RemoteWebElement> getElementsUnderstandingArgs(List<ZwlValue> args) {
    Preconditions.checkArgument(args.size() > 0, "Expected at least one argument");
    
    if (args.size() > 1) {
      // Following can't return empty list because each selector/elemId is evaluated separately,
      // if a selector returns nothing, exception will be thrown
      return getElements(args.stream().map(Objects::toString).collect(Collectors.toList()));
    }
    // we got only one argument, try finding elementS from it.
    String selector = tryCastString(0, args.get(0));
    List<RemoteWebElement> elements = findElements(driver, selector, true);
    // we must throw exception if the lone selector couldn't yield element(s) cause it's required
    // we've some elements to perform the desired action.
    if (elements.size() == 0) {
      throw getNoSuchElementException(selector);
    }
    return elements;
  }
  
  protected RemoteWebElement getWebElementUsingElemId(String elemId) {
    RemoteWebElement element = new RemoteWebElement();
    element.setParent(driver);
    element.setId(elemId);
    return element;
  }
  
  protected RemoteWebElement findElement(SearchContext ctx, String cssSelector, boolean wait) {
    Supplier<RemoteWebElement> s = () ->
        (RemoteWebElement) ctx.findElement(By.cssSelector(cssSelector));
    return waitOrNot(s, wait);
  }
  
  protected List<RemoteWebElement> findElements(SearchContext ctx, String cssSelector,
                                                boolean wait) {
    Supplier<List<RemoteWebElement>> s = () ->
        ctx.findElements(By.cssSelector(cssSelector)).stream()
        .map(e -> (RemoteWebElement) e).collect(Collectors.toList());
    return waitOrNot(s, wait);
  }
  
  private <T> T waitOrNot(Supplier<T> s, boolean wait) {
    if (wait) {
      return getElementAccessWait().until(d -> s.get());
    }
    return s.get();
  }
  
  protected ZwlValue convertIntoZwlElemId(RemoteWebElement remoteWebElement) {
    return new StringZwlValue(remoteWebElement.getId());
  }
  
  protected ZwlValue convertIntoZwlElemIds(List<RemoteWebElement> remoteWebElements) {
    return new ListZwlValue(remoteWebElements.stream()
        .map(this::convertIntoZwlElemId).collect(Collectors.toList()));
  }
  
  protected WebDriverWait getElementAccessWait() {
    int timeout = buildCapability.getWdTimeoutsElementAccess();
    if (timeout == 0) {
      timeout = wdProps.getDefaultTimeoutElementAccess();
    }
    
    return new WebDriverWait(driver, Duration.ofMillis(timeout));
  }
  
  protected NoSuchElementException getNoSuchElementException(String selector) {
    return new NoSuchElementException("Cannot locate an element using " + selector);
  }
}
