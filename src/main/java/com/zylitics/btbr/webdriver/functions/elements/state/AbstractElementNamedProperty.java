package com.zylitics.btbr.webdriver.functions.elements.state;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractElementNamedProperty extends AbstractWebdriverFunction {
  
  public AbstractElementNamedProperty(APICoreProperties.Webdriver wdProps,
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
  
    if (args.size() < 2) {
      throw unexpectedEndOfFunctionOverload(args.size());
    }
    String elemIdOrSelector = tryCastString(0, args.get(0));
    String propertyName = tryCastString(1, args.get(1));
    String propertyValue = handleWDExceptions(() ->
        get(getElement(elemIdOrSelector), propertyName));
    return tryGetStringZwlValue(propertyValue);
  }
  
  protected abstract String get(RemoteWebElement element, String propertyName);
}
