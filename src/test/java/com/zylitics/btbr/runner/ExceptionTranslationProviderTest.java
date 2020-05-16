package com.zylitics.btbr.runner;

import com.zylitics.zwl.antlr4.StoringErrorListener;
import com.zylitics.zwl.exception.ZwlLangException;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness= Strictness.STRICT_STUBS)
class ExceptionTranslationProviderTest {
  
  private static final Logger LOG = LoggerFactory.getLogger(ExceptionTranslationProviderTest.class);
  
  @DisplayName("webdriver exception with no cause is stripped of the extra details test")
  @Test
  void wdExNoCauseGettingStripped() {
    String msg = "element is not intractable";
    WebDriverException wdEx = new WebDriverException(msg);
    LOG.debug("wedriver exception is: {}", wdEx.getMessage());
    String translated = new ExceptionTranslationProvider(new StoringErrorListener()).get(wdEx);
    assertEquals("WebDriverException: " + msg, translated);
  }
  
  @DisplayName("ZwlLang exception with causes has trace and is stripped of the extra details test")
  @Test
  void zwlLangExWithCauseGettingStripped() {
    String elementNotInteractable = "The element a is not intractable";
    String timeout = "Timeout while waiting for element intractability";
    String lineNColumn = " at line 13:2";
    ElementNotInteractableException elementNotInteractableException =
        new ElementNotInteractableException(elementNotInteractable);
    TimeoutException timeoutException = new TimeoutException(timeout,
        elementNotInteractableException);
    ZwlLangException zwlLangException = new ZwlLangException(lineNColumn, timeoutException);
    String translated = new ExceptionTranslationProvider(new StoringErrorListener())
        .get(zwlLangException).trim();
    LOG.debug(translated);
    String expected = "Exception stack trace:\n" +
        "TimeoutException: " + timeout + "\n" +
        "ElementNotInteractableException: " + elementNotInteractable + lineNColumn;
    assertEquals(expected, translated);
  }
  
  @DisplayName("ZwlLang exception with no causes is returned as-is")
  @Test
  void zwlLangExWithoutCauseComeAsIs() {
    String message = "variable vxn not found";
    String lineNColumn = " at line 13:2";
    ZwlLangException zwlLangException = new ZwlLangException(message + lineNColumn);
    String translated = new ExceptionTranslationProvider(new StoringErrorListener())
        .get(zwlLangException);
    assertEquals("ZwlLangException: " + message + lineNColumn, translated);
  }
  
  @DisplayName("Recognition exception leads to exception message creation using storingListener")
  @Test
  void recognitionExSets() {
    String message = "{ expected";
    RecognitionException r = mock(RecognitionException.class);
    @SuppressWarnings("rawtypes") Recognizer recognizer = mock(Recognizer.class);
    StoringErrorListener storingErrorListener = new StoringErrorListener();
    storingErrorListener.syntaxError(recognizer, null, 12, 32, message, r);
    ZwlLangException zwlLangException = new ZwlLangException(r);
    String translated = new ExceptionTranslationProvider(storingErrorListener).
        get(zwlLangException);
    LOG.debug(translated);
    assertTrue(translated.contains(message));
  }
}
