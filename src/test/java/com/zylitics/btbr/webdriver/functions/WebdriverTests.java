package com.zylitics.btbr.webdriver.functions;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.util.IOUtil;
import com.zylitics.btbr.webdriver.Configuration;
import com.zylitics.btbr.webdriver.WebdriverFunctions;
import com.zylitics.btbr.webdriver.constants.Colorz;
import com.zylitics.btbr.webdriver.constants.Exceptions;
import com.zylitics.btbr.webdriver.constants.Keyz;
import com.zylitics.btbr.webdriver.constants.Timeouts;
import com.zylitics.zwl.api.Main;
import com.zylitics.zwl.api.ZwlInterpreterVisitor;
import com.zylitics.zwl.datatype.*;
import com.zylitics.zwl.function.debugging.Print;
import com.zylitics.zwl.function.debugging.PrintF;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.DiagnosticErrorListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Webdriver tests meant to run locally.
 */
public class WebdriverTests {
  
  public static List<ANTLRErrorListener> DEFAULT_TEST_LISTENERS =
      ImmutableList.of(ConsoleErrorListener.INSTANCE, new DiagnosticErrorListener());
  
  final APICoreProperties.Webdriver wdProps = getDefaultWDProps();
  final Storage storage = getStorage();
  final PrintStream printStream = System.out;
  
  BuildCapability buildCapability;
  Path fakeBuildDir;
  RemoteWebDriver driver;
  ZwlInterpreterVisitor interpreterVisitor;
  
  @Tag("basic")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void basicWdTest(Browsers browsers) throws Exception {
    multipleRuns(browsers.getName(), "BasicWdTest.zwl");
  }
  
  @Tag("actions")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void actionsTest(Browsers browsers) throws Exception {
    multipleRuns(browsers.getName(), "ActionsTest.zwl");
  }
  
  @Tag("color")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void colorTest(Browsers browsers) throws Exception {
    multipleRuns(browsers.getName(), "ColorTest.zwl");
  }
  
  @Tag("context")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void contextTest(Browsers browsers) throws Exception {
    multipleRuns(browsers.getName(), "ContextTest.zwl");
  }
  
  @Tag("cookie")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void cookieTest(Browsers browsers) throws Exception {
    multipleRuns(browsers.getName(), "CookieTest.zwl");
  }
  
  @Tag("document")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void documentTest(Browsers browsers) throws Exception {
    multipleRuns(browsers.getName(), "DocumentTest.zwl");
  }
  
  @Tag("einteraction")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void eInteractionTest(Browsers browsers) throws Exception {
    multipleRuns(browsers.getName(), "EInteractionTest.zwl");
  }
  
  @Tag("setfiles")
  @Tag("slow")
  @Tag("io")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void setFilesTest(Browsers browsers) throws Exception {
    multipleRuns(browsers.getName(), "SetFilesTest.zwl");
  }
  
  @Tag("einteractionkeys")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void eInteractionKeysTest(Browsers browsers) throws Exception {
    multipleRuns(browsers.getName(), "EInteractionKeysTest.zwl");
  }
  
  @Tag("elementretrieval")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void elementRetrievalTest(Browsers browsers) throws Exception {
    multipleRuns(browsers.getName(), "ElementRetrievalTest.zwl");
  }
  
  @Tag("elementstate")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void elementStateTest(Browsers browsers) throws Exception {
    multipleRuns(browsers.getName(), "ElementStateTest.zwl");
  }
  
  @Tag("navigation")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void navigationTest(Browsers browsers) throws Exception {
    multipleRuns(browsers.getName(), "NavigationTest.zwl");
  }
  
  @Tag("prompts")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void promptsTest(Browsers browsers) throws Exception {
    multipleRuns(browsers.getName(), "PromptsTest.zwl");
  }
  
  @Tag("select")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void selectTest(Browsers browsers) throws Exception {
    multipleRuns(browsers.getName(), "SelectTest.zwl");
  }
  
  @Tag("storage")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void storageTest(Browsers browsers) throws Exception {
    multipleRuns(browsers.getName(), "StorageTest.zwl");
  }
  
  @Tag("timeout")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void timeoutTest(Browsers browsers) throws Exception {
    multipleRuns(browsers.getName(), "TimeoutTest.zwl");
  }
  
  @Tag("until")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void untilTest(Browsers browsers) throws Exception {
    multipleRuns(browsers.getName(), "UntilTest.zwl");
  }
  
  private void multipleRuns(String browser, String file) throws Exception {
    Assumptions.assumeFalse(shouldSkip(browser), "Skipped");
    setup(browser);
    Main main = new Main("resources/" + file, Charsets.UTF_8, DEFAULT_TEST_LISTENERS);
    main.interpret(interpreterVisitor);
  }
  
  private boolean shouldSkip(String browser) {
    return System.getProperty("skipBrowser") != null
        && System.getProperty("skipBrowser").equalsIgnoreCase(browser);
  }
  
  private void setup(String browser) throws Exception {
    buildCapability = getBuildCapability(browser);
    Capabilities caps = getCapabilities(buildCapability, wdProps);
    if (buildCapability.getWdBrowserName().equals("chrome")) {
      ChromeOptions chrome = new ChromeOptions();
      chrome.merge(caps);
      driver = new ChromeDriver(ChromeDriverService.createDefaultService(), chrome);
    } else if (buildCapability.getWdBrowserName().equals("firefox")) {
      FirefoxOptions ff = new FirefoxOptions();
      String logLevel = System.getProperty("webdriver.firefox.loglevel");
      if (logLevel != null) {
        ff.setLogLevel(FirefoxDriverLogLevel.fromString(logLevel));
      }
      ff.merge(caps);
      driver = new FirefoxDriver(GeckoDriverService.createDefaultService(), ff);
    } else {
      throw new RuntimeException("can't run local build on " + buildCapability.getWdBrowserName());
    }
    
    // do some actions on driver based on build capabilities
    if (buildCapability.isBrw_start_maximize()) {
      driver.manage().window().maximize();
    }
    
    fakeBuildDir = Paths.get(Configuration.SYS_DEF_TEMP_DIR, "build-111111");
    
    if (Files.isDirectory(fakeBuildDir)) {
      IOUtil.deleteDir(fakeBuildDir);
    }
    Files.createDirectory(fakeBuildDir);
    WebdriverFunctions wdFunctions = new WebdriverFunctions(wdProps,
        buildCapability,
        driver,
        printStream,
        storage,
        "zl-user-data",
        "11021/uploads",
        fakeBuildDir);
    
    interpreterVisitor = zwlInterpreter -> {
      zwlInterpreter.setFunctions(wdFunctions.get());
      //!! add any user-agent specific functions to override the base wd functions
      
      // overwrite some zwl functions to use our print stream (although both use same System.out
      // stream for these tests)
      zwlInterpreter.setFunction(new Print(printStream));
      zwlInterpreter.setFunction(new PrintF(printStream));
      
      // readonly variables...
      // add ZWL & webdriver exceptions
      Map<String, ZwlValue> exceptions =
          new HashMap<>(com.zylitics.zwl.constants.Exceptions.asMap());
      exceptions.putAll(Exceptions.asMap());
      zwlInterpreter.setReadOnlyVariable("exceptions",
          new MapZwlValue(Collections.unmodifiableMap(exceptions)));
      
      // add colors and keys
      zwlInterpreter.setReadOnlyVariable("colors", new MapZwlValue(Colorz.asMap()));
      zwlInterpreter.setReadOnlyVariable("keys", new MapZwlValue(Keyz.asMap()));
      
      // browser detail, not adding version as it's not required and given in these tests.
      Map<String, ZwlValue> browserDetail = ImmutableMap.of(
          "name", new StringZwlValue(buildCapability.getWdBrowserName())
      );
      zwlInterpreter.setReadOnlyVariable("browser", new MapZwlValue(browserDetail));
      
      // add timeout type
      zwlInterpreter.setReadOnlyVariable("timeouts", new MapZwlValue(Timeouts.asMap()));
    };
  }
  
  @AfterEach
  void tearDown() {
    if (driver == null) {
      return;
    }
    int hold = Integer.getInteger("holdWdCloseFor", 0); // in seconds.
    if (hold > 0) {
      try {
        Thread.sleep(hold * 1000);
      } catch (InterruptedException ignored) {
        // ignore
      }
    }
    driver.quit();
  }
  
  private APICoreProperties.Webdriver getDefaultWDProps() {
    APICoreProperties.Webdriver wd = new APICoreProperties.Webdriver();
    wd.setDefaultPageLoadStrategy("eager");
    wd.setDefaultTimeoutElementAccess(10_000);
    wd.setDefaultTimeoutPageLoad(30_000);
    wd.setDefaultTimeoutScript(30_000);
    wd.setDefaultTimeoutNewWindow(10_000);
    return wd;
  }
  
  private BuildCapability getBuildCapability(String browser) {
    BuildCapability b = new BuildCapability();
    b.setWdBrowserName(browser);
    b.setWdPlatformName("mac");
    b.setWdSetWindowRect(true);
    b.setWdUnhandledPromptBehavior("ignore");
    b.setBrw_start_maximize(true);
    // all timeouts in build caps are initialized with -1 in db if no value is given.
    b.setWdTimeoutsPageLoad(-1);
    b.setWdTimeoutsElementAccess(-1);
    b.setWdTimeoutsScript(-1);
    return b;
  }
  
  private Storage getStorage() {
    return StorageOptions.getDefaultInstance().getService();
  }
  
  private Capabilities getCapabilities(BuildCapability buildCapability,
                                       APICoreProperties.Webdriver wdProps) {
    MutableCapabilities caps = new MutableCapabilities();
    
    caps.setCapability(CapabilityType.PLATFORM_NAME, buildCapability.getWdPlatformName());
    
    caps.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS,
        buildCapability.isWdAcceptInsecureCerts());
    
    caps.setCapability(CapabilityType.PAGE_LOAD_STRATEGY
        , Strings.isNullOrEmpty(buildCapability.getWdPageLoadStrategy())
            ? wdProps.getDefaultPageLoadStrategy()
            : buildCapability.getWdPageLoadStrategy());
    
    Preconditions.checkArgument(
        !Strings.isNullOrEmpty(buildCapability.getWdUnhandledPromptBehavior()),
        "unhandled prompt behaviour capability can't be empty");
    caps.setCapability(CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR,
        buildCapability.getWdUnhandledPromptBehavior());
    caps.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR,
        buildCapability.getWdUnhandledPromptBehavior());
    
    return caps;
  }
}
