package com.zylitics.btbr.runner.provider;

import com.google.common.base.Preconditions;

public class BuildStatusUpdateLine {
  
  private final int buildId;
  
  private final int testVersionId;
  
  private final int zwlExecutingLine;
  
  public BuildStatusUpdateLine(int buildId, int testVersionId, int zwlExecutingLine) {
    Preconditions.checkArgument(buildId > 0, "buildId is required");
    Preconditions.checkArgument(testVersionId > 0, "testVersionId is required");
    
    this.buildId = buildId;
    this.testVersionId = testVersionId;
    this.zwlExecutingLine = zwlExecutingLine;
  }
  
  public int getBuildId() {
    return buildId;
  }
  
  public int getTestVersionId() {
    return testVersionId;
  }
  
  public int getZwlExecutingLine() {
    return zwlExecutingLine;
  }
  
  @Override
  public String toString() {
    return "BuildStatusUpdateLine{" +
        "buildId=" + buildId +
        ", testVersionId=" + testVersionId +
        ", zwlExecutingLine=" + zwlExecutingLine +
        '}';
  }
}