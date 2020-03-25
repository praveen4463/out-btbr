package com.zylitics.btbr.webdriver.constants;

import com.google.common.collect.ImmutableMap;
import com.zylitics.zwl.datatype.StringZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.InvalidCoordinatesException;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;

import java.util.Map;

public class Exceptions {
  
  public static Map<String, ZwlValue> asMap() {
    return ImmutableMap.<String, ZwlValue>builder()
        .put("elemClickInterceptedEx", ex(ElementClickInterceptedException.class))
        .put("elemNotInteractableEx", ex(ElementNotInteractableException.class))
        .put("elemNotSelectableEx", ex(ElementNotSelectableException.class))
        .put("elemNotVisibleEx", ex(ElementNotVisibleException.class))
        .put("invalidArgumentEx", ex(InvalidArgumentException.class))
        .put("invalidCookieDomainEx", ex(InvalidCookieDomainException.class))
        .put("invalidElemStateEx", ex(InvalidElementStateException.class))
        .put("invalidSelectorEx", ex(InvalidSelectorException.class))
        .put("jsEx", ex(JavascriptException.class))
        .put("noAlertPresentEx", ex(NoAlertPresentException.class))
        .put("noSuchContextEx", ex(NoSuchContextException.class))
        .put("noSuchCookieEx", ex(NoSuchCookieException.class))
        .put("noSuchElemEx", ex(NoSuchElementException.class))
        .put("noSuchFrameEx", ex(NoSuchFrameException.class))
        .put("noSuchWindowEx", ex(NoSuchWindowException.class))
        .put("scriptTimeoutEx", ex(ScriptTimeoutException.class))
        .put("staleElemEx", ex(StaleElementReferenceException.class))
        .put("timeoutEx", ex(TimeoutException.class))
        .put("unableToSetCookieEx", ex(UnableToSetCookieException.class))
        .put("unhandledAlertEx", ex(UnhandledAlertException.class))
        .put("invalidCoordinatesEx", ex(InvalidCoordinatesException.class))
        .put("moveTargetOutOfBoundsEx", ex(MoveTargetOutOfBoundsException.class))
        .put("illegalArgumentEx",
            new StringZwlValue(IllegalArgumentException.class.getSimpleName()))
        .build();
  }
  
  private static ZwlValue ex(Class<? extends WebDriverException> t) {
    return new StringZwlValue(t.getSimpleName());
  }
}
