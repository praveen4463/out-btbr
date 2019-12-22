package com.zylitics.btbr.model;

import java.util.List;

public class SuiteTestBuild {
  
  private int suiteTestBuildId;
  
  private List<Integer> testVersionIds;
  
  private int buildId;
  
  public int getSuiteTestBuildId() {
    return suiteTestBuildId;
  }
  
  public SuiteTestBuild setSuiteTestBuildId(int suiteTestBuildId) {
    this.suiteTestBuildId = suiteTestBuildId;
    return this;
  }
  
  public List<Integer> getTestVersionIds() {
    return testVersionIds;
  }
  
  public SuiteTestBuild setTestVersionIds(List<Integer> testVersionIds) {
    this.testVersionIds = testVersionIds;
    return this;
  }
  
  public int getBuildId() {
    return buildId;
  }
  
  public SuiteTestBuild setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  @Override
  public String toString() {
    return "SuiteTestBuild{" +
        "suiteTestBuildId=" + suiteTestBuildId +
        ", testVersionIds=" + testVersionIds +
        ", buildId=" + buildId +
        '}';
  }
}
