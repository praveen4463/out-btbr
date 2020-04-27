package com.zylitics.btbr.model;

import java.time.ZonedDateTime;

public class BuildVM {

  private int buildVMId;
  
  private ZonedDateTime deleteDate;
  
  public int getBuildVMId() {
    return buildVMId;
  }
  
  public BuildVM setBuildVMId(int buildVMId) {
    this.buildVMId = buildVMId;
    return this;
  }
  
  public ZonedDateTime getDeleteDate() {
    return deleteDate;
  }
  
  public BuildVM setDeleteDate(ZonedDateTime deleteDate) {
    this.deleteDate = deleteDate;
    return this;
  }
  
  @Override
  public String toString() {
    return "BuildVM{" +
        "buildVMId=" + buildVMId +
        ", deleteDate=" + deleteDate +
        '}';
  }
}
