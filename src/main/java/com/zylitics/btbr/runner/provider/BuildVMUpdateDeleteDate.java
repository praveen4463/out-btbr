package com.zylitics.btbr.runner.provider;

import com.google.common.base.Preconditions;

import java.time.OffsetDateTime;

public class BuildVMUpdateDeleteDate {
  
  private final int buildVMId;
  
  private final OffsetDateTime deleteDate;
  
  public BuildVMUpdateDeleteDate(int buildVMId, OffsetDateTime deleteDate) {
    Preconditions.checkArgument(buildVMId > 0, "buildVMId is required");
    Preconditions.checkNotNull(deleteDate, "deleteDate can't be null");
    
    this.buildVMId = buildVMId;
    this.deleteDate = deleteDate;
  }
  
  public int getBuildVMId() {
    return buildVMId;
  }
  
  public OffsetDateTime getDeleteDate() {
    return deleteDate;
  }
  
  @Override
  public String toString() {
    return "BuildVMUpdateDeleteDate{" +
        "buildVMId=" + buildVMId +
        ", deleteDate=" + deleteDate +
        '}';
  }
}
