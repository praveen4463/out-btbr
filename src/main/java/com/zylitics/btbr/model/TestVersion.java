package com.zylitics.btbr.model;

public class TestVersion {

  private int testVersionId;
  
  private String name;
  
  private String code;
  
  private Test test;
  
  private File file;
  
  public int getTestVersionId() {
    return testVersionId;
  }
  
  public TestVersion setTestVersionId(int testVersionId) {
    this.testVersionId = testVersionId;
    return this;
  }
  
  public String getName() {
    return name;
  }
  
  public TestVersion setName(String name) {
    this.name = name;
    return this;
  }
  
  public String getCode() {
    return code;
  }
  
  public TestVersion setCode(String code) {
    this.code = code;
    return this;
  }
  
  public Test getTest() {
    return test;
  }
  
  public TestVersion setTest(Test test) {
    this.test = test;
    return this;
  }
  
  public File getFile() {
    return file;
  }
  
  public TestVersion setFile(File file) {
    this.file = file;
    return this;
  }
  
  @Override
  public String toString() {
    return "TestVersion{" +
        "testVersionId=" + testVersionId +
        ", name='" + name + '\'' +
        ", code='" + code + '\'' +
        ", test=" + test +
        ", file=" + file +
        '}';
  }
}
