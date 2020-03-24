package com.zylitics.btbr.webdriver.functions.elements.state;

import com.google.common.collect.ImmutableMap;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.DoubleZwlValue;
import com.zylitics.zwl.datatype.MapZwlValue;
import com.zylitics.zwl.datatype.NothingZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.Point;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public class GetElementViewportCoordinates extends AbstractWebdriverFunction {
  
  public GetElementViewportCoordinates(APICoreProperties.Webdriver wdProps,
                        BuildCapability buildCapability,
                        RemoteWebDriver driver,
                        PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "getElementViewportCoordinates";
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
    Point p =
        handleWDExceptions(() -> getElement(elemIdOrSelector).getCoordinates().inViewPort());
    if (p == null) {
      return new NothingZwlValue();
    }
    return new MapZwlValue(ImmutableMap.of(
        "x", new DoubleZwlValue(p.x),
        "y", new DoubleZwlValue(p.y)
    ));
  }
}
