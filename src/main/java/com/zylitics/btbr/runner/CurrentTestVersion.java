package com.zylitics.btbr.runner;

/**
 * Test version that is currently running by runner. The fields contained in it are used mostly
 * for storing state information of test in db, for example these can be attached with screenshots
 * so that each shot is tagged with the version of test and current line executing in program of
 * version when a particular shot is taken.
 */
public class CurrentTestVersion {
  
  private int testVersionId;
  
  private int controlAtLineInProgram;
  
  public int getTestVersionId() {
    return testVersionId;
  }
  
  public CurrentTestVersion setTestVersionId(int testVersionId) {
    this.testVersionId = testVersionId;
    return this;
  }
  
  public int getControlAtLineInProgram() {
    return controlAtLineInProgram;
  }
  
  public CurrentTestVersion setControlAtLineInProgram(int controlAtLineInProgram) {
    this.controlAtLineInProgram = controlAtLineInProgram;
    return this;
  }
  
  @Override
  public String toString() {
    return "CurrentTestVersion{" +
        "testVersionId=" + testVersionId +
        ", controlAtLineInProgram=" + controlAtLineInProgram +
        '}';
  }
}
