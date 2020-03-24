package com.zylitics.btbr.webdriver.functions.cookies;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.zwl.datatype.MapZwlValue;
import com.zylitics.zwl.datatype.NothingZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public class GetNamedCookie extends AbstractGetCookie {
  
  public GetNamedCookie(APICoreProperties.Webdriver wdProps,
                    BuildCapability buildCapability,
                    RemoteWebDriver driver,
                    PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "getNamedCookie";
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
    Cookie cookie = handleWDExceptions(() ->
        options.getCookieNamed(tryCastString(0, args.get(0))));
    if (cookie == null) {
      return new NothingZwlValue();
    }
    return new MapZwlValue(cookieToZwlMap(cookie));
  }
}
