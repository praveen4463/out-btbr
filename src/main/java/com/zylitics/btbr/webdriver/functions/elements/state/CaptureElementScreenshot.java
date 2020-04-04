package com.zylitics.btbr.webdriver.functions.elements.state;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.util.IOUtil;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class CaptureElementScreenshot extends AbstractWebdriverFunction {
  
  private static final Logger LOG = LoggerFactory.getLogger(CaptureElementScreenshot.class);
  
  public CaptureElementScreenshot(APICoreProperties.Webdriver wdProps,
                                       BuildCapability buildCapability,
                                       RemoteWebDriver driver,
                                       PrintStream printStream) {
    super(wdProps, buildCapability, driver, printStream);
  }
  
  @Override
  public String getName() {
    return "captureElementScreenshot";
  }
  
  @Override
  public int minParamsCount() {
    return 1;
  }
  
  @Override
  public int maxParamsCount() {
    return 2;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
  
    int argsCount = args.size();
    if (argsCount == 0) {
      throw unexpectedEndOfFunctionOverload(argsCount);
    }
    String elemIdOrSelector = tryCastString(0, args.get(0));
    String fileName = String.format("%s%s", argsCount == 2 ? args.get(1).toString() : "",
        UUID.randomUUID().toString());
    byte[] screenshot =
        handleWDExceptions(() -> getElement(elemIdOrSelector).getScreenshotAs(OutputType.BYTES));
    // Rather than directly uploading to cloud, write locally and push all after the end of build
    // so that test execution don't delay, as we don't need to show these shots to user during build
    try {
      IOUtil.write(screenshot, fileName, wdProps.getElementShotDir()); // doesn't throws if failed.
    } catch (IOException io) {
      writeBuildOutput("WARNING: " + getName() + " took the screenshot but it couldn't be saved" +
          " due to some internal error, your build will continue and won't fail. We will be" +
          " fixing it real quick.");
      LOG.error(io.getMessage(), io);
    }
    return _void;
  }
}
