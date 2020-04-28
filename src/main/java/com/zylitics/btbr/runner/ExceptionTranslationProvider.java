package com.zylitics.btbr.runner;

import com.zylitics.zwl.antlr4.StoringErrorListener;
import com.zylitics.zwl.exception.ZwlLangException;
import org.antlr.v4.runtime.RecognitionException;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class ExceptionTranslationProvider {
  
  private static final Logger LOG = LoggerFactory.getLogger(ExceptionTranslationProvider.class);
  
  private final StoringErrorListener storingErrorListener;
  
  ExceptionTranslationProvider(StoringErrorListener storingErrorListener) {
    this.storingErrorListener = storingErrorListener;
  }
  
  String get(Throwable t) {
    return translateExToUserReadableMsg(t);
  }
  
  private String translateExToUserReadableMsg(Throwable t) {
    StringBuilder msg = new StringBuilder();
    if (t instanceof WebDriverException) {
      // most likely an exception occurred during satinize between tests
      msg.append("A Webdriver exception occurred while cleaning up between each test. Details: ");
      msg.append(formatExClassAndMsg(t));
    } else if (t instanceof ZwlLangException) {
      if (t.getCause() == null) {
        // we can send the exception itself.
        msg.append(formatExClassAndMsg(t));
      } else if (t.getCause() instanceof RecognitionException) {
        // an exception occurred during parsing
        msg.append("An exception occurred during parsing: ");
        String parseError = "line " + storingErrorListener.getLine() + ":" +
            storingErrorListener.getCharPositionInLine() + " " + storingErrorListener.getMsg();
        msg.append(parseError);
        // no exception class like InputMismatch is written
      } else if (t.getCause() instanceof WebDriverException) {
        // when the cause is WebDriverException, it's most likely from our webdriver functions, and
        // the line and column information is the message.
        msg.append(composeWebdriverException(t.getMessage(), (WebDriverException) t.getCause()));
      }
    } else {
      msg.append("An unexpected internal exception has occurred.");
    }
    return msg.toString();
  }
  
  private String composeWebdriverException(String lineNColumn, WebDriverException wdEx) {
    StringBuilder msg = new StringBuilder();
    List<WebDriverException> exStack = new ArrayList<>();
    exStack.add(wdEx);
    while (wdEx.getCause() instanceof WebDriverException) {
      WebDriverException w = (WebDriverException) wdEx.getCause();
      exStack.add(w);
      wdEx = w;
    }
    int exStackSize = exStack.size();
    // from WebdriverException class, we need to strip the extra details added with the last
    // exception in cause chain because that includes server ip, internal class names
    // etc that may be irrelevant for user.
    WebDriverException lastEx = exStack.get(exStackSize - 1);
    String lastExMsg = lastEx.getMessage();
    // strip everything after a line break, which should be there per the code, but if it's not,
    // just get entire string.
    int indexOfLineBreak = lastExMsg.indexOf("\n");
    lastExMsg = lastExMsg.substring(0,
        indexOfLineBreak > -1 ? indexOfLineBreak : lastExMsg.length());
    // check whether the message still contains the extra details selenium added, this may happen
    // when the last exception did have an empty message.
    if (lastExMsg.contains("Build info:") || lastExMsg.contains("System info:")
        || lastExMsg.contains("Driver info:") || lastExMsg.contains("Element info:")
        || lastExMsg.length() == 0) {
      // make a generic message if this happens
      LOG.warn("Last webdriver exception message in cause chain is empty", wdEx);
      lastExMsg = "A Webdriver exception has occurred";
    }
    lastExMsg = formatExClassAndMsg(lastEx, lastExMsg + " at " + lineNColumn);
    
    // check whether the cause chain contain more than more than one exception
    if (exStackSize > 1) {
      msg.append("\nException stack trace:\n");
      for (WebDriverException ex : exStack.subList(0, exStackSize - 1)) {
        msg.append(formatExClassAndMsg(ex));
        msg.append("\n");
      }
    }
    msg.append(lastExMsg);
    return msg.toString();
  }
  
  private String formatExClassAndMsg(Throwable t, String message) {
    return String.format("%s: %s", t.getClass().getSimpleName(), message);
  }
  
  private String formatExClassAndMsg(Throwable t) {
    return formatExClassAndMsg(t, t.getMessage());
  }
}
