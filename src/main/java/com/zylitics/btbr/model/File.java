package com.zylitics.btbr.model;

public class File {
  
  private int fileId;
  
  private String name;
  
  public int getFileId() {
    return fileId;
  }
  
  public File setFileId(int fileId) {
    this.fileId = fileId;
    return this;
  }
  
  public String getName() {
    return name;
  }
  
  public File setName(String name) {
    this.name = name;
    return this;
  }
  
  @Override
  public String toString() {
    return "File{" +
        "fileId=" + fileId +
        ", name='" + name + '\'' +
        '}';
  }
}
