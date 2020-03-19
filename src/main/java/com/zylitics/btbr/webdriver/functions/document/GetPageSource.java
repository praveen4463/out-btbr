package com.zylitics.btbr.webdriver.functions.document;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public class GetPageSource extends AbstractWebdriverFunction {
  
  public GetPageSource(APICoreProperties.Webdriver wdProps,
                BuildCapability buildCapability,
                RemoteWebDriver driver,
                PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "getPageSource";
  }
  
  @Override
  public int minParamsCount() {
    return 0;
  }
  
  @Override
  public int maxParamsCount() {
    return 0;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    writeCommandUpdate(withArgsCommandUpdateText(args));
    return handleWDExceptions(() -> tryGetStringZwlValue(driver.getPageSource()));
  }
}
