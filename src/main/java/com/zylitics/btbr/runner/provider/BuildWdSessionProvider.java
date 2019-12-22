package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.model.BuildWdSession;

public interface BuildWdSessionProvider {
  
  /** Returns the number of rows affected. */
  int updateBuildWdSession(BuildWdSession buildWdSession);
}
