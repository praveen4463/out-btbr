package com.zylitics.btbr.webdriver.functions.timeout;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.Configuration;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.btbr.webdriver.TimeoutType;
import com.zylitics.zwl.datatype.DoubleZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import com.zylitics.zwl.exception.ZwlLangException;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public class GetTimeout extends AbstractWebdriverFunction {
  
  public GetTimeout(APICoreProperties.Webdriver wdProps,
                  BuildCapability buildCapability,
                  RemoteWebDriver driver,
                  PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "getTimeout";
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
    String t = tryCastString(0, args.get(0));
    try {
      TimeoutType timeoutType = parseEnum(0, args.get(0), TimeoutType.class);
      return new DoubleZwlValue(new Configuration().getTimeouts(wdProps, buildCapability,
          timeoutType));
    } catch (IllegalArgumentException i) {
      throw new ZwlLangException("Given timeout type " + t + " isn't valid.", i);
    }
  }
}
