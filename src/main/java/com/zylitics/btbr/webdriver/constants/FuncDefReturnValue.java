package com.zylitics.btbr.webdriver.constants;

import com.google.common.collect.ImmutableList;
import com.zylitics.zwl.datatype.ListZwlValue;
import com.zylitics.zwl.datatype.StringZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;

public enum FuncDefReturnValue {
  
  ELEMENT_ID (D.E1),
  ELEMENT_IDS(new ListZwlValue(ImmutableList.of(D.E1, D.E2)))
  ;
  
  
  
  private final ZwlValue defValue;
  
  FuncDefReturnValue(ZwlValue defValue) {
    this.defValue = defValue;
  }
  
  public ZwlValue getDefValue() {
    return defValue;
  }
  
  // lists all default values available in a separate class because enum static fields are
  // initialized after the own statics of enum and can't reference other static fields.
  // https://stackoverflow.com/a/444000/1624454
  private static class D {
  
    private static final ZwlValue E1 = new StringZwlValue("44b68dc3-9788-4b86-9d4c-3450e3727d19");
    private static final ZwlValue E2 = new StringZwlValue("c64e8a6a-5cE6-4ef0-9ae0-fdfd45a7a326");
  }
}
