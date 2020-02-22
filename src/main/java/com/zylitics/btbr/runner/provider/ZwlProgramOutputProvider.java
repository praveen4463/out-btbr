package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.model.ZwlProgramOutput;

public interface ZwlProgramOutputProvider extends BulkSaveProvider<ZwlProgramOutput> {
  
  /** This should be invoked, before using any method of this interface. */
  void setBuildCapability(BuildCapability buildCapability);
  
  @Override
  default boolean throwIfTurnedDown() {
    return false;
  }
  
  @Override
  default boolean throwOnException() {
    return false;
  }
}
