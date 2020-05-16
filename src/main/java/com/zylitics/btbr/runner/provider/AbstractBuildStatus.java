package com.zylitics.btbr.runner.provider;

import com.google.common.base.Preconditions;

import java.util.Objects;

abstract class AbstractBuildStatus {
  
  private final int buildId;
  
  private final int testVersionId;
  
  AbstractBuildStatus(int buildId, int testVersionId) {
    Preconditions.checkArgument(buildId > 0, "buildId is required");
    Preconditions.checkArgument(testVersionId > 0, "testVersionId is required");
  
    this.buildId = buildId;
    this.testVersionId = testVersionId;
  }
  
  public int getBuildId() {
    return buildId;
  }
  
  public int getTestVersionId() {
    return testVersionId;
  }
  
  @Override
  public String toString() {
    return "AbstractBuildStatus{" +
        "buildId=" + buildId +
        ", testVersionId=" + testVersionId +
        '}';
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AbstractBuildStatus that = (AbstractBuildStatus) o;
    return buildId == that.buildId &&
        testVersionId == that.testVersionId;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(buildId, testVersionId);
  }
}
