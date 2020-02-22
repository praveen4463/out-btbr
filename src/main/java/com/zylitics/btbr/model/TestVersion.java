package com.zylitics.btbr.model;

public class TestVersion {

  private int testVersionId;
  
  private ZwlProgram zwlProgram;
  
  public int getTestVersionId() {
    return testVersionId;
  }
  
  public TestVersion setTestVersionId(int testVersionId) {
    this.testVersionId = testVersionId;
    return this;
  }
  
  public ZwlProgram getZwlProgram() {
    return zwlProgram;
  }
  
  public TestVersion setZwlProgram(ZwlProgram zwlProgram) {
    this.zwlProgram = zwlProgram;
    return this;
  }
  
  @Override
  public String toString() {
    return "TestVersion{" +
        "testVersionId=" + testVersionId +
        ", zwlProgram=" + zwlProgram +
        '}';
  }
}
