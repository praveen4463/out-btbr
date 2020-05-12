package com.zylitics.btbr.util;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class DateTimeUtil {
  
  // OffsetDateTime is taken rather than ZonedDateTime because PGJDBC doesn't support it,
  // OffsetDateTime also works with ESDB.
  public static OffsetDateTime getCurrentUTC() {
    return OffsetDateTime.now(ZoneId.of("UTC"));
  }
}
