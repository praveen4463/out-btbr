package com.zylitics.btbr.webdriver.functions.document;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.util.CollectionUtil;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.*;
import com.zylitics.zwl.exception.InvalidTypeException;
import com.zylitics.zwl.util.ParseUtil;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Supplier;

// Note that JavascriptExecutor does't mention Map as a valid argument in executeScript and async
// methods but that's a valid argument and can be given.
public abstract class AbstractExecuteScript extends AbstractWebdriverFunction {
  
  public AbstractExecuteScript(APICoreProperties.Webdriver wdProps,
                       BuildCapability buildCapability,
                       RemoteWebDriver driver,
                       PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  // arguments passed to this function may be multiple lists, maps or anything that a valid type.
  @Override
  protected boolean doNotExpandListToArguments() {
    return true;
  }
  
  @Override
  public int minParamsCount() {
    return 1;
  }
  
  @Override
  public int maxParamsCount() {
    return Integer.MAX_VALUE;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
  
    int argsCount = args.size();
    if (argsCount < 1) {
      throw unexpectedEndOfFunctionOverload(argsCount);
    }
    String script = tryCastString(0, args.get(0));
    // don't let capacity be 0. The type is list of objects cause args may contain different type
    // of items.
    List<Object> scriptArgs = new ArrayList<>(CollectionUtil.getInitialCapacity(argsCount));
    if (argsCount > 1) {
      args.subList(1, argsCount).forEach(z -> scriptArgs.add(convertFromZwlValue(z)));
    }
    return handleWDExceptions(() -> transformJsResponse(execute(script, scriptArgs.toArray())));
  }
  
  protected abstract Object execute(String script, Object... args);
  
  /**
   * The goal of this method is to convert ZwlValue into a type that is acceptable and work as
   * expected in {@link org.openqa.selenium.JavascriptExecutor}. As mentioned there it requires
   * in arguments either Number (double or long), Boolean, WebElement, String, List/Map with
   * combination of previous types. User sends these types in ZWL in form of ZwlValue. This method
   * parses those ZwlValue arguments and returns the contained type. When arguments contain List or
   * Map, those are recursively iterated and their ZwlValue items are further given to this method
   * to get the actual parsed value creating List/Map that contain actually parsed values, it
   * processes nested lists/maps as well. The final result is an Object that is either Double,
   * Long, Boolean, WebElement, String, List of these types, Map of these types.
   * @param val the value to parse
   * @return One of Double, Long, Boolean, WebElement, String, List of these types,
   *         Map of these types.
   */
  private Object convertFromZwlValue(ZwlValue val) {
    InvalidTypeException iEx = new InvalidTypeException("");
    /*First do the internal parsing to help user sending number/boolean as string, otherwise they
    will have to send the correct type to be able to get correct argument. For example if user
    extracted a number from a string and stored in a variable, later used that variable and
    passed to this function. Although the type of variable is string but it contains a number,
    if we don't do parsing, it will be interpreted as string by webdriver while executing script.
    js can also do parsing but in some calculations its helpful to provide correct type.*/
    
    // !! keep parsing stuff on top and not the string cast because a number may be given as
    // a string and may require parsing.
    try {
      Double d = ParseUtil.parseDouble(val, () -> iEx);
      // if this double wasn't actually a double, check that and if true, return a long value.
      if (d % 1 == 0) {
        return d.longValue();
      }
      return d;
    } catch (InvalidTypeException ignore) {}
    
    try {
      return ParseUtil.parseBoolean(val, () -> iEx);
    } catch (InvalidTypeException ignore) {}
  
    Optional<String> s = val.getStringValue();
    if (s.isPresent()) {
      if(isValidElemId(s.get())) {
        return getWebElementUsingElemId(s.get());
      }
      return s.get();
    }
  
    if (val.getListValue().isPresent()) {
      List<ZwlValue> lz = val.getListValue().get();
      List<Object> lo = new ArrayList<>(CollectionUtil.getInitialCapacity(lz.size()));
      lz.forEach(z -> lo.add(convertFromZwlValue(z)));
      return lo;
    }
  
    if (val.getMapValue().isPresent()) {
      Map<String, ZwlValue> mz = val.getMapValue().get();
      Map<String, Object> mo = new HashMap<>(CollectionUtil.getInitialCapacity(mz.size()));
      mz.forEach(((k, v) -> mo.put(k, convertFromZwlValue(v))));
      return mo;
    }
    
    throw new IllegalArgumentException("Couldn't recognize the type of " + val); // shouldn't happen
  }
}
