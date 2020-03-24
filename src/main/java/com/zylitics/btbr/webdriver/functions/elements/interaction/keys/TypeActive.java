package com.zylitics.btbr.webdriver.functions.elements.interaction.keys;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class TypeActive extends AbstractWebdriverFunction {
  
  public TypeActive(APICoreProperties.Webdriver wdProps,
              BuildCapability buildCapability,
              RemoteWebDriver driver,
              PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "typeActive";
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
    if (argsCount == 0) {
      throw unexpectedEndOfFunctionOverload(argsCount);
    }
    String[] keys = args.stream().map(Objects::toString).toArray(String[]::new);
    return handleWDExceptions(() -> {
      targetLocator.activeElement().sendKeys(keys);
      return _void;
    });
  }
}
