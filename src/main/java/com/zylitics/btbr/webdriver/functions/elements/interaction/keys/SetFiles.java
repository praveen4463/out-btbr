package com.zylitics.btbr.webdriver.functions.elements.interaction.keys;

import com.google.cloud.storage.Storage;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.webdriver.functions.AbstractWebdriverFunction;
import com.zylitics.zwl.datatype.ZwlValue;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Meant to be used only when the file input's multiple attribute is set to 'true'.
 */
public class SetFiles extends AbstractWebdriverFunction {
  
  private final Storage storage;
  
  private final String userAccountBucket;
  
  private final String pathToUploadedFiles;
  
  private final Path buildDir;

  public SetFiles(APICoreProperties.Webdriver wdProps,
                 BuildCapability buildCapability,
                 RemoteWebDriver driver,
                 PrintStream printStream,
                 Storage storage,
                 String userAccountBucket,
                 String pathToUploadedFiles,
                  Path buildDir) {
    super(wdProps, buildCapability, driver, printStream);
    this.storage = storage;
    this.userAccountBucket = userAccountBucket;
    this.pathToUploadedFiles = pathToUploadedFiles;
    this.buildDir = buildDir;
  }
  
  @Override
  public String getName() {
    return "setFiles";
  }
  
  @Override
  public int minParamsCount() {
    return 2;
  }
  
  @Override
  public int maxParamsCount() {
    return Integer.MAX_VALUE;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    int argsCount = args.size();
    if (argsCount < 2) {
      throw unexpectedEndOfFunctionOverload(argsCount);
    }
    RemoteWebElement element = getElement(tryCastString(0, args.get(0)));
    Set<String> filesOnCloud = args.subList(1, argsCount)
        .stream().map(Objects::toString).collect(Collectors.toSet());
    // don't cast to string, may be possible the file is named like 322323 with no extension and
    // user sent it that way.
    Set<String> localFilePathsAfterDownload =
        new FileInputFilesProcessor(storage, userAccountBucket, pathToUploadedFiles,
            filesOnCloud, buildDir, lineNColumn).process();
    return handleWDExceptions(() -> {
      element.sendKeys(String.join("\n", localFilePathsAfterDownload));
      // per the spec https://w3c.github.io/webdriver/#dfn-dispatch-actions-for-a-string
      // , concat file paths with newline character.
      return _void;
    });
  }
}
