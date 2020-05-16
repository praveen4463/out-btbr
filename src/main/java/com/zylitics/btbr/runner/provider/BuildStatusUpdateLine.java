package com.zylitics.btbr.runner.provider;

import java.util.Objects;

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
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    BuildStatusUpdateLine that = (BuildStatusUpdateLine) o;
    return zwlExecutingLine == that.zwlExecutingLine;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), zwlExecutingLine);
  }
}