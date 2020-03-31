package com.zylitics.btbr.webdriver.functions.action;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

/**
 * Shorthand function for easily scrolling in document, can be also done by using
 * {@link PerformAction} with {@link ActionFunctions.Move}
 */
/*
Scroll allows scrolling outside the viewport but Move doesn't because per the specs in action
section, if the target x, y is outside viewport a move out of bounds should be thrown.
 */
public class Scroll extends AbstractWebdriverFunction {
  
  public Scroll(APICoreProperties.Webdriver wdProps,
                     BuildCapability buildCapability,
                     RemoteWebDriver driver,
                     PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "scroll";
  }
  
  @Override
  public int minParamsCount() {
    return 1;
  }
  
  @Override
  public int maxParamsCount() {
    return 3;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
  
    // Initially I used ActionFunctions.Move() but it didn't work with firefox when the target was
    // out if viewport and threw a MoveTargetOutOfBoundsException. Scrolling is proposed as a
    // command in spec, until then I am doing it with js for all drivers.
    // Note that MoveTargetOutOfBoundsException exception is expected as per the specs's point 7, 8
    // in https://w3c.github.io/webdriver/#dfn-dispatch-a-pointermove-action.
    return handleWDExceptions(() -> {
      switch (args.size()) {
        case 1:
          driver.executeScript("arguments[0].scrollIntoView(true);",
              getElement(tryCastString(0, args.get(0))));
          break;
        case 2:
          driver.executeScript(String.format("window.scrollBy(%d, %d)",
              parseDouble(0, args.get(0)).intValue(), parseDouble(1, args.get(1)).intValue()));
          break;
        case 3:
          // get element's position and add the offset into it before calling window.scroll
          Point location = getElement(tryCastString(0, args.get(0))).getLocation();
          driver.executeScript(String.format("window.scroll(%d, %d)",
              parseDouble(1, args.get(1)).intValue() + location.x,
              parseDouble(2, args.get(2)).intValue() + location.y));
          break;
        default:
          throw unexpectedEndOfFunctionOverload(args.size());
      }
      return _void;
    });
  }
}
