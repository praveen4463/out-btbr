package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.Configuration;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.btbr.webdriver.TimeoutType;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.PrintStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

// FLT = function level timeout
abstract class AbstractUntilExpectation extends AbstractWebdriverFunction {
  
  final static String TIMEOUT_KEY = "fltTimeout";
  
  final static String POLL_KEY = "fltPoll";
  
  private final static int DEFAULT_POLL_EVERY_MILLIS = 500;
  
  private final int minParams;
  
  private final int maxParams;
  
  // !!! These are saved in state of functions, remember to reset on each invocation.
  private int fltTimeoutMillis;
  
  private int fltPollMillis;
  
  public AbstractUntilExpectation(APICoreProperties.Webdriver wdProps,
                                  BuildCapability buildCapability,
                                  RemoteWebDriver driver,
                                  PrintStream printStream,
                                  int minParams,
                                  int maxParams) {
    super(wdProps, buildCapability, driver, printStream);
    this.minParams = minParams;
    this.maxParams = maxParams;
  }
  
  @Override
  public int minParamsCount() {
    return minParams;
  }
  
  @Override
  public int maxParamsCount() {
    if (maxParams == Integer.MAX_VALUE) {
      return maxParams;
    }
    return maxParams + 1; // room for the 'until function level timeout' result as last argument
  }
  
  // strip the last argument given if that is result of 'until function level timeout'. We should
  // strip that so that if a varargs argument precedes this argument, varargs could be correctly
  // expanded by the super class (cause we don't expand varargs if it's not the last argument).
  // I want the timeout related functionality to be abstract from all classes as if none other
  // class knows about it.
  // invoke super's invoke only after strip is done because it work on argument list.
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    // reset variables in state that are supposed to be different in each invocation.
    fltTimeoutMillis = 0;
    fltPollMillis = 0;
    
    int argsCount = args.size();
    if (argsCount > 0) {
      int index = argsCount - 1;
      Optional<Map<String, ZwlValue>> m = args.get(index).getMapValue();
      if (m.isPresent() && isFLTArgument(m.get())) {
        Map<String, ZwlValue> timeout = m.get();
        fltTimeoutMillis = parseDouble(index, timeout.get(TIMEOUT_KEY)).intValue();
        fltPollMillis = parseDouble(index, timeout.get(POLL_KEY)).intValue();
        // now strip the FLT argument
        args.remove(index);
      }
    }
    return super.invoke(args, defaultValue, lineNColumn);
  }
  
  private boolean isFLTArgument(Map<String, ZwlValue> m) {
    return m.size() == 2 && m.containsKey(TIMEOUT_KEY) && m.containsKey(POLL_KEY);
  }
  
  @Override
  protected WebDriverWait getWait(TimeoutType timeoutType, String timeoutMsg) {
    // if user has provided custom timeouts for this function try use that first
    int timeout;
    if (fltTimeoutMillis > 0) {
      timeout = fltTimeoutMillis;
    } else {
      timeout = new Configuration().getTimeouts(wdProps, buildCapability, timeoutType);
    }
    int poll = fltPollMillis > 0 ? fltPollMillis : DEFAULT_POLL_EVERY_MILLIS;
  
    WebDriverWait wait =
        new WebDriverWait(driver, Duration.ofMillis(timeout), Duration.ofMillis(poll));
    wait.withMessage(timeoutMsg);
    return wait;
  }
  
  /**
   * Gets instance of {@link WebDriverWait} using the given timeout type.
   * @param timeoutType The default timeout this function uses, when a custom timeout is provided
   *                    that will take precedence over this.
   * @return a new instance of {@link WebDriverWait}
   */
  WebDriverWait getWait(TimeoutType timeoutType) {
    // when timeout, exception message will say something like
    // "Expected condition failed: waiting for untilxxx (ie the name of until function that
    // timed-out). This is done to avoid a message need to be passed from all until functions and
    // providing the parameters etc because it's clear from until function's name what were we
    // waiting for.
    return getWait(timeoutType, "waiting for " + getName());
  }
}
