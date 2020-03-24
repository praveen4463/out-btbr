package com.zylitics.btbr.webdriver.functions.cookies;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.*;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractGetCookie extends AbstractWebdriverFunction {
  
  public AbstractGetCookie(APICoreProperties.Webdriver wdProps,
                    BuildCapability buildCapability,
                    RemoteWebDriver driver,
                    PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  Map<String, ZwlValue> cookieToZwlMap(Cookie c) {
    Preconditions.checkNotNull(c, "Cookie can't be null");
    
    Map<String, ZwlValue> m = new HashMap<>();
    m.put("name", new StringZwlValue(c.getName()));
    m.put("value", new StringZwlValue(c.getValue()));
    m.put("domain",
        c.getDomain() != null ? new StringZwlValue(c.getDomain()) : new NothingZwlValue());
    m.put("path", new StringZwlValue(c.getPath())); // path is default set when null
    ZwlValue expiry = new NothingZwlValue();
      /*
      Normally a user would send a date as expiry date and the same date is returned when getting
      the cookie, but in ZWL we ask user to send millis after which cookie should expire. When
      getting the same cookie, if we try returning the same millis sent during cookie creation,
      user can't understand the exact expiry date because those millis are meant to be calculated
      since a past date (when cookie was created), thus we should send back millis remaining from
      now until the cookie will expire. This can be retrieved by getting the millis expiry date
      represent since epoch and subtracting 'current time millis' (since epoch) from it. This is
      actually the time difference between expiry date and now. It may be negative if cookie was
      already expired.
       */
    Date expiryAsDate = c.getExpiry();
    if (expiryAsDate != null) {
      expiry = new DoubleZwlValue(expiryAsDate.getTime() - System.currentTimeMillis());
    }
    m.put("expiry", expiry);
    m.put("secure", new BooleanZwlValue(c.isSecure()));
    m.put("httpOnly", new BooleanZwlValue(c.isHttpOnly()));
    return m;
  }
}
