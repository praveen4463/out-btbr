package com.zylitics.btbr.shot;

import com.google.cloud.storage.Storage;
import com.zylitics.btbr.config.APICoreProperties;

import java.io.InputStream;

interface ShotCloudStore {
  
  /**
   *
   * @param name the name/key of the shot taken
   * @param stream the stream length and stream itself of the taken shot
   * @return boolean indicating whether shot was saved.
   */
  boolean storeShot(String name, InputStream stream);
  
  interface Factory {
    
    static Factory getDefault() {
      return new GCPShotCloudStore.Factory();
    }
    
    ShotCloudStore create(String bucket, APICoreProperties.Shot shotProps, Storage storage);
  }
}
