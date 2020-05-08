package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.ShotMetadata;
import org.elasticsearch.client.RestHighLevelClient;

public interface ShotMetadataProvider extends BulkSaveProvider<ShotMetadata> {
  
  @Override
  default boolean throwIfTurnedDown() {
    return false;
  }
  
  @Override
  default boolean throwOnException() {
    return false;
  }
  
  interface Factory {
    
    ShotMetadataProvider create(APICoreProperties apiCoreProperties, RestHighLevelClient client);
  }
}
