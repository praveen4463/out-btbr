package com.zylitics.btbr.runner;

import com.google.cloud.storage.Storage;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.zwl.api.ZwlWdTestProperties;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

public class ZwlWdTestPropertiesImpl implements ZwlWdTestProperties {
  
  private final APICoreProperties.Webdriver wdProps;
  private final Storage storage;
  private final Build build;
  private final RemoteWebDriver driver;
  private final PrintStream printStream;
  private final Consumer<String> callTestHandler;
  private final Map<String, String> buildVariables;
  private final Map<String, String> zwlPreferences;
  private final Path buildDir;
  private final Map<String, String> zwlGlobals;
  
  public ZwlWdTestPropertiesImpl(APICoreProperties.Webdriver wdProps,
                                 Storage storage,
                                 Build build,
                                 RemoteWebDriver driver,
                                 PrintStream printStream,
                                 Consumer<String> callTestHandler,
                                 @Nullable Map<String, String> buildVariables,
                                 @Nullable Map<String, String> zwlPreferences,
                                 Path buildDir,
                                 @Nullable Map<String, String> zwlGlobals) {
    this.wdProps = wdProps;
    this.storage = storage;
    this.build = build;
    this.driver = driver;
    this.printStream = printStream;
    this.callTestHandler = callTestHandler;
    this.buildVariables = buildVariables;
    this.zwlPreferences = zwlPreferences;
    this.buildDir = buildDir;
    this.zwlGlobals = zwlGlobals;
  }
  
  @Override
  public RemoteWebDriver getDriver() {
    return driver;
  }
  
  @Override
  public PrintStream getPrintStream() {
    return printStream;
  }
  
  @Override
  public Consumer<String> getCallTestHandler() {
    return callTestHandler;
  }
  
  @Override
  public Storage getStorage() {
    return storage;
  }
  
  @Override
  public String getUserUploadsCloudPath() {
    return wdProps.getUserUploadsStorageDirTmpl().replace("USER_ID",
        String.valueOf(build.getUserId()));
  }
  
  @Override
  public Path getBuildDir() {
    return buildDir;
  }
  
  @Override
  public Defaults getDefault() {
    return new Defaults() {
      @Override
      public String getUserDataBucket() {
        return wdProps.getUserDataBucket();
      }
  
      @Override
      public Integer getDefaultTimeoutElementAccess() {
        return wdProps.getDefaultTimeoutElementAccess();
      }
  
      @Override
      public Integer getDefaultTimeoutPageLoad() {
        return wdProps.getDefaultTimeoutPageLoad();
      }
  
      @Override
      public Integer getDefaultTimeoutScript() {
        return wdProps.getDefaultTimeoutScript();
      }
  
      @Override
      public Integer getDefaultTimeoutNewWindow() {
        return wdProps.getDefaultTimeoutNewWindow();
      }
  
      @Override
      public String getElementShotDir() {
        return wdProps.getElementShotDir();
      }
    };
  }
  
  @Override
  public Capabilities getCapabilities() {
    BuildCapability buildCapability = build.getBuildCapability();
    
    return new Capabilities() {
      @Override
      public String getBrowserName() {
        return buildCapability.getWdBrowserName();
      }
  
      @Override
      public String getBrowserVersion() {
        return buildCapability.getWdBrowserVersion();
      }
  
      @Override
      public String getPlatformName() {
        return buildCapability.getWdPlatformName();
      }
  
      @Override
      public String getMeDeviceResolution() {
        return buildCapability.getWdMeDeviceResolution();
      }
  
      @Override
      public Integer getCustomTimeoutElementAccess() {
        return buildCapability.getWdTimeoutsElementAccess();
      }
  
      @Override
      public Integer getCustomTimeoutPageLoad() {
        return buildCapability.getWdTimeoutsPageLoad();
      }
  
      @Override
      public Integer getCustomTimeoutScript() {
        return buildCapability.getWdTimeoutsScript();
      }
    };
  }
  
  @Override
  public Variables getVariables() {
    return new Variables() {
      @Nullable
      @Override
      public Map<String, String> getBuildVariables() {
        return buildVariables;
      }
  
      @Nullable
      @Override
      public Map<String, String> getPreferences() {
        return zwlPreferences;
      }
  
      @Nullable
      @Override
      public Map<String, String> getGlobal() {
        return zwlGlobals;
      }
    };
  }
  
  @Override
  public String getVMResolution() {
    return build.getServerScreenSize();
  }
}
