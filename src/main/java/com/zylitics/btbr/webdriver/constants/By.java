package com.zylitics.btbr.webdriver.constants;

import com.google.common.collect.ImmutableMap;
import com.zylitics.zwl.datatype.StringZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;

import java.util.Map;

public class By {
  
  public static Map<String, ZwlValue> asMap() {
    return ImmutableMap.<String, ZwlValue>builder()
        .put("id", new StringZwlValue(ByType.ID.name()))
        .put("lt", new StringZwlValue(ByType.LINK_TEXT.name()))
        .put("plt", new StringZwlValue(ByType.PARTIAL_LINK_TEXT.name()))
        .put("name", new StringZwlValue(ByType.NAME.name()))
        .put("tn", new StringZwlValue(ByType.TAG_NAME.name()))
        .put("xp", new StringZwlValue(ByType.XPATH.name()))
        .put("cn", new StringZwlValue(ByType.CLASS_NAME.name()))
        .put("cs", new StringZwlValue(ByType.CSS_SELECTOR.name()))
        .build();
  }
}
