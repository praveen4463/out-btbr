package com.zylitics.btbr.util;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
  
  static final DateTimeFormatter DB_COMPATIBLE_DATE_TIME_FORMAT = DateTimeFormatter
      .ofPattern("yyyy-MM-dd HH:mm:ss.SSS zzz");
  
  public static OffsetDateTime getCurrentUTC() {
    return OffsetDateTime.now(ZoneId.of("UTC"));
  }
  
  public static String formatDb(ZonedDateTime zonedDateTime) {
    return zonedDateTime.format(DB_COMPATIBLE_DATE_TIME_FORMAT);
  }
}
