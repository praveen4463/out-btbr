package com.zylitics.btbr.webdriver.functions.select;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import com.zylitics.zwl.exception.InvalidTypeException;
import com.zylitics.zwl.util.ParseUtil;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

abstract class AbstractSelectDeselectBy extends AbstractWebdriverFunction {
  
  public AbstractSelectDeselectBy(APICoreProperties.Webdriver wdProps,
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
    
    if (args.size() == 2) {
      throw unexpectedEndOfFunctionOverload(args.size());
    }
    String elemIdOrSelector = tryCastString(0, args.get(0));
    return handleWDExceptions(() -> {
      Select select = new Select(getElement(elemIdOrSelector));
      selectDeselect(select, args.get(1));
      return _void;
    });
  }
  
  abstract void selectDeselect(Select select, ZwlValue value);
  
  int parseSelectIndex(ZwlValue value) {
    return ParseUtil.parseDouble(value,
        new InvalidTypeException(withLineNCol(getName() + " requires a numeric index.")))
        .intValue();
  }
}
