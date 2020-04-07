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
import com.zylitics.btbr.webdriver.TimeoutType;
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
import jnr.ffi.annotations.In;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.DiagnosticErrorListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
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
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

/**
 * Webdriver tests meant to run locally.
 * I contains tests for individual zwl files and a test for running all of them together in the
 * same session as a suite. Whenever need to run all of them, always use {@link #allTests}
 * method.
 * Don't run all methods of this class, use tag name while running (either mvn or IDE)
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
  
  @BeforeEach
  void checks() {
    Preconditions.checkNotNull(System.getProperty("os"), "os should be set as system property");
  }
  
  // This executes all zwl tests in a single webdriver session, clearing cookies, windows before
  // running each test.
  @Tag("all")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void allTests(Browsers browsers) throws Exception {
    Assumptions.assumeFalse(shouldSkip(browsers), "Skipped");
    setup(browsers);
    for (ZwlTests t : ZwlTests.values()) {
      //-------------------------------Sanitize start-----------------------------------------------
      // delete any open windows and leave just one with about:blank, delete all cookies before
      // reading new test
      List<String> winHandles = new ArrayList<>(driver.getWindowHandles());
      for (int i = 0; i < winHandles.size(); i++) {
        driver.switchTo().window(winHandles.get(i));
        if (i < winHandles.size() - 1) {
          driver.close();
        }
      }
      driver.get("about:blank"); // "about local scheme" can be given to 'get' as per webdriver spec
      driver.manage().deleteAllCookies(); // delete all cookies
      
      //-------------------------------Sanitize end-------------------------------------------------
      String file = t.getFile();
      printStream.println("Reading and executing from " + file);
      Main main = new Main("resources/" + t.getFile(), Charsets.UTF_8, DEFAULT_TEST_LISTENERS);
      main.interpret(interpreterVisitor);
    }
  }
  
  @Tag("basic")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void basicWdTest(Browsers browsers) throws Exception {
    run(browsers, ZwlTests.BASIC_WD_TEST.getFile());
  }
  
  @Tag("actions")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void actionsTest(Browsers browsers) throws Exception {
    run(browsers, ZwlTests.ACTIONS_TEST.getFile());
  }
  
  @Tag("color")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void colorTest(Browsers browsers) throws Exception {
    run(browsers, ZwlTests.COLOR_TEST.getFile());
  }
  
  @Tag("context")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void contextTest(Browsers browsers) throws Exception {
    run(browsers, ZwlTests.CONTEXT_TEST.getFile());
  }
  
  @Tag("cookie")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void cookieTest(Browsers browsers) throws Exception {
    run(browsers, ZwlTests.COOKIE_TEST.getFile());
  }
  
  @Tag("document")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void documentTest(Browsers browsers) throws Exception {
    run(browsers, ZwlTests.DOCUMENT_TEST.getFile());
  }
  
  @Tag("einteraction")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void eInteractionTest(Browsers browsers) throws Exception {
    run(browsers, ZwlTests.E_INTERACTION_TEST.getFile());
  }
  
  @Tag("setfiles")
  @Tag("slow")
  @Tag("io")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void setFilesTest(Browsers browsers) throws Exception {
    run(browsers, ZwlTests.SET_FILES_TEST.getFile());
  }
  
  @Tag("einteractionkeys")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void eInteractionKeysTest(Browsers browsers) throws Exception {
    run(browsers, ZwlTests.E_INTERACTION_KEYS_TEST.getFile());
  }
  
  @Tag("elementretrieval")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void elementRetrievalTest(Browsers browsers) throws Exception {
    run(browsers, ZwlTests.ELEMENT_RETRIEVAL_TEST.getFile());
  }
  
  @Tag("elementstate")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void elementStateTest(Browsers browsers) throws Exception {
    run(browsers, ZwlTests.ELEMENT_STATE_TEST.getFile());
  }
  
  @Tag("navigation")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void navigationTest(Browsers browsers) throws Exception {
    run(browsers, ZwlTests.NAVIGATION_TEST.getFile());
  }
  
  @Tag("prompts")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void promptsTest(Browsers browsers) throws Exception {
    run(browsers, ZwlTests.PROMPTS_TEST.getFile());
  }
  
  @Tag("select")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void selectTest(Browsers browsers) throws Exception {
    run(browsers, ZwlTests.SELECT_TEST.getFile());
  }
  
  @Tag("storage")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void storageTest(Browsers browsers) throws Exception {
    run(browsers, ZwlTests.STORAGE_TEST.getFile());
  }
  
  @Tag("timeout")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void timeoutTest(Browsers browsers) throws Exception {
    run(browsers, ZwlTests.TIMEOUT_TEST.getFile());
  }
  
  @Tag("until")
  @ParameterizedTest
  @EnumSource(value = Browsers.class)
  void untilTest(Browsers browsers) throws Exception {
    run(browsers, ZwlTests.UNTIL_TEST.getFile());
  }
  
  private void run(Browsers browser, String file) throws Exception {
    Assumptions.assumeFalse(shouldSkip(browser), "Skipped");
    setup(browser);
    Main main = new Main("resources/" + file, Charsets.UTF_8, DEFAULT_TEST_LISTENERS);
    main.interpret(interpreterVisitor);
  }
  
  private boolean shouldSkip(Browsers browser) {
    String toSkip = System.getProperty("skipBrowser");
    if (Strings.isNullOrEmpty(toSkip)) {
      return false;
    }
    List<String> browsersToSkip = Arrays.asList(toSkip.split(","));
    // can given either ie, IE or internet explorer in skip list.
    return browsersToSkip.stream().anyMatch(b ->
        browser.name().equalsIgnoreCase(b) || browser.getName().equalsIgnoreCase(b));
  }
  
  private void setup(Browsers browser) throws Exception {
    buildCapability = getBuildCapability(browser);
    Capabilities caps = getCapabilities(buildCapability, wdProps);
    if (browser.equals(Browsers.CHROME)) {
      ChromeOptions chrome = new ChromeOptions();
      chrome.merge(caps);
      driver = new ChromeDriver(ChromeDriverService.createDefaultService(), chrome);
    } else if (browser.equals(Browsers.FIREFOX)) {
      FirefoxOptions ff = new FirefoxOptions();
      String logLevel = System.getProperty("webdriver.firefox.loglevel");
      if (logLevel != null) {
        ff.setLogLevel(FirefoxDriverLogLevel.fromString(logLevel));
      }
      ff.merge(caps);
      driver = new FirefoxDriver(GeckoDriverService.createDefaultService(), ff);
    } else if (browser.equals(Browsers.IE)) {
      // IE driver has lot of custom capabilities available as ie options, their description could
      // be found from the changelog
      // https://raw.githubusercontent.com/SeleniumHQ/selenium/master/cpp/iedriverserver/CHANGELOG
      // Also read the known issues and details of some workarounds from
      // https://github.com/SeleniumHQ/selenium/wiki/InternetExplorerDriver
      // Three files are important, InternetExplorerOptions, InternetExplorerDriver and
      // InternetExplorerDriverService
      InternetExplorerOptions ie = new InternetExplorerOptions();
      ie.merge(caps);
      ie.withAttachTimeout(Duration.ofMillis(wdProps.getIeDefaultBrowserAttachTimeout()));
      // enablePersistentHovering, not using as it can cause issues while hovering over, like
      // continues mouse move even after reaching target element.
      // elementScrollTo, keeping the default Top, don't want to give to user.
      ie.destructivelyEnsureCleanSession();
      // useCreateProcessApiToLaunchIe, useShellWindowsApiToAttachToIe not using for now until
      // we get some problem in launch.
      ie.withInitialBrowserUrl("about:blank");
      ie.requireWindowFocus(); // this will be important for using native events, note that
      // the browser window should always be in focus while the test is running.
      ie.waitForUploadDialogUpTo(Duration.ofMillis(wdProps.getIeDefaultFileUploadDialogTimeout()));
      // ignoreZoomSettings, don't ignore zoom settings
      driver = new InternetExplorerDriver(InternetExplorerDriverService.createDefaultService(), ie);
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
    wd.setDefaultTimeoutElementAccess(30_000);
    wd.setDefaultTimeoutPageLoad(60_000);
    wd.setDefaultTimeoutScript(30_000);
    wd.setDefaultTimeoutNewWindow(10_000);
    wd.setIeDefaultBrowserAttachTimeout(5000);
    return wd;
  }
  
  private BuildCapability getBuildCapability(Browsers browser) {
    BuildCapability b = new BuildCapability();
    b.setWdBrowserName(browser.getName());
    b.setWdPlatformName(System.getProperty("os"));
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
  
    Map<String, Object> timeouts = new HashMap<>(3);
    timeouts.put("script",
        new Configuration().getTimeouts(wdProps, buildCapability, TimeoutType.JAVASCRIPT));
    timeouts.put("pageLoad",
        new Configuration().getTimeouts(wdProps, buildCapability, TimeoutType.PAGE_LOAD));
    caps.setCapability("timeouts", timeouts);
    
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
