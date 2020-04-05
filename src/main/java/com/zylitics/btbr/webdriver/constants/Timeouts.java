package com.zylitics.btbr.webdriver.constants;

import com.google.common.collect.ImmutableMap;
import com.zylitics.btbr.webdriver.TimeoutType;
import com.zylitics.zwl.datatype.StringZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;

import java.util.Map;

public class Timeouts {
  
  public static Map<String, ZwlValue> asMap() {
    return ImmutableMap.<String, ZwlValue>builder()
        .put("elemAccess", new StringZwlValue(TimeoutType.ELEMENT_ACCESS.name()))
        .put("pageLoad", new StringZwlValue(TimeoutType.PAGE_LOAD.name()))
        .put("js", new StringZwlValue(TimeoutType.JAVASCRIPT.name()))
        .build();
  }
}
