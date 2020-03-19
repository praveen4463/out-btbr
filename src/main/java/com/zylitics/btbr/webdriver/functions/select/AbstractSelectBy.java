package com.zylitics.btbr.webdriver.functions.select;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import com.zylitics.zwl.util.ParseUtil;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractSelectBy extends AbstractWebdriverFunction {
  
  public AbstractSelectBy(APICoreProperties.Webdriver wdProps,
                                BuildCapability buildCapability,
                                RemoteWebDriver driver,
                                PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
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
      String elemIdOrSelector = tryCastString(0, args.get(0));
      return handleWDExceptions(() -> {
        Select select = new Select(getElement(elemIdOrSelector));
        select(select, args.get(1));
        return _void;
      });
    }
    
    throw unexpectedEndOfFunctionOverload(argsCount);
  }
  
  abstract void select(Select select, ZwlValue value);
}
