package com.zylitics.btbr.util;

import java.io.IOException;

public class IOUtil {
  
  /**
   * Creates a file of specified name in the given dir and writes the given bytes into it.
   * If the file already exists in dir, appends some random identifier to the file name and tries
   * saving again.
   */
  // TODO: work on this while working on saving logs, as this will go into the same location where
  // logs are saved and will then go into cloud storage on build completion at some accessible
  // location for user.
  public static void write(byte[] data, String file, String dir) throws IOException {
  
  }
}
