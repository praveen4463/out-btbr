package com.zylitics.btbr.webdriver.functions.elements.interaction.keys;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TypeUsingMap extends AbstractWebdriverFunction {
  
  public TypeUsingMap(APICoreProperties.Webdriver wdProps,
              BuildCapability buildCapability,
              RemoteWebDriver driver,
              PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "typeUsingMap";
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
    
    writeCommandUpdate(onlyCommandUpdateText());
    int argsCount = args.size();
    
    if (argsCount == 1) {
      Map<String, ZwlValue> m = tryCastMap(0, args.get(0));
      return handleWDExceptions(() -> {
        m.forEach((k, v) -> getElement(k).sendKeys(v.toString()));
        return _void;
      });
    }
    
    throw unexpectedEndOfFunctionOverload(argsCount);
  }
}
