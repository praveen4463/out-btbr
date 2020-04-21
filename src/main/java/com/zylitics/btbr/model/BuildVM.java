package com.zylitics.btbr.model;

import java.time.ZonedDateTime;

public class BuildVM {

  private int buildId;
  
  private ZonedDateTime deleteDate;
  
  public int getBuildId() {
    return buildId;
  }
  
  public BuildVM setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  public ZonedDateTime getDeleteDate() {
    return deleteDate;
  }
  
  public BuildVM setDeleteDate(ZonedDateTime deleteDate) {
    this.deleteDate = deleteDate;
    return this;
  }
}
