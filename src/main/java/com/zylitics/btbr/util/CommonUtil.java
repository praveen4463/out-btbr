package com.zylitics.btbr.util;

import org.springframework.jdbc.core.RowMapper;

public class CommonUtil {
  
  public static RowMapper<Integer> getSingleInt() {
    return ((rs, rowNum) -> rs.getInt(1));
  }
}
