package com.zylitics.btbr.model;

import com.zylitics.btbr.runner.TestStatus;

import java.time.LocalDateTime;

public class Build {

  private int buildId;
  
  private String buildKey;
  
  private String buildName;
  
  private BuildCapability buildCapability;
  
  private int buildVMId;
  
  private String serverScreenSize;
  
  private String serverTimezone;
  
  // The reason why we store date in LocalDateTime rather than OffsetDateTime is, we send a
  // timestamp to postgres in UTC and it stores it as is. While retrieving, the timestamp is
  // converted to a particular time zone (for example the zone user is currently in or has selected)
  // After conversion the timestamp doesn't technically represent a timezone and is converted to a
  // local date time.
  private LocalDateTime createDateUTC;
  
  private TestStatus finalStatus;
  
  private String shotBucketSessionStorage;
  
  private boolean abortOnFailure;
  
  private int retryFailedTestsUpto;
  
  private boolean captureShots;
  
  private boolean captureDriverLogs;
  
  private boolean notifyOnCompletion;
  
  private boolean aetKeepSingleWindow;
  
  private boolean aetUpdateUrlBlank;
  
  private boolean aetResetTimeouts;
  
  private boolean aetDeleteAllCookies;
  
  private int userId;
  
  private Project project;
  
  private Organization organization;
  
  private BuildSourceType sourceType;
  
  private long buildRequestId;
  
  public int getBuildId() {
    return buildId;
  }
  
  public Build setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  public String getBuildKey() {
    return buildKey;
  }
  
  public Build setBuildKey(String buildKey) {
    this.buildKey = buildKey;
    return this;
  }
  
  public String getBuildName() {
    return buildName;
  }
  
  public Build setBuildName(String buildName) {
    this.buildName = buildName;
    return this;
  }
  
  public BuildCapability getBuildCapability() {
    return buildCapability;
  }
  
  public Build setBuildCapability(BuildCapability buildCapability) {
    this.buildCapability = buildCapability;
    return this;
  }
  
  public int getBuildVMId() {
    return buildVMId;
  }
  
  public Build setBuildVMId(int buildVMId) {
    this.buildVMId = buildVMId;
    return this;
  }
  
  public String getServerScreenSize() {
    return serverScreenSize;
  }
  
  public Build setServerScreenSize(String serverScreenSize) {
    this.serverScreenSize = serverScreenSize;
    return this;
  }
  
  public String getServerTimezone() {
    return serverTimezone;
  }
  
  public Build setServerTimezone(String serverTimezone) {
    this.serverTimezone = serverTimezone;
    return this;
  }
  
  public LocalDateTime getCreateDateUTC() {
    return createDateUTC;
  }
  
  public Build setCreateDateUTC(LocalDateTime createDateUTC) {
    this.createDateUTC = createDateUTC;
    return this;
  }
  
  public TestStatus getFinalStatus() {
    return finalStatus;
  }
  
  public Build setFinalStatus(TestStatus finalStatus) {
    this.finalStatus = finalStatus;
    return this;
  }
  
  public String getShotBucketSessionStorage() {
    return shotBucketSessionStorage;
  }
  
  public Build setShotBucketSessionStorage(String shotBucketSessionStorage) {
    this.shotBucketSessionStorage = shotBucketSessionStorage;
    return this;
  }
  
  public boolean isAbortOnFailure() {
    return abortOnFailure;
  }
  
  public Build setAbortOnFailure(boolean abortOnFailure) {
    this.abortOnFailure = abortOnFailure;
    return this;
  }
  
  public int getRetryFailedTestsUpto() {
    return retryFailedTestsUpto;
  }
  
  public Build setRetryFailedTestsUpto(int retryFailedTestsUpto) {
    this.retryFailedTestsUpto = retryFailedTestsUpto;
    return this;
  }
  
  public boolean isCaptureShots() {
    return captureShots;
  }
  
  public Build setCaptureShots(boolean captureShots) {
    this.captureShots = captureShots;
    return this;
  }
  
  public boolean isCaptureDriverLogs() {
    return captureDriverLogs;
  }
  
  public Build setCaptureDriverLogs(boolean captureDriverLogs) {
    this.captureDriverLogs = captureDriverLogs;
    return this;
  }
  
  public boolean isNotifyOnCompletion() {
    return notifyOnCompletion;
  }
  
  public Build setNotifyOnCompletion(boolean notifyOnCompletion) {
    this.notifyOnCompletion = notifyOnCompletion;
    return this;
  }
  
  public boolean isAetKeepSingleWindow() {
    return aetKeepSingleWindow;
  }
  
  public Build setAetKeepSingleWindow(boolean aetKeepSingleWindow) {
    this.aetKeepSingleWindow = aetKeepSingleWindow;
    return this;
  }
  
  public boolean isAetUpdateUrlBlank() {
    return aetUpdateUrlBlank;
  }
  
  public Build setAetUpdateUrlBlank(boolean aetUpdateUrlBlank) {
    this.aetUpdateUrlBlank = aetUpdateUrlBlank;
    return this;
  }
  
  public boolean isAetResetTimeouts() {
    return aetResetTimeouts;
  }
  
  public Build setAetResetTimeouts(boolean aetResetTimeouts) {
    this.aetResetTimeouts = aetResetTimeouts;
    return this;
  }
  
  public boolean isAetDeleteAllCookies() {
    return aetDeleteAllCookies;
  }
  
  public Build setAetDeleteAllCookies(boolean aetDeleteAllCookies) {
    this.aetDeleteAllCookies = aetDeleteAllCookies;
    return this;
  }
  
  public int getUserId() {
    return userId;
  }
  
  public Build setUserId(int userId) {
    this.userId = userId;
    return this;
  }
  
  public Project getProject() {
    return project;
  }
  
  public Build setProject(Project project) {
    this.project = project;
    return this;
  }
  
  public Organization getOrganization() {
    return organization;
  }
  
  public Build setOrganization(Organization organization) {
    this.organization = organization;
    return this;
  }
  
  public BuildSourceType getSourceType() {
    return sourceType;
  }
  
  public Build setSourceType(BuildSourceType sourceType) {
    this.sourceType = sourceType;
    return this;
  }
  
  public long getBuildRequestId() {
    return buildRequestId;
  }
  
  public Build setBuildRequestId(long buildRequestId) {
    this.buildRequestId = buildRequestId;
    return this;
  }
  
  @Override
  public String toString() {
    return "Build{" +
        "buildId=" + buildId +
        ", buildKey='" + buildKey + '\'' +
        ", buildCapability=" + buildCapability +
        ", buildVMId=" + buildVMId +
        ", createDateUTC=" + createDateUTC +
        ", finalStatus=" + finalStatus +
        ", shotBucketSessionStorage='" + shotBucketSessionStorage + '\'' +
        ", abortOnFailure=" + abortOnFailure +
        ", aetKeepSingleWindow=" + aetKeepSingleWindow +
        ", aetUpdateUrlBlank=" + aetUpdateUrlBlank +
        ", aetResetTimeouts=" + aetResetTimeouts +
        ", aetDeleteAllCookies=" + aetDeleteAllCookies +
        ", userId=" + userId +
        ", sourceType=" + sourceType +
        ", buildRequestId=" + buildRequestId +
        '}';
  }
}
