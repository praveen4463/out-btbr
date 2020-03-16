package com.zylitics.btbr.webdriver.functions.elements.interaction.keys;

import com.google.cloud.storage.Storage;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class SetFile extends AbstractWebdriverFunction {
  
  private final Storage storage;
  
  private final String userAccountBucket;
  
  private final String pathToUploadedFiles;
  
  /**
   *
   * @param pathToUploadedFiles should be a string containing subdirectories that keep user
   *                            uploaded files. Subdirectories are separated by a forward slash
   *                            , such as 1002/uploads where 1002 is userId.
   */
  public SetFile(APICoreProperties.Webdriver wdProps,
                 BuildCapability buildCapability,
                 RemoteWebDriver driver,
                 PrintStream printStream,
                 Storage storage,
                 String userAccountBucket,
                 String pathToUploadedFiles) {
    super(wdProps, buildCapability, driver, printStream);
    this.storage = storage;
    this.userAccountBucket = userAccountBucket;
    this.pathToUploadedFiles = pathToUploadedFiles;
  }
  
  @Override
  public String getName() {
    return "setFile";
  }
  
  @Override
  public int minParamsCount() {
    return 2;
  }
  
  @Override
  public int maxParamsCount() {
    return 2;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
  
    writeCommandUpdate(withArgsCommandUpdateText(args));
    int argsCount = args.size();
    
    if (argsCount == 2) {
      RemoteWebElement element = getElement(tryCastString(0, args.get(0)));
      String fileOnCloud = args.get(1).toString();
      // don't case to string, may be possible the file is named like 322323 with no extension and
      // user sent it that way.
      String localFilePathAfterDownload =
          new FileInputFilesProcessor(storage, userAccountBucket, pathToUploadedFiles,
              Collections.singleton(fileOnCloud)).process().iterator().next();
      return handleWDExceptions(() -> {
        element.sendKeys(localFilePathAfterDownload);
        return _void;
      });
    }
    
    throw unexpectedEndOfFunctionOverload(argsCount);
  }
}
