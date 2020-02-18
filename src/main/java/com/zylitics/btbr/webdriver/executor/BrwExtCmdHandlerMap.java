package com.zylitics.btbr.webdriver.executor;

import com.google.common.collect.ImmutableMap;
import com.zylitics.btbr.webdriver.BrwExtCmd;

import java.util.Map;

class BrwExtCmdHandlerMap {
  
  private final AbstractBrwExtCmdExecutor executor;
  
  BrwExtCmdHandlerMap(AbstractBrwExtCmdExecutor executor) {
    this.executor = executor;
  }
  
  // increase map size when entries are more than 75 (load factor is default .75)
  @SuppressWarnings("UnstableApiUsage")
  Map<String, Runnable> get() {
    return ImmutableMap.<String, Runnable>builderWithExpectedSize(100)
        .put(BrwExtCmd.WAIT_FOR_TITLE, executor::waitForTitle)
        .put(BrwExtCmd.WAIT_FOR_LOCATION, executor::waitForLocation)
        .put(BrwExtCmd.WAIT_FOR_IMAGE_SRC, executor::waitForImageSrc)
        .put(BrwExtCmd.WAIT_FOR_SELECT_OPTIONS, executor::waitForSelectOptions)
        .put(BrwExtCmd.WAIT_FOR_TEXT, executor::waitForText)
        .put(BrwExtCmd.WAIT_FOR_VALUE, executor::waitForValue)
        .put(BrwExtCmd.WAIT_FOR_SELECTION, executor::waitForSelection)
        .put(BrwExtCmd.WAIT_FOR_CHECKED, executor::waitForChecked)
        .put(BrwExtCmd.WAIT_FOR_NOT_CHECKED, executor::waitForNotChecked)
        .put(BrwExtCmd.WAIT_FOR_EDITABLE, executor::waitForEditable)
        .put(BrwExtCmd.WAIT_FOR_NOT_EDITABLE, executor::waitForNotEditable)
        .put(BrwExtCmd.WAIT_FOR_SOMETHING_SELECTED, executor::waitForSomethingSelected)
        .put(BrwExtCmd.WAIT_FOR_ELEMENT_PRESENT, executor::waitForElementPresent)
        .put(BrwExtCmd.WAIT_FOR_COLOR, executor::waitForColor)
    
        .put(BrwExtCmd.VERIFY_TITLE, executor::verifyTitle)
        .put(BrwExtCmd.VERIFY_LOCATION, executor::verifyLocation)
        .put(BrwExtCmd.VERIFY_IMAGE_SRC, executor::verifyImageSrc)
        .put(BrwExtCmd.VERIFY_TEXT, executor::verifyText)
        .put(BrwExtCmd.VERIFY_VALUE, executor::verifyValue)
        .put(BrwExtCmd.VERIFY_SELECTION, executor::verifySelection)
        .put(BrwExtCmd.VERIFY_CHECKED, executor::verifyChecked)
        .put(BrwExtCmd.VERIFY_NOT_CHECKED, executor::verifyNotChecked)
        .put(BrwExtCmd.VERIFY_EDITABLE, executor::verifyEditable)
        .put(BrwExtCmd.VERIFY_NOT_EDITABLE, executor::verifyNotEditable)
        .put(BrwExtCmd.VERIFY_SOMETHING_SELECTED, executor::verifySomethingSelected)
        .put(BrwExtCmd.VERIFY_ELEMENT_PRESENT, executor::verifyElementPresent)
        .put(BrwExtCmd.VERIFY_COLOR, executor::verifyColor)
    
        .put(BrwExtCmd.ASSERT_TITLE, executor::assertTitle)
        .put(BrwExtCmd.ASSERT_LOCATION, executor::assertLocation)
        .put(BrwExtCmd.ASSERT_IMAGE_SRC, executor::assertImageSrc)
        .put(BrwExtCmd.ASSERT_TEXT, executor::assertText)
        .put(BrwExtCmd.ASSERT_VALUE, executor::assertValue)
        .put(BrwExtCmd.ASSERT_SELECTION, executor::assertSelection)
        .put(BrwExtCmd.ASSERT_CHECKED, executor::assertChecked)
        .put(BrwExtCmd.ASSERT_NOT_CHECKED, executor::assertNotChecked)
        .put(BrwExtCmd.ASSERT_EDITABLE, executor::assertEditable)
        .put(BrwExtCmd.ASSERT_NOT_EDITABLE, executor::assertNotEditable)
        .put(BrwExtCmd.ASSERT_SOMETHING_SELECTED, executor::assertSomethingSelected)
        .put(BrwExtCmd.ASSERT_ELEMENT_PRESENT, executor::assertElementPresent)
        .put(BrwExtCmd.ASSERT_COLOR, executor::assertColor)
    
        .put(BrwExtCmd.ASSERT_ALERT, executor::assertAlert)
        .put(BrwExtCmd.ASSERT_CONFIRM, executor::assertConfirm)
        .put(BrwExtCmd.ASSERT_PROMPT, executor::assertPrompt)
        .put(BrwExtCmd.CHOOSE_OK_ON_NEXT_CONFIRMATION, executor::chooseOkOnNextConfirmation)
        .put(BrwExtCmd.CHOOSE_CANCEL_ON_NEXT_CONFIRMATION, executor::chooseCancelOnNextConfirmation)
        .put(BrwExtCmd.ANSWER_ON_NEXT_PROMPT, executor::answerOnNextPrompt)
        .put(BrwExtCmd.CHOOSE_CANCEL_ON_NEXT_PROMPT, executor::chooseCancelOnNextPrompt)
    
        .put(BrwExtCmd.OPEN_AND_WAIT, executor::openAndWait)
        .put(BrwExtCmd.CREATE_TAB, executor::createTab)
        .put(BrwExtCmd.CREATE_WINDOW, executor::createWindow)
        .put(BrwExtCmd.SELECT_FRAME, executor::selectFrame)
        .put(BrwExtCmd.SELECT_WINDOW, executor::selectWindow)
        .put(BrwExtCmd.SELECT_TAB, executor::selectTab)
        .put(BrwExtCmd.CLOSE_TAB, executor::closeTab)
        .put(BrwExtCmd.CLOSE_WINDOW, executor::closeWindow)
        .put(BrwExtCmd.ACCEPT_ON_PAGE_LEAVE_DLG_IF_ANY, executor::acceptOnPageLeaveDlgIfAny)
        .put(BrwExtCmd.CLICK, executor::click)
        .put(BrwExtCmd.SCROLL_TO, executor::scrollTo)
        .put(BrwExtCmd.MOUSE_MOVE, executor::mouseMove)
        .put(BrwExtCmd.DBL_CLICK, executor::dblClick)
        .put(BrwExtCmd.DRAG_DROP, executor::dragDrop)
        .put(BrwExtCmd.SEND_KEYS, executor::sendKeys)
        .put(BrwExtCmd.SELECT, executor::select)
        .put(BrwExtCmd.KEY_DOWN, executor::keyDown)
        .put(BrwExtCmd.MAXIMIZE, executor::maximize)
        .put(BrwExtCmd.FULL_SCREEN, executor::fullScreen)
        .put(BrwExtCmd.DELETE_COOKIE, executor::deleteCookie)
        .put(BrwExtCmd.DELETE_ALL_COOKIES, executor::deleteAllCookies)
        .build();
  }
}
