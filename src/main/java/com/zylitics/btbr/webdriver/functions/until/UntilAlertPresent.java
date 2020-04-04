package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.TimeoutType;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public class UntilAlertPresent extends AbstractUntilExpectation {
  
  public UntilAlertPresent(APICoreProperties.Webdriver wdProps,
                               BuildCapability buildCapability,
                               RemoteWebDriver driver,
                               PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream, 0, 0);
  }
  
  @Override
  public String getName() {
    return "untilAlertPresent";
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    return handleWDExceptions(() -> {
      getWait(TimeoutType.ELEMENT_ACCESS).until(ExpectedConditions.alertIsPresent());
      return _void;
    });
  }
}
