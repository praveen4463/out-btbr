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
  
  /**
   * Gets instance of {@link WebDriverWait} using the given timeout type.
   * Note that even if a wait by default ignores some exception, it doesn't mean the code using
   * it can't handle ignored exceptions and return a custom value. When you handle that ignored
   * exception on your own, it doesn't propagate to 'until' block and doesn't let it decide
   * whether to ignore or throw. Ignoring an exception just gives you cleaner code so that you
   * don't have to catch that every time and return a false/null to re-evaluate.
   * @param timeoutType The default timeout this function uses, when a custom timeout is provided
   *                    that will take precedence over this.
   * @return a new instance of {@link WebDriverWait}
   */
  WebDriverWait getWait(TimeoutType timeoutType) {
    // if user has provided custom timeouts for this function try use that first
    int timeout;
    if (fltTimeoutMillis > 0) {
      timeout = fltTimeoutMillis;
    } else {
      timeout = new Configuration().getTimeouts(wdProps, buildCapability, timeoutType);
    }
    int poll = fltPollMillis > 0 ? fltPollMillis : DEFAULT_POLL_EVERY_MILLIS;
    
    return new WebDriverWait(driver, Duration.ofMillis(timeout), Duration.ofMillis(poll));
  }
}
