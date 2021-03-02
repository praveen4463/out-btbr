package com.zylitics.btbr.runner.provider;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.runner.TestStatus;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Objects;

public class BuildUpdateOnComplete {
  
  private final int buildId;
  
  private final OffsetDateTime endDate;
  
  private final TestStatus finalStatus;
  
  private final String error;
  
  public BuildUpdateOnComplete(int buildId, OffsetDateTime endDate, TestStatus finalStatus,
                               @Nullable String error) {
    Preconditions.checkArgument(buildId > 0, "buildId is required");
    Preconditions.checkNotNull(endDate, "endDate can't be null");
    
    this.buildId = buildId;
    this.endDate = endDate;
    this.finalStatus = finalStatus;
    this.error = error;
  }
  
  public int getBuildId() {
    return buildId;
  }
  
  public OffsetDateTime getEndDate() {
    return endDate;
  }
  
  public TestStatus getFinalStatus() {
    return finalStatus;
  }
  
  public String getError() {
    return error;
  }
  
  @Override
  public String toString() {
    return "BuildUpdateOnComplete{" +
        "buildId=" + buildId +
        ", endDate=" + endDate +
        ", finalStatus=" + finalStatus +
        ", error='" + error + '\'' +
        '}';
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BuildUpdateOnComplete that = (BuildUpdateOnComplete) o;
    return buildId == that.buildId
        && endDate.equals(that.endDate)
        && finalStatus == that.finalStatus
        && Objects.equals(error, that.error);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(buildId, endDate, finalStatus, error);
  }
}
