package com.zylitics.btbr.webdriver.executor;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.util.Assert;
import com.zylitics.btbr.webdriver.BrwExtCmdDef;
import com.zylitics.btbr.webdriver.BrwExtCmdResult;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * An abstract implementation of command executor. All available browsers should extend from this
 * so that if any command requires browser specific handling, that behaviour can be overridden.
 * All command execution related methods should be package private and all of them should throw
 * a RuntimeException on failure.
 */
public abstract class AbstractBrwExtCmdExecutor extends BaseBrwExtCmdExecutor {
  
  private static final Logger LOG = LoggerFactory.getLogger(AbstractBrwExtCmdExecutor.class);
  
  final WebDriver.Window window;
  final WebDriver.Options options;
  final Map<String, Runnable> cmdMap;
  
  BrwExtCmdDef cmdDef;
  
  public AbstractBrwExtCmdExecutor(APICoreProperties.Webdriver wdProps,
                                   RemoteWebDriver driver) {
    super(wdProps, driver);
    
    options = driver.manage();
    window = options.window();
    cmdMap = new BrwExtCmdHandlerMap(this).get();
  }
  
  public final BrwExtCmdResult submitCmdDef(BrwExtCmdDef cmdDef) {
    /*
    this.cmdDef = cmdDef;
    // validations
    if (!cmdMap.containsKey(cmdDef.getCommand())) {
      return buildErrorResult("Unknown command: " + cmdDef.getCommand());
    }
  
    boolean success = false;
    boolean doNotAbortBuild = false;
    String error = "";
    long totalMillis = 0;
  
    long start = System.nanoTime();
    try {
      cmdMap.get(cmdDef.getCommand()).run();
      success = true;
    } catch (Throwable t) {
      LOG.error(t.getMessage(), t);
      Class tClass = t.getClass();
      if (tClass == VerificationException.class) {
        doNotAbortBuild = true;
      }
      // For now let's just send the message part and exception class name.
      error = String.format("%s, %s", tClass.getSimpleName(), t.getMessage());
    }
    totalMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
    return buildResult(totalMillis, success, error, doNotAbortBuild);*/
    return null;
  }
  
  /**
   *
   */
  void waitForTitle() {
  
  }
  
  void waitForLocation() {
  
  }
  
  void waitForImageSrc() {
  
  }
  
  void waitForSelectOptions() {
  
  }
  
  void waitForText() {
  
  }
  
  void waitForValue() {
  
  }
  
  void waitForSelection() {
  
  }
  
  void waitForChecked() {
  
  }
  
  void waitForNotChecked() {
  
  }
  
  void waitForEditable() {
  
  }
  
  void waitForNotEditable() {
  
  }
  
  void waitForSomethingSelected() {
  
  }
  
  void waitForElementPresent() {
  
  }
  
  void waitForColor() {
  
  }
  
  void verifyTitle() {
  
  }
  
  void verifyLocation() {
  
  }
  
  void verifyImageSrc() {
  
  }
  
  void verifyText() {
  
  }
  
  void verifyValue() {
  
  }
  
  void verifySelection() {
  
  }
  
  void verifyChecked() {
  
  }
  
  void verifyNotChecked() {
  
  }
  
  void verifyEditable() {
  
  }
  
  void verifyNotEditable() {
  
  }
  
  void verifySomethingSelected() {
  
  }
  
  void verifyElementPresent() {
  
  }
  
  void verifyColor() {
  
  }
  
  void assertTitle() {
  
  }
  
  void assertLocation() {
  
  }
  
  void assertImageSrc() {
  
  }
  
  void assertText() {
  
  }
  
  void assertValue() {
  
  }
  
  void assertSelection() {
  
  }
  
  void assertChecked() {
  
  }
  
  void assertNotChecked() {
  
  }
  
  void assertEditable() {
  
  }
  
  void assertNotEditable() {
  
  }
  
  void assertSomethingSelected() {
  
  }
  
  void assertElementPresent() {
  
  }
  
  void assertColor() {
  
  }
  
  void assertAlert() {
  
  }
  
  void assertConfirm() {
  
  }
  
  void assertPrompt() {
  
  }
  
  void chooseOkOnNextConfirmation() {
  
  }
  
  void chooseCancelOnNextConfirmation() {
  
  }
  
  void answerOnNextPrompt() {
  
  }
  
  void chooseCancelOnNextPrompt() {
  
  }
  
  void openAndWait() {
  
  }
  
  void createTab() {
  
  }
  
  void createWindow() {
  
  }
  
  void selectFrame() {
  
  }
  
  void selectWindow() {
  
  }
  
  void selectTab() {
  
  }
  
  void closeTab() {
  
  }
  
  void closeWindow() {
  
  }
  
  void acceptOnPageLeaveDlgIfAny() {
  
  }
  
  void click() {
  
  }
  
  void scrollTo() {
  
  }
  
  void mouseMove() {
  
  }
  
  void dblClick() {
  
  }
  
  void dragDrop() {
  
  }
  
  void sendKeys() {
  
  }
  
  void select() {
  
  }
  
  void keyDown() {
  
  }
  
  void maximize() {
    window.maximize();
  }
  
  void fullScreen() {
    window.fullscreen();
  }
  
  void deleteCookie() {
    Assert.notEmpty(cmdDef.getTarget(), "Cookie name is required to delete it.");
    options.deleteCookieNamed(cmdDef.getTarget());
  }
  
  void deleteAllCookies() {
    options.deleteAllCookies();
  }
}
