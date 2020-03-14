package com.zylitics.btbr.webdriver.functions.context.resize;

import com.google.common.collect.ImmutableMap;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.zwl.datatype.DoubleZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.Point;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.Map;

public class GetWinPosition extends GetWinRect {
  
  public GetWinPosition(APICoreProperties.Webdriver wdProps,
                    BuildCapability buildCapability,
                    RemoteWebDriver driver,
                    PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "getWinPosition";
  }
  
  @Override
  protected Map<String, ZwlValue> get() {
    Point p = window.getPosition();
    return ImmutableMap.of(
        "x", new DoubleZwlValue(p.x),
        "y", new DoubleZwlValue(p.y)
    );
  }
}
