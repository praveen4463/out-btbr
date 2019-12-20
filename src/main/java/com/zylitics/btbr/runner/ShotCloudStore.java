package com.zylitics.btbr.runner;

import com.zylitics.btbr.model.BuildCapability;

import java.io.InputStream;

public interface ShotCloudStore {
  
  /** Should be set before using any methods. */
  void setBuildCapability(BuildCapability buildCapability);
  
  /**
   *
   * @param name the name/key of the shot taken
   * @param stream the stream length and stream itself of the taken shot
   * @return boolean indicating whether shot was saved.
   */
  boolean storeShot(String name, InputStream stream);
}
