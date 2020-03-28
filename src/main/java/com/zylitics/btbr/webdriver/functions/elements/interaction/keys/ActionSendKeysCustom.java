package com.zylitics.btbr.webdriver.functions.elements.interaction.keys;

import com.zylitics.btbr.webdriver.functions.action.Modifiers;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.KeyInput;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

class ActionSendKeysCustom {
  
  private final RemoteWebDriver driver;
  
  ActionSendKeysCustom(RemoteWebDriver driver) {
    this.driver = driver;
  }
  
  // the Actions.sendKeys method presses each key and immediately releases which leads to no affect
  // for modifier keys, this custom method fix that. It will detect modifier keys, presses them
  // and retains the press until all other non modifier keys are pressed/released.
  void actionSendKeysCustom(@Nullable RemoteWebElement element, CharSequence... keys) {
    Actions actions = new Actions(driver);
    KeyInput defaultKeyboard = new KeyInput("default keyboard");
    if (element != null) {
      actions.click(element); // focus
    }
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
    modifierToKeyUp.forEach(codePoint -> actions.tick(defaultKeyboard.createKeyUp(codePoint)));
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
