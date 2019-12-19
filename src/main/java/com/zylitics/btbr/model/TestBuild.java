package com.zylitics.btbr.model;

public class TestBuild {

  private int testBuildId;
  
  private int testVersionId;
  
  private int buildId;
  
  public int getTestBuildId() {
    return testBuildId;
  }
  
  public TestBuild setTestBuildId(int testBuildId) {
    this.testBuildId = testBuildId;
    return this;
  }
  
  public int getTestVersionId() {
    return testVersionId;
  }
  
  public TestBuild setTestVersionId(int testVersionId) {
    this.testVersionId = testVersionId;
    return this;
  }
  
  public int getBuildId() {
    return buildId;
  }
  
  public TestBuild setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
}
