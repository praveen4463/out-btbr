package com.zylitics.btbr.webdriver.functions.color;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.BooleanZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.Color;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public class ColorMatches extends AbstractWebdriverFunction {
  
  public ColorMatches(APICoreProperties.Webdriver wdProps,
                     BuildCapability buildCapability,
                     RemoteWebDriver driver,
                     PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "colorMatches";
  }
  
  @Override
  public int minParamsCount() {
    return 2;
  }
  
  @Override
  public int maxParamsCount() {
    return 2;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    writeCommandUpdate(withArgsCommandUpdateText(args));
    int argsCount = args.size();
    
    if (argsCount == 2) {
      Color color1 = Color.fromString(tryCastString(0, args.get(0)));
      Color color2 = Color.fromString(tryCastString(1, args.get(1)));
      return new BooleanZwlValue(color1.equals(color2));
    }
    
    throw unexpectedEndOfFunctionOverload(argsCount);
  }
}
