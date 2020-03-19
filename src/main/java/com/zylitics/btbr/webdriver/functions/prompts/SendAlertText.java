package com.zylitics.btbr.webdriver.functions.prompts;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public class SendAlertText extends AbstractWebdriverFunction {
  
  public SendAlertText(APICoreProperties.Webdriver wdProps,
                      BuildCapability buildCapability,
                      RemoteWebDriver driver,
                      PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "sendAlertText";
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
    
    int argsCount = args.size();
    writeCommandUpdate(withArgsCommandUpdateText(args));
    
    if (argsCount == 1) {
      return handleWDExceptions(() -> {
        targetLocator.alert().sendKeys(args.get(0).toString());
        return _void;
      });
    }
    
    throw unexpectedEndOfFunctionOverload(argsCount);
  }
}
