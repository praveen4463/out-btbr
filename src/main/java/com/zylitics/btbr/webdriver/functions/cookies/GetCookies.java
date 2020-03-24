package com.zylitics.btbr.webdriver.functions.cookies;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.util.CollectionUtil;
import com.zylitics.zwl.datatype.*;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Supplier;

public class GetCookies extends AbstractGetCookie {
  
  public GetCookies(APICoreProperties.Webdriver wdProps,
                    BuildCapability buildCapability,
                    RemoteWebDriver driver,
                    PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "getCookies";
  }
  
  @Override
  public int minParamsCount() {
    return 0;
  }
  
  @Override
  public int maxParamsCount() {
    return 0;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    Set<Cookie> cookies = handleWDExceptions(options::getCookies);
    if (cookies == null) {
      return new ListZwlValue(new ArrayList<>(0));
    }
    List<ZwlValue> result = new ArrayList<>(CollectionUtil.getInitialCapacity(cookies.size()));
    cookies.forEach(c -> result.add(new MapZwlValue(cookieToZwlMap(c))));
    return new ListZwlValue(result);
  }
}
