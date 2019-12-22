package com.zylitics.btbr.shot;

import java.io.InputStream;

interface CaptureDevice {
  
  /**
   * <p>This will load several classes from Java AWT package in the running VM, thus
   * saving the efforts that would otherwise have taken time during client session.</p>
   * <p>!! Run this once during lifetime of JVM.</p>
   */
  default void init() {}
  
  /**
   * captures the entire screen of the computer.
   * @return {@link Result} containing the extension of shot and an {@link InputStream} to read it.
   */
  Result captureFull() throws Exception;
  
  /**
   * captures only the graphically visible part excluding the CHROME of browser/OS.
   * @return {@link Result} containing the extension of shot and an {@link InputStream} to read it.
   */
  @SuppressWarnings("unused")
  Result captureVisible() throws Exception;
  
  interface Result {
    
    /**
     * should always return a stream that doesn't require closing.
     */
    InputStream getShotInputStream();
  }
  
  interface Factory {
    
    static Factory getDefault() {
      return new JavaRobotCaptureDevice.Factory();
    }
    
    CaptureDevice create(String shotExtension);
  }
}
