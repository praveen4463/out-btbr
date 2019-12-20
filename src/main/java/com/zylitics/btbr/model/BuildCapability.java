package com.zylitics.btbr.model;

public class BuildCapability {

  private int buildCapabilityId;
  
  private String shotBucketSessionStorage;
  
  private boolean shotTakeTestShot;
  
  private int commandResultFlushRecords;
  
  private long commandResultFlushMillis;
  
  private String serverScreenSize;
  
  private String serverTimeZoneWithDst;
  
  private String wdBrowserName;
  
  private String wdBrowserVersion;
  
  private String wdPlatformName;
  
  private boolean wdAcceptInsecureCerts;
  
  private String wdPageLoadStrategy;
  
  private boolean wdSetWindowRect;
  
  private int wdTimeoutsScript;
  
  private int wdTimeoutsPageLoad;
  
  private int wdTimeoutsImplicit;
  
  private boolean wdStrictFileInteractability;
  
  private String wdUnhandledPromptBehavior;
  
  private boolean brwIsFullScreen;
  
  private boolean chromeEnableNetwork;
  
  private boolean chromeEnablePage;
  
  public int getBuildCapabilityId() {
    return buildCapabilityId;
  }
  
  public BuildCapability setBuildCapabilityId(int buildCapabilityId) {
    this.buildCapabilityId = buildCapabilityId;
    return this;
  }
  
  public String getShotBucketSessionStorage() {
    return shotBucketSessionStorage;
  }
  
  public BuildCapability setShotBucketSessionStorage(String shotBucketSessionStorage) {
    this.shotBucketSessionStorage = shotBucketSessionStorage;
    return this;
  }
  
  public boolean isShotTakeTestShot() {
    return shotTakeTestShot;
  }
  
  public BuildCapability setShotTakeTestShot(boolean shotTakeTestShot) {
    this.shotTakeTestShot = shotTakeTestShot;
    return this;
  }
  
  public int getCommandResultFlushRecords() {
    return commandResultFlushRecords;
  }
  
  public BuildCapability setCommandResultFlushRecords(int commandResultFlushRecords) {
    this.commandResultFlushRecords = commandResultFlushRecords;
    return this;
  }
  
  /** Should be used only when {@link BuildCapability#getCommandResultFlushRecords()} is not given */
  public long getCommandResultFlushMillis() {
    return commandResultFlushMillis;
  }
  
  public BuildCapability setCommandResultFlushMillis(long commandResultFlushMillis) {
    this.commandResultFlushMillis = commandResultFlushMillis;
    return this;
  }
  
  public String getServerScreenSize() {
    return serverScreenSize;
  }
  
  public BuildCapability setServerScreenSize(String serverScreenSize) {
    this.serverScreenSize = serverScreenSize;
    return this;
  }
  
  public String getServerTimeZoneWithDst() {
    return serverTimeZoneWithDst;
  }
  
  public BuildCapability setServerTimeZoneWithDst(String serverTimeZoneWithDst) {
    this.serverTimeZoneWithDst = serverTimeZoneWithDst;
    return this;
  }
  
  public String getWdBrowserName() {
    return wdBrowserName;
  }
  
  public BuildCapability setWdBrowserName(String wdBrowserName) {
    this.wdBrowserName = wdBrowserName;
    return this;
  }
  
  public String getWdBrowserVersion() {
    return wdBrowserVersion;
  }
  
  public BuildCapability setWdBrowserVersion(String wdBrowserVersion) {
    this.wdBrowserVersion = wdBrowserVersion;
    return this;
  }
  
  public String getWdPlatformName() {
    return wdPlatformName;
  }
  
  public BuildCapability setWdPlatformName(String wdPlatformName) {
    this.wdPlatformName = wdPlatformName;
    return this;
  }
  
  public boolean isWdAcceptInsecureCerts() {
    return wdAcceptInsecureCerts;
  }
  
  public BuildCapability setWdAcceptInsecureCerts(boolean wdAcceptInsecureCerts) {
    this.wdAcceptInsecureCerts = wdAcceptInsecureCerts;
    return this;
  }
  
  public String getWdPageLoadStrategy() {
    return wdPageLoadStrategy;
  }
  
  public BuildCapability setWdPageLoadStrategy(String wdPageLoadStrategy) {
    this.wdPageLoadStrategy = wdPageLoadStrategy;
    return this;
  }
  
  public boolean isWdSetWindowRect() {
    return wdSetWindowRect;
  }
  
  public BuildCapability setWdSetWindowRect(boolean wdSetWindowRect) {
    this.wdSetWindowRect = wdSetWindowRect;
    return this;
  }
  
  public int getWdTimeoutsScript() {
    return wdTimeoutsScript;
  }
  
  public BuildCapability setWdTimeoutsScript(int wdTimeoutsScript) {
    this.wdTimeoutsScript = wdTimeoutsScript;
    return this;
  }
  
  public int getWdTimeoutsPageLoad() {
    return wdTimeoutsPageLoad;
  }
  
  public BuildCapability setWdTimeoutsPageLoad(int wdTimeoutsPageLoad) {
    this.wdTimeoutsPageLoad = wdTimeoutsPageLoad;
    return this;
  }
  
  public int getWdTimeoutsImplicit() {
    return wdTimeoutsImplicit;
  }
  
  public BuildCapability setWdTimeoutsImplicit(int wdTimeoutsImplicit) {
    this.wdTimeoutsImplicit = wdTimeoutsImplicit;
    return this;
  }
  
  public boolean isWdStrictFileInteractability() {
    return wdStrictFileInteractability;
  }
  
  public BuildCapability setWdStrictFileInteractability(boolean wdStrictFileInteractability) {
    this.wdStrictFileInteractability = wdStrictFileInteractability;
    return this;
  }
  
  public String getWdUnhandledPromptBehavior() {
    return wdUnhandledPromptBehavior;
  }
  
  public BuildCapability setWdUnhandledPromptBehavior(String wdUnhandledPromptBehavior) {
    this.wdUnhandledPromptBehavior = wdUnhandledPromptBehavior;
    return this;
  }
  
  public boolean isBrwIsFullScreen() {
    return brwIsFullScreen;
  }
  
  public BuildCapability setBrwIsFullScreen(boolean brwIsFullScreen) {
    this.brwIsFullScreen = brwIsFullScreen;
    return this;
  }
  
  public boolean isChromeEnableNetwork() {
    return chromeEnableNetwork;
  }
  
  public BuildCapability setChromeEnableNetwork(boolean chromeEnableNetwork) {
    this.chromeEnableNetwork = chromeEnableNetwork;
    return this;
  }
  
  public boolean isChromeEnablePage() {
    return chromeEnablePage;
  }
  
  public BuildCapability setChromeEnablePage(boolean chromeEnablePage) {
    this.chromeEnablePage = chromeEnablePage;
    return this;
  }
}
