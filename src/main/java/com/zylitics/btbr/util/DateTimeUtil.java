package com.zylitics.btbr.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateTimeUtil {
  
  public static ZonedDateTime getCurrentUTC() {
    return ZonedDateTime.now(ZoneId.of("UTC"));
  }
}
