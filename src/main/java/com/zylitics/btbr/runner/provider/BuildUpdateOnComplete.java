package com.zylitics.btbr.runner.provider;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;

public class BuildUpdateOnComplete {
  
  private final int buildId;
  
  private final OffsetDateTime endDate;
  
  private final boolean isSuccess;
  
  private final String error;
  
  public BuildUpdateOnComplete(int buildId, OffsetDateTime endDate, boolean isSuccess,
                               @Nullable String error) {
    Preconditions.checkArgument(buildId > 0, "buildId is required");
    Preconditions.checkNotNull(endDate, "endDate can't be null");
    
    this.buildId = buildId;
    this.endDate = endDate;
    this.isSuccess = isSuccess;
    this.error = error;
  }
  
  public int getBuildId() {
    return buildId;
  }
  
  public OffsetDateTime getEndDate() {
    return endDate;
  }
  
  public boolean isSuccess() {
    return isSuccess;
  }
  
  public String getError() {
    return error;
  }
  
  @Override
  public String toString() {
    return "BuildUpdateOnComplete{" +
        "buildId=" + buildId +
        ", endDate=" + endDate +
        ", isSuccess=" + isSuccess +
        ", error='" + error + '\'' +
        '}';
  }
}
