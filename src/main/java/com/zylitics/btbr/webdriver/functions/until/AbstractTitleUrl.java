package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.zwl.datatype.BooleanZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractTitleUrl extends AbstractUntilExpectation {
  
  public AbstractTitleUrl(APICoreProperties.Webdriver wdProps,
                      BuildCapability buildCapability,
                      RemoteWebDriver driver,
                      PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream, 1, 1);
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
  
    writeCommandUpdate(withArgsCommandUpdateText(args));
    int argsCount = args.size();
    
    if (argsCount == 1) {
      String s = tryCastString(0, args.get(0));
      return handleWDExceptions(() ->
          new BooleanZwlValue(getWait(TimeoutType.PAGE_LOAD).until(condition(s))));
    }
    
    throw unexpectedEndOfFunctionOverload(argsCount);
  }
  
  abstract ExpectedCondition<Boolean> condition(String s);
}
