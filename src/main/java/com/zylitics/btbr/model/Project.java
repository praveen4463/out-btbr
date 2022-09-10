package com.zylitics.btbr.model;

public class Project {
  
  private int projectId;
  
  private boolean gitEnabled;
  
  private GitProvider gitProvider;
  
  public int getProjectId() {
    return projectId;
  }
  
  public Project setProjectId(int projectId) {
    this.projectId = projectId;
    return this;
  }
  
  public boolean isGitEnabled() {
    return gitEnabled;
  }
  
  public Project setGitEnabled(boolean gitEnabled) {
    this.gitEnabled = gitEnabled;
    return this;
  }
  
  public GitProvider getGitProvider() {
    return gitProvider;
  }
  
  public Project setGitProvider(GitProvider gitProvider) {
    this.gitProvider = gitProvider;
    return this;
  }
}
