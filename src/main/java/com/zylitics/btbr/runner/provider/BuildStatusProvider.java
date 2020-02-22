package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.model.BuildStatus;

public interface BuildStatusProvider {
  
  /** Returns the number of rows affected. */
  int save(BuildStatus buildStatus);
  
  /** Updates only the zwl-line-number using build and testVersion.*/
  int updateLine(BuildStatus buildStatus);
  
  /** Updates all updatable fields using build and testVersion.*/
  int update(BuildStatus buildStatus);
}
