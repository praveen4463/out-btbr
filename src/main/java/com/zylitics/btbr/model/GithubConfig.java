package com.zylitics.btbr.model;

public class GithubConfig {

  private String apiToken;
  
  private String repoOwner;
  
  private String repoName;
  
  private String mainBranchName;
  
  public String getApiToken() {
    return apiToken;
  }
  
  public GithubConfig setApiToken(String apiToken) {
    this.apiToken = apiToken;
    return this;
  }
  
  public String getRepoOwner() {
    return repoOwner;
  }
  
  public GithubConfig setRepoOwner(String repoOwner) {
    this.repoOwner = repoOwner;
    return this;
  }
  
  public String getRepoName() {
    return repoName;
  }
  
  public GithubConfig setRepoName(String repoName) {
    this.repoName = repoName;
    return this;
  }
  
  public String getMainBranchName() {
    return mainBranchName;
  }
  
  public GithubConfig setMainBranchName(String mainBranchName) {
    this.mainBranchName = mainBranchName;
    return this;
  }
}
