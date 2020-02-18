package com.zylitics.btbr.webdriver;

public class BrwExtCmdDef {
  
  private String command;
  
  private String target;
  
  private String value;
  
  private int index;
  
  public String getCommand() {
    return command;
  }
  
  public BrwExtCmdDef setCommand(String command) {
    this.command = command;
    return this;
  }
  
  public String getTarget() {
    return target;
  }
  
  public BrwExtCmdDef setTarget(String target) {
    this.target = target;
    return this;
  }
  
  public String getValue() {
    return value;
  }
  
  public BrwExtCmdDef setValue(String value) {
    this.value = value;
    return this;
  }
  
  public int getIndex() {
    return index;
  }
  
  public BrwExtCmdDef setIndex(int index) {
    this.index = index;
    return this;
  }
  
  @Override
  public String toString() {
    return "BrwExtCmdDef{" +
        "command='" + command + '\'' +
        ", target='" + target + '\'' +
        ", value='" + value + '\'' +
        '}';
  }
}
