package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.model.BuildCommandResult;

public interface BuildCommandResultProvider extends BulkSaveProvider<BuildCommandResult> {
  
  /** This should be invoked, before using any method of this interface. */
  void setBuildCapability(BuildCapability buildCapability);
  
  @Override
  default boolean throwIfTurnedDown() {
    return true;
  }
  
  @Override
  default boolean throwOnException() {
    return true;
  }
}
