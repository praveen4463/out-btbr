package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.model.Build;

import java.util.Optional;

public interface BuildProvider {
  
  Optional<Build> getBuild(int buildId);
  
  /** Updates the build upon completion */
  int updateOnComplete(BuildUpdateOnComplete buildUpdateOnComplete);
}
