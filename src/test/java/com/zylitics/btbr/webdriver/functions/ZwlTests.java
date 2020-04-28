package com.zylitics.btbr.webdriver.functions;

public enum ZwlTests {
  
  ACTIONS_TEST ("ActionsTest.zwl"),
  BASIC_WD_TEST ("BasicWdTest.zwl"),
  COLOR_TEST ("ColorTest.zwl"),
  CONTEXT_TEST ("ContextTest.zwl"),
  COOKIE_TEST ("CookieTest.zwl"),
  DOCUMENT_TEST ("DocumentTest.zwl"),
  E_INTERACTION_KEYS_TEST ("EInteractionKeysTest.zwl"),
  E_INTERACTION_TEST ("EInteractionTest.zwl"),
  ELEMENT_RETRIEVAL_TEST ("ElementRetrievalTest.zwl"),
  ELEMENT_STATE_TEST ("ElementStateTest.zwl"),
  NAVIGATION_TEST ("NavigationTest.zwl"),
  PROMPTS_TEST ("PromptsTest.zwl"),
  SELECT_TEST ("SelectTest.zwl"),
  SET_FILES_TEST ("SetFilesTest.zwl"),
  STORAGE_TEST ("StorageTest.zwl"),
  TIMEOUT_TEST ("TimeoutTest.zwl"),
  UNTIL_TEST ("UntilTest.zwl"),
  ELEMENT_CAPTURE_TEST ("ElementCaptureTest.zwl");
  
  private final String file;
  
  ZwlTests(String file) {
    this.file = file;
  }
  
  public String getFile() { return file; }
}
