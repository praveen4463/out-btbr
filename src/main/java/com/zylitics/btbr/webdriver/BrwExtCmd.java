package com.zylitics.btbr.webdriver;

public interface BrwExtCmd {
  
  String WAIT_FOR_TITLE = "waitForTitle";
  String WAIT_FOR_LOCATION = "waitForLocation";
  String WAIT_FOR_IMAGE_SRC = "waitForImageSrc";
  String WAIT_FOR_SELECT_OPTIONS= "waitForSelectOptions";
  String WAIT_FOR_TEXT = "waitForText";
  String WAIT_FOR_VALUE = "waitForValue";
  String WAIT_FOR_SELECTION = "waitForSelection";
  String WAIT_FOR_CHECKED = "waitForChecked";
  String WAIT_FOR_NOT_CHECKED = "waitForNotChecked";
  String WAIT_FOR_EDITABLE = "waitForEditable";
  String WAIT_FOR_NOT_EDITABLE = "waitForNotEditable";
  String WAIT_FOR_SOMETHING_SELECTED = "waitForSomethingSelected";
  String WAIT_FOR_ELEMENT_PRESENT = "waitForElementPresent";
  String WAIT_FOR_COLOR = "waitForColor";
  
  String VERIFY_TITLE = "verifyTitle";
  String VERIFY_LOCATION = "verifyLocation";
  String VERIFY_IMAGE_SRC = "verifyImageSrc";
  String VERIFY_TEXT = "verifyText";
  String VERIFY_VALUE = "verifyValue";
  String VERIFY_SELECTION = "verifySelection";
  String VERIFY_CHECKED = "verifyChecked";
  String VERIFY_NOT_CHECKED = "verifyNotChecked";
  String VERIFY_EDITABLE = "verifyEditable";
  String VERIFY_NOT_EDITABLE = "verifyNotEditable";
  String VERIFY_SOMETHING_SELECTED = "verifySomethingSelected";
  String VERIFY_ELEMENT_PRESENT = "verifyElementPresent";
  String VERIFY_COLOR = "verifyColor";
  
  String ASSERT_TITLE = "assertTitle";
  String ASSERT_LOCATION = "assertLocation";
  String ASSERT_IMAGE_SRC = "assertImageSrc";
  String ASSERT_TEXT = "assertText";
  String ASSERT_VALUE = "assertValue";
  String ASSERT_SELECTION = "assertSelection";
  String ASSERT_CHECKED = "assertChecked";
  String ASSERT_NOT_CHECKED = "assertNotChecked";
  String ASSERT_EDITABLE = "assertEditable";
  String ASSERT_NOT_EDITABLE = "assertNotEditable";
  String ASSERT_SOMETHING_SELECTED = "assertSomethingSelected";
  String ASSERT_ELEMENT_PRESENT = "assertElementPresent";
  String ASSERT_COLOR = "assertColor";
  
  String ASSERT_ALERT = "assertAlert";
  String ASSERT_CONFIRM = "assertConfirm";
  String ASSERT_PROMPT = "assertPrompt";
  String CHOOSE_OK_ON_NEXT_CONFIRMATION = "chooseOkOnNextConfirmation";
  String CHOOSE_CANCEL_ON_NEXT_CONFIRMATION = "chooseCancelOnNextConfirmation";
  String ANSWER_ON_NEXT_PROMPT = "answerOnNextPrompt";
  String CHOOSE_CANCEL_ON_NEXT_PROMPT = "chooseCancelOnNextPrompt";
  
  String OPEN_AND_WAIT = "openAndWait";
  String CREATE_TAB = "createTab";
  String CREATE_WINDOW = "createWindow";
  String SELECT_FRAME = "selectFrame";
  String SELECT_WINDOW = "selectWindow";
  String SELECT_TAB = "selectTab";
  String CLOSE_TAB = "closeTab";
  String CLOSE_WINDOW = "closeWindow";
  String ACCEPT_ON_PAGE_LEAVE_DLG_IF_ANY = "acceptOnPageLeaveDlgIfAny";
  String CLICK = "click";
  String SCROLL_TO = "scrollTo";
  String MOUSE_MOVE = "mouseMove";
  String DBL_CLICK = "dblClick";
  String DRAG_DROP = "dragDrop";
  String SEND_KEYS = "sendKeys";
  String SELECT = "select";
  String KEY_DOWN = "keyDown";
  String KEY_UP = "keyUp";
  String MAXIMIZE = "maximize";
  String FULL_SCREEN = "fullScreen";
  String DELETE_COOKIE = "deleteCookie";
  String DELETE_ALL_COOKIES = "deleteAllCookies";
}
