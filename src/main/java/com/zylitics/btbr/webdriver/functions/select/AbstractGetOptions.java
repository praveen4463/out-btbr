package com.zylitics.btbr.webdriver.functions.select;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.Select;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractGetOptions extends AbstractWebdriverFunction {
  
  public AbstractGetOptions(APICoreProperties.Webdriver wdProps,
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
    List<WebElement> options = getOptions(new Select(getElement(elemIdOrSelector)));
    return convertIntoZwlElemIds(options.stream().map(e -> (RemoteWebElement) e)
        .collect(Collectors.toList()));
  }
  
  abstract List<WebElement> getOptions(Select select);
}
