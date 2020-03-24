package com.zylitics.btbr.webdriver.functions.elements.interaction.keys;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.btbr.webdriver.functions.action.Modifiers;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.KeyInput;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Supplier;

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
    String[] keys = args.stream().map(Objects::toString).toArray(String[]::new);
    return handleWDExceptions(() -> {
      // every browsing context should have a body element.
      RemoteWebElement element = findElement(driver, "body", true);
      actionSendKeysCustom(element, keys);
      return _void;
    });
  }
  
  // the Actions.sendKeys method presses each key and immediately releases which leads to no affect
  // for modifier keys, this custom method fix that. It will detect modifier keys, presses them
  // and retains the press until all other non modifier keys are pressed/released.
  private void actionSendKeysCustom(RemoteWebElement element, CharSequence... keys) {
    Actions actions = new Actions(driver);
    KeyInput defaultKeyboard = new KeyInput("default keyboard");
    actions.click(element); // focus
    Set<Integer> modifierToKeyUp = new HashSet<>();
    for (CharSequence key : keys) {
      key.codePoints().forEach(codePoint -> {
        // it's important that we first keydown any modifier and then non modifiers so that it takes
        // affect for non modifiers.
        if (isSupportedModifier(codePoint)) {
          if (modifierToKeyUp.add(codePoint)) {
            actions.tick(defaultKeyboard.createKeyDown(codePoint));
          }
          return;
        }
        actions.tick(defaultKeyboard.createKeyDown(codePoint));
        actions.tick(defaultKeyboard.createKeyUp(codePoint));
      });
    }
    modifierToKeyUp.forEach(defaultKeyboard::createKeyUp);
    actions.perform();
  }
  
  private boolean isSupportedModifier(int codePoint) {
    switch (codePoint) {
      case Modifiers.SHIFT:
      case Modifiers.ALT:
      case Modifiers.COMMAND:
      case Modifiers.CONTROL:
        return true;
    }
    return false;
  }
}
