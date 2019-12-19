package com.zylitics.btbr.runner;

import java.io.InputStream;

public interface ShotCloudStore {
  
  /**
   *
   * @param name the name/key of the shot taken
   * @param stream the stream length and stream itself of the taken shot
   * @return boolean indicating whether shot was saved.
   */
  boolean storeShot(String name, InputStream stream);
}
