package com.zylitics.btbr.webdriver;

import com.google.cloud.storage.Storage;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.action.ActionFunctions;
import com.zylitics.btbr.webdriver.functions.action.DragAndDrop;
import com.zylitics.btbr.webdriver.functions.action.PerformAction;
import com.zylitics.btbr.webdriver.functions.action.Scroll;
import com.zylitics.btbr.webdriver.functions.color.ColorMatches;
import com.zylitics.btbr.webdriver.functions.context.*;
import com.zylitics.btbr.webdriver.functions.context.resize.*;
import com.zylitics.btbr.webdriver.functions.cookies.*;
import com.zylitics.btbr.webdriver.functions.document.ExecuteAsyncScript;
import com.zylitics.btbr.webdriver.functions.document.ExecuteScript;
import com.zylitics.btbr.webdriver.functions.document.GetPageSource;
import com.zylitics.btbr.webdriver.functions.elements.interaction.*;
import com.zylitics.btbr.webdriver.functions.elements.interaction.keys.*;
import com.zylitics.btbr.webdriver.functions.elements.retrieval.*;
import com.zylitics.btbr.webdriver.functions.elements.state.*;
import com.zylitics.btbr.webdriver.functions.navigation.*;
import com.zylitics.btbr.webdriver.functions.prompts.AcceptAlert;
import com.zylitics.btbr.webdriver.functions.prompts.DismissAlert;
import com.zylitics.btbr.webdriver.functions.prompts.GetAlertText;
import com.zylitics.btbr.webdriver.functions.prompts.SendAlertText;
import com.zylitics.btbr.webdriver.functions.select.*;
import com.zylitics.btbr.webdriver.functions.storage.*;
import com.zylitics.btbr.webdriver.functions.timeout.GetTimeout;
import com.zylitics.btbr.webdriver.functions.timeout.SetElementAccessTimeout;
import com.zylitics.btbr.webdriver.functions.timeout.SetPageLoadTimeout;
import com.zylitics.btbr.webdriver.functions.timeout.SetScriptTimeout;
import com.zylitics.btbr.webdriver.functions.until.*;
import com.zylitics.btbr.webdriver.functions.util.IsValidElemId;
import com.zylitics.btbr.webdriver.functions.util.Sleep;
import com.zylitics.zwl.interpret.Function;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Set;

/**
 * These are the webdriver functions applicable to all user agents and are as per the specs, if any
 * user agent doesn't confirm to the spec, a separate implementation specific to the user agent
 * should be implemented by extending the respective function. All those custom implementations
 * should overwrite their respective function returned from this class, which means at a time only
 * one implementation for a particular webdriver command should exist.
 */
public class WebdriverFunctions {
  
  private final APICoreProperties.Webdriver wdProps;
  
  private final BuildCapability buildCapability;
  
  private final RemoteWebDriver driver;
  
  private final PrintStream printStream;
  
  private final Storage storage;
  
  private final String userAccountBucket;
  
  private final String pathToUploadedFiles;
  
  private final Path buildDir;
  
  public WebdriverFunctions(APICoreProperties.Webdriver wdProps,
                            BuildCapability buildCapability,
                            RemoteWebDriver driver,
                            PrintStream printStream,
                            Storage storage,
                            String userAccountBucket,
                            String pathToUploadedFiles,
                            Path buildDir) {
    Preconditions.checkNotNull(wdProps, "wdProps can't be null");
    Preconditions.checkNotNull(buildCapability, "buildCapability can't be null");
    Preconditions.checkNotNull(driver, "driver can't be null");
    Preconditions.checkNotNull(printStream, "printStream can't be null");
    Preconditions.checkNotNull(storage, "storage can't be null");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(userAccountBucket),
        "userAccountBucket can't be empty");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(pathToUploadedFiles),
        "pathToUploadedFiles can't be empty");
    Preconditions.checkNotNull(buildDir, "buildDir can't be null");
    
    this.wdProps = wdProps;
    this.buildCapability = buildCapability;
    this.driver = driver;
    this.printStream = printStream;
    this.storage = storage;
    this.userAccountBucket = userAccountBucket;
    this.pathToUploadedFiles = pathToUploadedFiles;
    this.buildDir = buildDir;
  }
  
  public Set<Function> get() {
    ImmutableSet.Builder<Function> builder = ImmutableSet.builder();
    builder.add(
        // action
        new DragAndDrop(wdProps, buildCapability, driver, printStream),
        new PerformAction(wdProps, buildCapability, driver, printStream),
        new ActionFunctions.Focus(),
        new ActionFunctions.ShiftDown(),
        new ActionFunctions.ShiftUp(),
        new ActionFunctions.CtrlDown(),
        new ActionFunctions.CtrlUp(),
        new ActionFunctions.AltDown(),
        new ActionFunctions.AltUp(),
        new ActionFunctions.CmdDown(),
        new ActionFunctions.CmdUp(),
        new ActionFunctions.SendKeys(),
        new ActionFunctions.Move(),
        new ActionFunctions.Hold(),
        new ActionFunctions.Release(),
        new ActionFunctions.ClickOnce(),
        new ActionFunctions.DoubleClick(),
        new ActionFunctions.ContextClick(),
        new ActionFunctions.Pause(),
        new Scroll(wdProps, buildCapability, driver, printStream),
        // color
        new ColorMatches(wdProps, buildCapability, driver, printStream),
        // context
        new Close(wdProps, buildCapability, driver, printStream),
        new GetAllWinIds(wdProps, buildCapability, driver, printStream),
        new GetCurrentWinId(wdProps, buildCapability, driver, printStream),
        new NewWin(wdProps, buildCapability, driver, printStream),
        new SwitchFrame(wdProps, buildCapability, driver, printStream),
        new SwitchParentFrame(wdProps, buildCapability, driver, printStream),
        new SwitchWin(wdProps, buildCapability, driver, printStream),
        // resize
        new FullScreenWin(wdProps, buildCapability, driver, printStream),
        new GetWinPosition(wdProps, buildCapability, driver, printStream),
        new GetWinSize(wdProps, buildCapability, driver, printStream),
        new MaximizeWin(wdProps, buildCapability, driver, printStream),
        new MoveWinBy(wdProps, buildCapability, driver, printStream),
        new ResizeWinBy(wdProps, buildCapability, driver, printStream),
        new SetWinPosition(wdProps, buildCapability, driver, printStream),
        new SetWinSize(wdProps, buildCapability, driver, printStream),
        // cookies
        new AddCookie(wdProps, buildCapability, driver, printStream),
        new DeleteAllCookies(wdProps, buildCapability, driver, printStream),
        new DeleteCookie(wdProps, buildCapability, driver, printStream),
        new GetCookies(wdProps, buildCapability, driver, printStream),
        new GetNamedCookie(wdProps, buildCapability, driver, printStream),
        // document
        new ExecuteAsyncScript(wdProps, buildCapability, driver, printStream),
        new ExecuteScript(wdProps, buildCapability, driver, printStream),
        new GetPageSource(wdProps, buildCapability, driver, printStream),
        // interaction
        new Clear(wdProps, buildCapability, driver, printStream),
        new ClearAll(wdProps, buildCapability, driver, printStream),
        new Click(wdProps, buildCapability, driver, printStream),
        new ClickAll(wdProps, buildCapability, driver, printStream),
        new ClickNoSwitch(wdProps, buildCapability, driver, printStream),
        new ClickSwitchNew(wdProps, buildCapability, driver, printStream),
        new Submit(wdProps, buildCapability, driver, printStream),
        // interaction.keys
        new SendKeysToPage(wdProps, buildCapability, driver, printStream),
        new SendKeysToPageF(wdProps, buildCapability, driver, printStream),
        new SetFile(wdProps, buildCapability, driver, printStream, storage, userAccountBucket,
            pathToUploadedFiles, buildDir),
        new SetFiles(wdProps, buildCapability, driver, printStream, storage, userAccountBucket,
            pathToUploadedFiles, buildDir),
        new Type(wdProps, buildCapability, driver, printStream),
        new TypeActive(wdProps, buildCapability, driver, printStream),
        new TypeIntoElements(wdProps, buildCapability, driver, printStream),
        new TypeUsingMap(wdProps, buildCapability, driver, printStream),
        // retrieval
        new ActiveElement(wdProps, buildCapability, driver, printStream),
        new FindElement(wdProps, buildCapability, driver, printStream),
        new FindElementFromElement(wdProps, buildCapability, driver, printStream),
        new FindElements(wdProps, buildCapability, driver, printStream),
        new FindElementsFromElement(wdProps, buildCapability, driver, printStream),
        new FindElementsWithSelectors(wdProps, buildCapability, driver, printStream),
        new FindElementWithSelectors(wdProps, buildCapability, driver, printStream),
        // state
        new AllElementsDisplayed(wdProps, buildCapability, driver, printStream),
        new AllElementsEnabled(wdProps, buildCapability, driver, printStream),
        new AllElementsSelected(wdProps, buildCapability, driver, printStream),
        new AnyElementDisplayed(wdProps, buildCapability, driver, printStream),
        new AnyElementEnabled(wdProps, buildCapability, driver, printStream),
        new AnyElementSelected(wdProps, buildCapability, driver, printStream),
        new CaptureElementScreenshot(wdProps, buildCapability, driver, printStream),
        new ElementExists(wdProps, buildCapability, driver, printStream),
        new GetElementAttribute(wdProps, buildCapability, driver, printStream),
        new GetElementAttributeOrCssValue(wdProps, buildCapability, driver, printStream),
        new GetElementCssValue(wdProps, buildCapability, driver, printStream),
        new GetElementRect(wdProps, buildCapability, driver, printStream),
        new GetElementText(wdProps, buildCapability, driver, printStream),
        new GetElementValue(wdProps, buildCapability, driver, printStream),
        new GetElementViewportCoordinates(wdProps, buildCapability, driver, printStream),
        new GetTagName(wdProps, buildCapability, driver, printStream),
        new IsElementDisplayed(wdProps, buildCapability, driver, printStream),
        new IsElementEnabled(wdProps, buildCapability, driver, printStream),
        new IsElementSelected(wdProps, buildCapability, driver, printStream),
        new IsStale(wdProps, buildCapability, driver, printStream),
        // navigation
        new Back(wdProps, buildCapability, driver, printStream),
        new Forward(wdProps, buildCapability, driver, printStream),
        new GetCurrentUrl(wdProps, buildCapability, driver, printStream),
        new GetTitle(wdProps, buildCapability, driver, printStream),
        new OpenUrl(wdProps, buildCapability, driver, printStream),
        new OpenUrlNewWin(wdProps, buildCapability, driver, printStream),
        new Refresh(wdProps, buildCapability, driver, printStream),
        // prompts
        new AcceptAlert(wdProps, buildCapability, driver, printStream),
        new DismissAlert(wdProps, buildCapability, driver, printStream),
        new GetAlertText(wdProps, buildCapability, driver, printStream),
        new SendAlertText(wdProps, buildCapability, driver, printStream),
        // select
        new DeselectAll(wdProps, buildCapability, driver, printStream),
        new DeselectByIndex(wdProps, buildCapability, driver, printStream),
        new DeselectByValue(wdProps, buildCapability, driver, printStream),
        new DeselectByVisibleText(wdProps, buildCapability, driver, printStream),
        new GetAllSelectedOptions(wdProps, buildCapability, driver, printStream),
        new GetFirstSelectedOption(wdProps, buildCapability, driver, printStream),
        new GetOptions(wdProps, buildCapability, driver, printStream),
        new IsMultiple(wdProps, buildCapability, driver, printStream),
        new SelectByIndex(wdProps, buildCapability, driver, printStream),
        new SelectByValue(wdProps, buildCapability, driver, printStream),
        new SelectByVisibleText(wdProps, buildCapability, driver, printStream),
        // storage
        new LsClear(wdProps, buildCapability, driver, printStream),
        new LsGetItem(wdProps, buildCapability, driver, printStream),
        new LsGetKeySet(wdProps, buildCapability, driver, printStream),
        new LsRemoveItem(wdProps, buildCapability, driver, printStream),
        new LsSetItem(wdProps, buildCapability, driver, printStream),
        new LsSize(wdProps, buildCapability, driver, printStream),
        new SsClear(wdProps, buildCapability, driver, printStream),
        new SsGetItem(wdProps, buildCapability, driver, printStream),
        new SsGetKeySet(wdProps, buildCapability, driver, printStream),
        new SsRemoveItem(wdProps, buildCapability, driver, printStream),
        new SsSetItem(wdProps, buildCapability, driver, printStream),
        new SsSize(wdProps, buildCapability, driver, printStream),
        // timeout
        new SetElementAccessTimeout(wdProps, buildCapability, driver, printStream),
        new SetPageLoadTimeout(wdProps, buildCapability, driver, printStream),
        new SetScriptTimeout(wdProps, buildCapability, driver, printStream),
        new GetTimeout(wdProps, buildCapability, driver, printStream),
        // until expectation
        new UntilAlertPresent(wdProps, buildCapability, driver, printStream),
        new UntilAllDisabled(wdProps, buildCapability, driver, printStream),
        new UntilAllEnabled(wdProps, buildCapability, driver, printStream),
        new UntilAllInvisible(wdProps, buildCapability, driver, printStream),
        new UntilAllRemoved(wdProps, buildCapability, driver, printStream),
        new UntilAllSelectionsAre(wdProps, buildCapability, driver, printStream),
        new UntilAllVisible(wdProps, buildCapability, driver, printStream),
        new UntilAnySelectionIs(wdProps, buildCapability, driver, printStream),
        new UntilAttributeValueContains(wdProps, buildCapability, driver, printStream),
        new UntilAttributeValueIs(wdProps, buildCapability, driver, printStream),
        new UntilAttributeValueLike(wdProps, buildCapability, driver, printStream),
        new UntilAttributeValueNonEmpty(wdProps, buildCapability, driver, printStream),
        new UntilClickable(wdProps, buildCapability, driver, printStream),
        new UntilDisabled(wdProps, buildCapability, driver, printStream),
        new UntilEnabled(wdProps, buildCapability, driver, printStream),
        new UntilFlt(),
        new UntilInvisible(wdProps, buildCapability, driver, printStream),
        new UntilJsReturnsAValue(wdProps, buildCapability, driver, printStream),
        new UntilJsThrowsNoException(wdProps, buildCapability, driver, printStream),
        new UntilRemoved(wdProps, buildCapability, driver, printStream),
        new UntilSelectionIs(wdProps, buildCapability, driver, printStream),
        new UntilStale(wdProps, buildCapability, driver, printStream),
        new UntilSwitchedToFrame(wdProps, buildCapability, driver, printStream),
        new UntilTextContains(wdProps, buildCapability, driver, printStream),
        new UntilTextIs(wdProps, buildCapability, driver, printStream),
        new UntilTextLike(wdProps, buildCapability, driver, printStream),
        new UntilTextNonEmpty(wdProps, buildCapability, driver, printStream),
        new UntilTitleContains(wdProps, buildCapability, driver, printStream),
        new UntilTitleIs(wdProps, buildCapability, driver, printStream),
        new UntilTotalElementsEQ(wdProps, buildCapability, driver, printStream),
        new UntilTotalElementsGT(wdProps, buildCapability, driver, printStream),
        new UntilTotalElementsLT(wdProps, buildCapability, driver, printStream),
        new UntilTotalWindowsAre(wdProps, buildCapability, driver, printStream),
        new UntilUrlContains(wdProps, buildCapability, driver, printStream),
        new UntilUrlIs(wdProps, buildCapability, driver, printStream),
        new UntilUrlLike(wdProps, buildCapability, driver, printStream),
        new UntilValueContains(wdProps, buildCapability, driver, printStream),
        new UntilValueIs(wdProps, buildCapability, driver, printStream),
        new UntilValueLike(wdProps, buildCapability, driver, printStream),
        new UntilValueNonEmpty(wdProps, buildCapability, driver, printStream),
        new UntilVisible(wdProps, buildCapability, driver, printStream),
        // util
        new Sleep(wdProps, buildCapability, driver, printStream),
        new IsValidElemId(wdProps, buildCapability, driver, printStream)
    );
    return builder.build();
  }
}
