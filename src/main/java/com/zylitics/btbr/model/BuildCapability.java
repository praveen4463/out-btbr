package com.zylitics.btbr.model;

public class BuildCapability {
  
  private String wdBrowserName;
  
  private String wdBrowserVersion;
  
  private String wdPlatformName;
  
  private boolean wdAcceptInsecureCerts;
  
  private String wdPageLoadStrategy;
  
  private boolean wdSetWindowRect;
  
  private int wdTimeoutsScript;
  
  private int wdTimeoutsPageLoad;
  
  private int wdTimeoutsElementAccess;
  
  private boolean wdStrictFileInteractability;
  
  private String wdUnhandledPromptBehavior;
  
  private String wdIeElementScrollBehavior;
  
  private boolean wdIeEnablePersistentHovering;
  
  private boolean wdIeRequireWindowFocus;
  
  private boolean wdIeDisableNativeEvents;
  
  private boolean wdIeDestructivelyEnsureCleanSession;
  
  private String wdIeLogLevel;
  
  private boolean wdChromeVerboseLogging;
  
  private boolean wdChromeSilentOutput;
  
  private boolean wdChromeEnableNetwork;
  
  private boolean wdChromeEnablePage;
  
  private String wdFirefoxLogLevel;
  
  private boolean wdBrwStartMaximize;
  
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
  
  @SuppressWarnings("unused")
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
  
  public int getWdTimeoutsElementAccess() {
    return wdTimeoutsElementAccess;
  }
  
  public BuildCapability setWdTimeoutsElementAccess(int wdTimeoutsElementAccess) {
    this.wdTimeoutsElementAccess = wdTimeoutsElementAccess;
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
  
  public String getWdIeElementScrollBehavior() {
    return wdIeElementScrollBehavior;
  }
  
  public BuildCapability setWdIeElementScrollBehavior(String wdIeElementScrollBehavior) {
    this.wdIeElementScrollBehavior = wdIeElementScrollBehavior;
    return this;
  }
  
  public boolean isWdIeEnablePersistentHovering() {
    return wdIeEnablePersistentHovering;
  }
  
  public BuildCapability setWdIeEnablePersistentHovering(boolean wdIeEnablePersistentHovering) {
    this.wdIeEnablePersistentHovering = wdIeEnablePersistentHovering;
    return this;
  }
  
  public boolean isWdIeRequireWindowFocus() {
    return wdIeRequireWindowFocus;
  }
  
  public BuildCapability setWdIeRequireWindowFocus(boolean wdIeRequireWindowFocus) {
    this.wdIeRequireWindowFocus = wdIeRequireWindowFocus;
    return this;
  }
  
  public boolean isWdIeDisableNativeEvents() {
    return wdIeDisableNativeEvents;
  }
  
  public BuildCapability setWdIeDisableNativeEvents(boolean wdIeDisableNativeEvents) {
    this.wdIeDisableNativeEvents = wdIeDisableNativeEvents;
    return this;
  }
  
  public boolean isWdIeDestructivelyEnsureCleanSession() {
    return wdIeDestructivelyEnsureCleanSession;
  }
  
  public BuildCapability setWdIeDestructivelyEnsureCleanSession(
      boolean wdIeDestructivelyEnsureCleanSession) {
    this.wdIeDestructivelyEnsureCleanSession = wdIeDestructivelyEnsureCleanSession;
    return this;
  }
  
  public String getWdIeLogLevel() {
    return wdIeLogLevel;
  }
  
  public BuildCapability setWdIeLogLevel(String wdIeLogLevel) {
    this.wdIeLogLevel = wdIeLogLevel;
    return this;
  }
  
  public boolean isWdChromeVerboseLogging() {
    return wdChromeVerboseLogging;
  }
  
  public BuildCapability setWdChromeVerboseLogging(boolean wdChromeVerboseLogging) {
    this.wdChromeVerboseLogging = wdChromeVerboseLogging;
    return this;
  }
  
  public boolean isWdChromeSilentOutput() {
    return wdChromeSilentOutput;
  }
  
  public BuildCapability setWdChromeSilentOutput(boolean wdChromeSilentOutput) {
    this.wdChromeSilentOutput = wdChromeSilentOutput;
    return this;
  }
  
  public boolean isWdChromeEnableNetwork() {
    return wdChromeEnableNetwork;
  }
  
  public BuildCapability setWdChromeEnableNetwork(boolean wdChromeEnableNetwork) {
    this.wdChromeEnableNetwork = wdChromeEnableNetwork;
    return this;
  }
  
  public boolean isWdChromeEnablePage() {
    return wdChromeEnablePage;
  }
  
  public BuildCapability setWdChromeEnablePage(boolean wdChromeEnablePage) {
    this.wdChromeEnablePage = wdChromeEnablePage;
    return this;
  }
  
  public String getWdFirefoxLogLevel() {
    return wdFirefoxLogLevel;
  }
  
  public BuildCapability setWdFirefoxLogLevel(String wdFirefoxLogLevel) {
    this.wdFirefoxLogLevel = wdFirefoxLogLevel;
    return this;
  }
  
  public boolean isWdBrwStartMaximize() {
    return wdBrwStartMaximize;
  }
  
  public BuildCapability setWdBrwStartMaximize(boolean wdBrwStartMaximize) {
    this.wdBrwStartMaximize = wdBrwStartMaximize;
    return this;
  }
  
  @Override
  public String toString() {
    return "BuildCapability{" +
        "wdBrowserName='" + wdBrowserName + '\'' +
        ", wdBrowserVersion='" + wdBrowserVersion + '\'' +
        ", wdPlatformName='" + wdPlatformName + '\'' +
        ", wdAcceptInsecureCerts=" + wdAcceptInsecureCerts +
        ", wdPageLoadStrategy='" + wdPageLoadStrategy + '\'' +
        ", wdSetWindowRect=" + wdSetWindowRect +
        ", wdTimeoutsScript=" + wdTimeoutsScript +
        ", wdTimeoutsPageLoad=" + wdTimeoutsPageLoad +
        ", wdTimeoutsElementAccess=" + wdTimeoutsElementAccess +
        ", wdStrictFileInteractability=" + wdStrictFileInteractability +
        ", wdUnhandledPromptBehavior='" + wdUnhandledPromptBehavior + '\'' +
        ", wdIeElementScrollBehavior='" + wdIeElementScrollBehavior + '\'' +
        ", wdIeEnablePersistentHovering=" + wdIeEnablePersistentHovering +
        ", wdIeRequireWindowFocus=" + wdIeRequireWindowFocus +
        ", wdIeDisableNativeEvents=" + wdIeDisableNativeEvents +
        ", wdIeDestructivelyEnsureCleanSession=" + wdIeDestructivelyEnsureCleanSession +
        ", wdIeLogLevel='" + wdIeLogLevel + '\'' +
        ", wdChromeVerboseLogging=" + wdChromeVerboseLogging +
        ", wdChromeSilentOutput=" + wdChromeSilentOutput +
        ", wdChromeEnableNetwork=" + wdChromeEnableNetwork +
        ", wdChromeEnablePage=" + wdChromeEnablePage +
        ", wdFirefoxLogLevel='" + wdFirefoxLogLevel + '\'' +
        ", wdBrwStartMaximize=" + wdBrwStartMaximize +
        '}';
  }
}
