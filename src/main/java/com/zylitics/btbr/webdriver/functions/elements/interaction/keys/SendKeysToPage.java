package com.zylitics.btbr.webdriver.functions.elements.interaction.keys;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.btbr.webdriver.functions.action.Modifiers;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.KeyInput;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

import static org.openqa.selenium.interactions.PointerInput.Kind.MOUSE;

public class SendKeysToPage extends AbstractWebdriverFunction {
  
  public SendKeysToPage(APICoreProperties.Webdriver wdProps,
                      BuildCapability buildCapability,
                      RemoteWebDriver driver,
                      PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "sendKeysToPage";
  }
  
  @Override
  public int minParamsCount() {
    return 2;
  }
  
  @Override
  public int maxParamsCount() {
    return Integer.MAX_VALUE;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    if (args.size() < 2) {
      throw unexpectedEndOfFunctionOverload(args.size());
    }
    String elemIdOrSelector = tryCastString(0, args.get(0));
    String[] keys = args.subList(1, args.size()).stream().map(Objects::toString)
        .toArray(String[]::new);
    return handleWDExceptions(() -> {
      new ActionSendKeysCustom(driver).actionSendKeysCustom(getElement(elemIdOrSelector), keys);
      return _void;
    });
  }
}
