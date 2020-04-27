package com.zylitics.btbr.runner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Wrapper for better unit test I/O
 */
public class IOWrapper {
  
  public void createDirectory(Path dir) throws IOException {
    Files.createDirectory(dir);
  }
}
