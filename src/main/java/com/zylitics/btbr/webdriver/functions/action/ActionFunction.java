package com.zylitics.btbr.webdriver.functions.action;

import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.interactions.Actions;

import java.util.List;

public interface ActionFunction {
  
  void process(AbstractWebdriverFunction wd, Actions actions, List<ZwlValue> args);
}
