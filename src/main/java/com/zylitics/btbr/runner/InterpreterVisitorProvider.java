package com.zylitics.btbr.runner;

import com.google.cloud.storage.Storage;
import com.google.common.collect.ImmutableMap;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.WebdriverFunctions;
import com.zylitics.btbr.webdriver.constants.*;
import com.zylitics.zwl.api.ZwlInterpreterVisitor;
import com.zylitics.zwl.datatype.MapZwlValue;
import com.zylitics.zwl.datatype.StringZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import com.zylitics.zwl.function.debugging.Print;
import com.zylitics.zwl.function.debugging.PrintF;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class InterpreterVisitorProvider {
  
  private final APICoreProperties.Webdriver wdProps;
  private final Storage storage;
  private final Consumer<Integer> lineChangeListener;
  private final Build build;
  private final RemoteWebDriver driver;
  private final PrintStream printStream;
  private final Map<String, String> zwlPreferences;
  private final Path buildDir;
  private final Map<String, String> zwlGlobals;
  
  InterpreterVisitorProvider(APICoreProperties.Webdriver wdProps,
                             Storage storage,
                             Consumer<Integer> lineChangeListener,
                             Build build,
                             RemoteWebDriver driver,
                             PrintStream printStream,
                             @Nullable Map<String, String> zwlPreferences,
                             Path buildDir,
                             @Nullable Map<String, String> zwlGlobals) {
    this.wdProps = wdProps;
    this.storage = storage;
    this.lineChangeListener = lineChangeListener;
    this.build = build;
    this.driver = driver;
    this.printStream = printStream;
    this.zwlPreferences = zwlPreferences;
    this.buildDir = buildDir;
    this.zwlGlobals = zwlGlobals;
  }
  
  // unit test can call this method, provide an instance of ZwlInterpreter and check that all
  // necessary methods are with appropriate arguments.
  ZwlInterpreterVisitor get() {
    BuildCapability buildCapability = build.getBuildCapability();
    WebdriverFunctions wdFunctions = new WebdriverFunctions(wdProps,
        buildCapability,
        driver,
        printStream,
        storage,
        wdProps.getUserUploadsStorageDirTmpl().replace("USER_ID",
            String.valueOf(build.getUserId())),
        buildDir);
  
    return zwlInterpreter -> {
      zwlInterpreter.setLineChangeListener(lineChangeListener::accept);
      zwlInterpreter.setFunctions(wdFunctions.get());
      //!! add any user-agent specific functions to override the base wd functions
    
      // overwrite some zwl functions to use our print stream
      zwlInterpreter.setFunction(new Print(printStream));
      zwlInterpreter.setFunction(new PrintF(printStream));
  
      // readonly variables...
      
      // from db
      if (zwlPreferences != null && zwlPreferences.size() > 0) {
        zwlInterpreter.setReadOnlyVariable("preferences",
            new MapZwlValue(zwlPreferences.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> new StringZwlValue(e.getValue())))));
      }
      if (zwlGlobals != null && zwlGlobals.size() > 0) {
        zwlInterpreter.setReadOnlyVariable("global",
            new MapZwlValue(zwlGlobals.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> new StringZwlValue(e.getValue())))));
      }
      
      // add ZWL & webdriver exceptions
      Map<String, ZwlValue> exceptions =
          new HashMap<>(com.zylitics.zwl.constants.Exceptions.asMap());
      exceptions.putAll(Exceptions.asMap());
      zwlInterpreter.setReadOnlyVariable("exceptions",
          new MapZwlValue(Collections.unmodifiableMap(exceptions)));
    
      // add colors and keys
      zwlInterpreter.setReadOnlyVariable("colors", new MapZwlValue(Colorz.asMap()));
      zwlInterpreter.setReadOnlyVariable("keys", new MapZwlValue(Keyz.asMap()));
    
      // add By
      zwlInterpreter.setReadOnlyVariable("by", new MapZwlValue(By.asMap()));
    
      // browser detail
      Map<String, ZwlValue> browserDetail = ImmutableMap.of(
          "name", new StringZwlValue(Browsers.valueByName(buildCapability.getWdBrowserName())
              .getAlias()),
          "version", new StringZwlValue(buildCapability.getWdBrowserVersion())
      );
      zwlInterpreter.setReadOnlyVariable("browser", new MapZwlValue(browserDetail));
    
      // add timeout type
      zwlInterpreter.setReadOnlyVariable("timeouts", new MapZwlValue(Timeouts.asMap()));
    
      // platform
      zwlInterpreter.setReadOnlyVariable("platform",
          new StringZwlValue(buildCapability.getWdPlatformName()));
    };
  }
}
