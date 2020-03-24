package com.zylitics.btbr.webdriver.functions.cookies;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

public class AddCookie extends AbstractWebdriverFunction {
  
  public AddCookie(APICoreProperties.Webdriver wdProps,
                               BuildCapability buildCapability,
                               RemoteWebDriver driver,
                               PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "addCookie";
  }
  
  @Override
  public int minParamsCount() {
    return 2;
  }
  
  @Override
  public int maxParamsCount() {
    return 7;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    if (args.size() < 2) {
      throw unexpectedEndOfFunctionOverload(args.size());
    }
    return add(args);
  }
  
  private ZwlValue add(List<ZwlValue> args) {
    int argsCount = args.size();
    String name = tryCastString(0, args.get(0));
    String value = tryCastString(1, args.get(1));
    String path = null;
    String domain = null;
    Date expiryDate = null;
    boolean isSecure = false;
    boolean isHttpOnly = false;
    if (argsCount >= 3) {
      path = tryCastString(2, args.get(2));
    }
    if (argsCount >= 4) {
      domain = tryCastString(3, args.get(3));
    }
    if (argsCount >= 5) {
      expiryDate = new Date(System.currentTimeMillis() + parseDouble(4, args.get(4)).longValue());
    }
    if (argsCount >= 6) {
      isSecure = parseBoolean(5, args.get(5));
    }
    if (argsCount == 7) {
      isHttpOnly = parseBoolean(6, args.get(6));
    }
    Cookie cookie = new Cookie(name, value, domain, path, expiryDate, isSecure, isHttpOnly);
    return handleWDExceptions(() -> {
      options.addCookie(cookie);
      return _void;
    });
  }
}
