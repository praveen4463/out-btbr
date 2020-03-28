package com.zylitics.btbr.webdriver.functions.elements.state;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.NothingZwlValue;
import com.zylitics.zwl.datatype.StringZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

abstract class AbstractElementProperty extends AbstractWebdriverFunction {
  
  public AbstractElementProperty(APICoreProperties.Webdriver wdProps,
                        BuildCapability buildCapability,
                        RemoteWebDriver driver,
                        PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
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
  
    if (args.size() == 0) {
      throw unexpectedEndOfFunctionOverload(args.size());
    }
    String elemIdOrSelector = tryCastString(0, args.get(0));
    String text = handleWDExceptions(() -> getProperty(getElement(elemIdOrSelector)));
    return tryGetStringZwlValue(text);
  }
  
  protected abstract String getProperty(RemoteWebElement element);
}
