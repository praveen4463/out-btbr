package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.model.Build;

import java.time.LocalDateTime;

public interface QuotaProvider {
  
  /** Updates the Quota upon all tasks completion */
  int updateConsumed(Build build, LocalDateTime currentUTC);
}
