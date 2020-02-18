package com.zylitics.btbr.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class Assert {
  
  public static void notEmpty(String s, String error) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(s), error);
  }
}
