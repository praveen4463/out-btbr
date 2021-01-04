package com.zylitics.btbr.runner;

import com.zylitics.zwl.exception.ZwlLangException;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

class ExceptionTranslationProvider {
  
  private static final Logger LOG = LoggerFactory.getLogger(ExceptionTranslationProvider.class);
  
  String get(Throwable t) {
    return translateExToUserReadableMsg(t);
  }
  
  private String translateExToUserReadableMsg(Throwable t) {
    String msg;
    if (t instanceof WebDriverException) {
      // This may not be called from anywhere yet but let's keep it.
      msg = composeWebdriverException((WebDriverException) t, null);
    } else if (t instanceof ZwlLangException) {
      if (t.getCause() == null) {
        // we can send the exception itself.
        msg = formatExClassAndMsg(t);
      } else if (t.getCause() instanceof WebDriverException) {
        // when the cause is WebDriverException, it's most likely from our webdriver functions, and
        // the line and column information is the message.
        msg = composeWebdriverException((WebDriverException) t.getCause(), t.getMessage());
      } else {
        LOG.warn("Got unexpected ZwlLangException cause " +
            t.getCause().getClass().getSimpleName() + ", forced to send a generic failure message");
        msg = "An unexpected internal exception has occurred.";
      }
    } else {
      msg = "An unexpected internal exception has occurred.";
    }
    return msg;
  }
  
  private String composeWebdriverException(WebDriverException wdEx, @Nullable String lineNColumn) {
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
    String lastExMsg = stripSeleniumExtraExDetail(lastEx.getMessage());
    lastExMsg = formatExClassAndMsg(lastEx, lastExMsg);
    if (lineNColumn != null) {
      lastExMsg += lineNColumn;
    }
    
    // check whether the cause chain contain more than one exception
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
  
  /*
   * org.openqa.selenium.WebDriverException adds some build/system detail to exception message
   * when it doesn't have a cause of the same exception class, this method tries stripping that
   * as it's irrelevant for our users.
   */
  private String stripSeleniumExtraExDetail(String exMessage) {
    // first check whether those extra details are really there
    if (!(exMessage.contains("Build info:") || exMessage.contains("System info:")
        || exMessage.contains("Driver info:") || exMessage.contains("Element info:"))) {
      LOG.warn("Expected extra webdriver exception detail but there was nothing");
      return exMessage;
    }
    // check whether following phrase is present
    int indexOfStripStart = exMessage.indexOf("For documentation on this error");
    if (indexOfStripStart == -1) {
      // if not, following must be there if we're following WebDriverException's code right
      indexOfStripStart = exMessage.indexOf("Build info:");
    }
    if (indexOfStripStart == -1) {
      // if not, there must be something wrong as we're not fully stripping
      LOG.warn("Couldn't find a suitable phrase to strip out extra webdriver exception details");
      // just send some common message
      return "A Webdriver exception has occurred";
    }
    return exMessage.substring(0, indexOfStripStart).trim();
  }
  
  private String formatExClassAndMsg(Throwable t, String message) {
    return String.format("%s: %s", t.getClass().getSimpleName(), message);
  }
  
  private String formatExClassAndMsg(Throwable t) {
    return formatExClassAndMsg(t, t.getMessage());
  }
}
