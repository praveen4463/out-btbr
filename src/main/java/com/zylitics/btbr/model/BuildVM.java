package com.zylitics.btbr.model;

public class BuildVM {
  
  private String name;
  
  private String zone;
  
  private boolean deleteFromRunner;
  
  public String getName() {
    return name;
  }
  
  public BuildVM setName(String name) {
    this.name = name;
    return this;
  }
  
  public String getZone() {
    return zone;
  }
  
  public BuildVM setZone(String zone) {
    this.zone = zone;
    return this;
  }
  
  public boolean isDeleteFromRunner() {
    return deleteFromRunner;
  }
  
  public BuildVM setDeleteFromRunner(boolean deleteFromRunner) {
    this.deleteFromRunner = deleteFromRunner;
    return this;
  }
}
