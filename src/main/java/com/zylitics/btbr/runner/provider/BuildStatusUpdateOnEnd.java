package com.zylitics.btbr.runner.provider;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.runner.TestStatus;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Objects;

public class BuildStatusUpdateOnEnd extends AbstractBuildStatus {
  
  private final TestStatus status;
  
  private final OffsetDateTime endDate;
  
  private final String error;
  
  private final String errorFromPos;
  
  private final String errorToPos;
  
  private final String urlUponError;
  
  public BuildStatusUpdateOnEnd(int buildId, int testVersionId, TestStatus status,
                                OffsetDateTime endDate, @Nullable String error,
                                @Nullable String errorFromPos, @Nullable String errorToPos,
                                @Nullable String urlUponError) {
    super(buildId, testVersionId);
    Preconditions.checkNotNull(status, "status can't be null");
    Preconditions.checkNotNull(endDate, "endDate can't be null");
    
    this.status = status;
    this.endDate = endDate;
    this.error = error;
    this.errorFromPos = errorFromPos;
    this.errorToPos = errorToPos;
    this.urlUponError = urlUponError;
  }
  
  public BuildStatusUpdateOnEnd(int buildId, int testVersionId, TestStatus status,
                                OffsetDateTime endDate) {
    this(buildId, testVersionId, status, endDate, null, null, null, null);
  }
  
  public TestStatus getStatus() {
    return status;
  }
  
  public OffsetDateTime getEndDate() {
    return endDate;
  }
  
  public String getError() {
    return error;
  }
  
  public String getErrorFromPos() {
    return errorFromPos;
  }
  
  public String getErrorToPos() {
    return errorToPos;
  }
  
  public String getUrlUponError() {
    return urlUponError;
  }
  
  @Override
  public String toString() {
    return "BuildStatusUpdateOnEnd{" +
        "status=" + status +
        ", endDate=" + endDate +
        ", error='" + error + '\'' +
        ", errorFromPos='" + errorFromPos + '\'' +
        ", errorToPos='" + errorToPos + '\'' +
        ", urlUponError='" + urlUponError + '\'' +
        "} " + super.toString();
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    BuildStatusUpdateOnEnd that = (BuildStatusUpdateOnEnd) o;
    return status == that.status &&
        endDate.equals(that.endDate) &&
        Objects.equals(error, that.error) &&
        Objects.equals(errorFromPos, that.errorFromPos) &&
        Objects.equals(errorToPos, that.errorToPos) &&
        Objects.equals(urlUponError, that.urlUponError);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), status, endDate, error, errorFromPos, errorToPos,
        urlUponError);
  }
}