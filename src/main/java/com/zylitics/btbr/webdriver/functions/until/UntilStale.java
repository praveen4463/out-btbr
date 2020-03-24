package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.zwl.datatype.BooleanZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public class UntilStale extends AbstractUntilExpectation {
  
  public UntilStale(APICoreProperties.Webdriver wdProps,
                         BuildCapability buildCapability,
                         RemoteWebDriver driver,
                         PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream, 1, 1);
  }
  
  @Override
  public String getName() {
    return "untilStale";
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
  
    if (args.size() != 1) {
      throw unexpectedEndOfFunctionOverload(args.size());
    }
    String s = tryCastString(0, args.get(0));
    WebDriverWait wait = getWait(TimeoutType.ELEMENT_ACCESS);
    RemoteWebElement e = getWebElementUsingElemId(s);
    return handleWDExceptions(() ->
          new BooleanZwlValue(wait.until(ExpectedConditions.stalenessOf(e))));
  }
}
