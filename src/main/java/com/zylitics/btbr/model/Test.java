package com.zylitics.btbr.model;

public class Test {
  
  private int testId;
  
  private String name;
  
  public int getTestId() {
    return testId;
  }
  
  public Test setTestId(int testId) {
    this.testId = testId;
    return this;
  }
  
  public String getName() {
    return name;
  }
  
  public Test setName(String name) {
    this.name = name;
    return this;
  }
  
  @Override
  public String toString() {
    return "Test{" +
        "testId=" + testId +
        ", name='" + name + '\'' +
        '}';
  }
}
