package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.model.Build;

import java.util.Optional;

public interface BuildProvider {
  
  Optional<Build> getBuild(int buildId);
  
  /** Returns the number of rows affected. */
  int updateBuild(Build build);
}
