package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.model.ZwlProgramOutput;
import org.elasticsearch.client.RestHighLevelClient;

public interface ZwlProgramOutputProvider extends BulkSaveProvider<ZwlProgramOutput> {
  
  @Override
  default boolean throwIfTurnedDown() {
    return false;
  }
  
  @Override
  default boolean throwOnException() {
    return false;
  }
  
  interface Factory {
    
    ZwlProgramOutputProvider create(APICoreProperties apiCoreProperties,
                                    RestHighLevelClient client,
                                    BuildCapability buildCapability);
  }
}
