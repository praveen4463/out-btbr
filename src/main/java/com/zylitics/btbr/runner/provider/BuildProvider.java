package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.model.Build;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface BuildProvider {
  
  Optional<Build> getBuild(int buildId);
  
  /** Updates the build upon completion */
  int updateOnStart(int buildId, OffsetDateTime startDate);
  
  /** Updates the build upon completion */
  int updateOnComplete(BuildUpdateOnComplete buildUpdateOnComplete);
  
  /** Updates the build upon all tasks completion */
  int updateOnAllTasksDone(int buildId, OffsetDateTime allDoneDate);
}
