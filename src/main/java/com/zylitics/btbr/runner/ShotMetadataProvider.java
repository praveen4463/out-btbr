package com.zylitics.btbr.runner;

import com.zylitics.btbr.model.ShotMetadata;

public interface ShotMetadataProvider extends BulkSaveProvider<ShotMetadata> {
  
  @Override
  default boolean throwIfTurnedDown() {
    return false;
  }
  
  @Override
  default boolean throwOnException() {
    return false;
  }
}
