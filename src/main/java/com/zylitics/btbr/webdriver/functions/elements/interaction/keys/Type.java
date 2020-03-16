package com.zylitics.btbr.webdriver.functions.elements.interaction.keys;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Type extends AbstractWebdriverFunction {
  
  public Type(APICoreProperties.Webdriver wdProps,
                BuildCapability buildCapability,
                RemoteWebDriver driver,
                PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "type";
  }
  
  @Override
  public int minParamsCount() {
    return 2;
  }
  
  @Override
  public int maxParamsCount() {
    return Integer.MAX_VALUE;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    writeCommandUpdate(onlyCommandUpdateText());
    int argsCount = args.size();
  
    String elemIdOrSelector = tryCastString(0, args.get(0));
    if (argsCount >= 2) {
      return handleWDExceptions(() -> {
        List<String> keys = args.subList(1, argsCount - 1)
            .stream().map(Objects::toString).collect(Collectors.toList());
        getElement(elemIdOrSelector).sendKeys(keys);
        return _void;
      });
    }
    
    throw unexpectedEndOfFunctionOverload(argsCount);
  }
}
