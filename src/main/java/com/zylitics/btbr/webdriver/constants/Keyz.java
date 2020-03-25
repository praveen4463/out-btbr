package com.zylitics.btbr.webdriver.constants;

import com.google.common.collect.ImmutableMap;
import com.zylitics.zwl.datatype.StringZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.Keys;

import java.util.Map;

public class Keyz {
  
  public static Map<String, ZwlValue> asMap() {
    return ImmutableMap.<String, ZwlValue>builder()
        .put("null", key(Keys.NULL))
        .put("cancel", key(Keys.CANCEL))
        .put("help", key(Keys.HELP))
        .put("back", key(Keys.BACK_SPACE))
        .put("tab", key(Keys.TAB))
        .put("clear", key(Keys.CLEAR))
        .put("return", key(Keys.RETURN))
        .put("enter", key(Keys.ENTER))
        .put("shift", key(Keys.SHIFT))
        .put("ctrl", key(Keys.CONTROL))
        .put("alt", key(Keys.ALT))
        .put("pause", key(Keys.PAUSE))
        .put("esc", key(Keys.ESCAPE))
        .put("space", key(Keys.SPACE))
        .put("pageup", key(Keys.PAGE_UP))
        .put("pagedown", key(Keys.PAGE_DOWN))
        .put("end", key(Keys.END))
        .put("home", key(Keys.HOME))
        .put("left", key(Keys.LEFT))
        .put("up", key(Keys.UP))
        .put("right", key(Keys.RIGHT))
        .put("down", key(Keys.DOWN))
        .put("insert", key(Keys.INSERT))
        .put("delete", key(Keys.DELETE))
        .put("semicolon", key(Keys.SEMICOLON))
        .put("equals", key(Keys.EQUALS))
        .put("numpad0", key(Keys.NUMPAD0))
        .put("numpad1", key(Keys.NUMPAD1))
        .put("numpad2", key(Keys.NUMPAD2))
        .put("numpad3", key(Keys.NUMPAD3))
        .put("numpad4", key(Keys.NUMPAD4))
        .put("numpad5", key(Keys.NUMPAD5))
        .put("numpad6", key(Keys.NUMPAD6))
        .put("numpad7", key(Keys.NUMPAD7))
        .put("numpad8", key(Keys.NUMPAD8))
        .put("numpad9", key(Keys.NUMPAD9))
        .put("multiply", key(Keys.MULTIPLY))
        .put("add", key(Keys.ADD))
        .put("separator", key(Keys.SEPARATOR))
        .put("subtract", key(Keys.SUBTRACT))
        .put("decimal", key(Keys.DECIMAL))
        .put("divide", key(Keys.DIVIDE))
        .put("f1", key(Keys.F1))
        .put("f2", key(Keys.F2))
        .put("f3", key(Keys.F3))
        .put("f4", key(Keys.F4))
        .put("f5", key(Keys.F5))
        .put("f6", key(Keys.F6))
        .put("f7", key(Keys.F7))
        .put("f8", key(Keys.F8))
        .put("f9", key(Keys.F9))
        .put("f10", key(Keys.F10))
        .put("f11", key(Keys.F11))
        .put("f12", key(Keys.F12))
        .put("meta", key(Keys.META))
        .put("cmd", key(Keys.COMMAND))
        .put("zenkakuhankaku", key(Keys.ZENKAKU_HANKAKU))
        .build();
  }
  
  private static ZwlValue key(Keys k) {
    return new StringZwlValue(k.toString()); // toString gets the char keyCode as string
  }
}
