package com.zylitics.btbr.webdriver.functions.action;

import com.zylitics.btbr.webdriver.functions.action.ActionFunctions.*;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import com.zylitics.zwl.exception.ZwlLangException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Takes action functions which will be converted into {@link Map}, so each argument to this
 * function is a single map entry. See {@link ActionFunctions}
  */
public class PerformAction extends AbstractWebdriverFunction {
  
  public PerformAction(APICoreProperties.Webdriver wdProps,
                      BuildCapability buildCapability,
                      RemoteWebDriver driver,
                      PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "performAction";
  }
  
  @Override
  public int minParamsCount() {
    return 1;
  }
  
  @Override
  public int maxParamsCount() {
    return Integer.MAX_VALUE;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    if (args.size() == 0) {
      throw unexpectedEndOfFunctionOverload(args.size());
    }
    process(args);
    return _void;
  }
  
  private void process(List<ZwlValue> args) {
    Actions actions = new Actions(driver);
    for (int i = 0; i < args.size(); i++) {
      Optional<Map<String, ZwlValue>> m = args.get(i).getMapValue();
      if (!m.isPresent()) {
        throw new ZwlLangException(new IllegalArgumentException(),
            String.format("Argument at: %s, isn't one of the action function. Please provide" +
                " only the allowed action functions to perform them. %s", i, lineNColumn.get()));
      }
      if (m.get().size() != 1) {
        throw new RuntimeException("Action function at argument index " + i + " returned a" +
            " map of size " + m.get().size() + " rather than single valued one.");
      }
      
      Map.Entry<String, ZwlValue> entry = m.get().entrySet().iterator().next();
      String actionFunctionName = entry.getKey();
      List<ZwlValue> actionFunctionArgs = entry.getValue().getListValue().orElseThrow(() ->
          new RuntimeException("Action function " + actionFunctionName + " returned a map that" +
              " has a value that is not a list of arguments."));
      ActionFunction actionFunction;
      switch (actionFunctionName) {
        case ActionFunctions.FOCUS:
          actionFunction = new Focus();
          break;
        case ActionFunctions.SHIFT_DOWN:
          actionFunction = new ShiftDown();
          break;
        case ActionFunctions.SHIFT_UP:
          actionFunction = new ShiftUp();
          break;
        case ActionFunctions.CTRL_DOWN:
          actionFunction = new CtrlDown();
          break;
        case ActionFunctions.CTRL_UP:
          actionFunction = new CtrlUp();
          break;
        case ActionFunctions.CMD_DOWN:
          actionFunction = new CmdDown();
          break;
        case ActionFunctions.CMD_UP:
          actionFunction = new CmdUp();
          break;
        case ActionFunctions.ALT_DOWN:
          actionFunction = new AltDown();
          break;
        case ActionFunctions.ALT_UP:
          actionFunction = new AltUp();
          break;
        case ActionFunctions.SEND_KEYS:
          actionFunction = new SendKeys();
          break;
        case ActionFunctions.MOVE:
          actionFunction = new Move();
          break;
        case ActionFunctions.HOLD:
          actionFunction = new Hold();
          break;
        case ActionFunctions.RELEASE:
          actionFunction = new Release();
          break;
        case ActionFunctions.CLICK_ONCE:
          actionFunction = new ClickOnce();
          break;
        case ActionFunctions.DOUBLE_CLICK:
          actionFunction = new DoubleClick();
          break;
        case ActionFunctions.CONTEXT_CLICK:
          actionFunction = new ContextClick();
          break;
        case ActionFunctions.PAUSE:
          actionFunction = new Pause();
          break;
        default:
          throw new ZwlLangException(new IllegalArgumentException(), withLineNCol("Unrecognized" +
              " action function at argument " + i));
      }
      actionFunction.process(this, actions, actionFunctionArgs);
    }
  }
}
