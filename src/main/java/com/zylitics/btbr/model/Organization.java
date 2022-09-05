package com.zylitics.btbr.model;

public class Organization {
  
  private int organizationId;
  
  private boolean gitEnabled;
  
  private GitProvider gitProvider;
  
  public int getOrganizationId() {
    return organizationId;
  }
  
  public Organization setOrganizationId(int organizationId) {
    this.organizationId = organizationId;
    return this;
  }
  
  public boolean isGitEnabled() {
    return gitEnabled;
  }
  
  public Organization setGitEnabled(boolean gitEnabled) {
    this.gitEnabled = gitEnabled;
    return this;
  }
  
  public GitProvider getGitProvider() {
    return gitProvider;
  }
  
  public Organization setGitProvider(GitProvider gitProvider) {
    this.gitProvider = gitProvider;
    return this;
  }
}
