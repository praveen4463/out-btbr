package com.zylitics.btbr.runner.provider;

public class BuildStatusUpdateLine extends AbstractBuildStatus {
  
  private final int zwlExecutingLine;
  
  public BuildStatusUpdateLine(int buildId, int testVersionId, int zwlExecutingLine) {
    super(buildId, testVersionId);
    
    this.zwlExecutingLine = zwlExecutingLine;
  }
  
  public int getZwlExecutingLine() {
    return zwlExecutingLine;
  }
  
  @Override
  public String toString() {
    return "BuildStatusUpdateLine{" +
        "zwlExecutingLine=" + zwlExecutingLine +
        "} " + super.toString();
  }
}