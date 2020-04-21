package com.zylitics.btbr.webdriver.logs;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.util.IOUtil;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>Captures enabled logs from driver and writes them locally to respective files. Here is how it
 * should be consumed by runner:</p>
 * <ul>
 *   <li>
 *     Runner starts a timer upon the start of build and checks the elapsed time on every invocation
 *     of 'onLineChangeEventHandler'. An 'onLineChangeEventHandler' gets called by ZWL whenever
 *     control changes a line in a ZWL program (code with a test version).
 *   </li>
 *   <li>
 *     After every {@link APICoreProperties.Webdriver#getWaitBetweenLogsCapture()} milli seconds,
 *     and upon the end of the build, runner should invoke {@link WebdriverLogHandler#capture()}.
 *   </li>
 *   <li>
 *     Note that the log capture happens in the same thread that is running the build at runner and
 *     it should be fine. We just need to make sure the
 *     {@link APICoreProperties.Webdriver#getWaitBetweenLogsCapture()} time isn't too small or too
 *     big. If it's big log capture may take longer than usual as it has to invoke I/O.
 *   </li>
 *   <li>
 *     If any error occurs while capturing logs, it won't be relayed to runner. Runner can just
 *     invoke this handler without worrying about exception handling, all exceptions will be
 *     absorbed and the build should run normally. We're assuming build can run even if logs
 *     couldn't be captured. In case of error, handler will silently mark itself 'down' and return
 *     immediately whenever a call to capture is made.
 *   </li>
 * </ul>
 */
public class WebdriverLogHandler {
  
  private static final Logger LOG = LoggerFactory.getLogger(WebdriverLogHandler.class);
  
  private final Logs logs;
  
  private final APICoreProperties.Webdriver wdProps;
  
  private final BuildCapability buildCapability;
  
  private final Path buildDir;
  
  private boolean error = false;
  
  private boolean initDone = false;
  
  private Path perfLogFile;
  
  private Path clientLogFile;
  
  private Path profilerLogFile;
  
  public WebdriverLogHandler(RemoteWebDriver driver,
                             APICoreProperties.Webdriver wdProps,
                             BuildCapability buildCapability,
                             Path buildDir) {
    Preconditions.checkNotNull(driver, "driver can't be null");
    Preconditions.checkNotNull(wdProps, "wdProps can't be null");
    Preconditions.checkNotNull(buildCapability, "buildCapability can't be null");
    Preconditions.checkNotNull(buildDir, "buildDir can't be null");
    
    logs = driver.manage().logs();
    this.wdProps = wdProps;
    this.buildCapability = buildCapability;
    this.buildDir = buildDir;
  }
  
  public void capture() {
    if (!initDone) {
      prepareFilesForWriting();
    }
    if (error) {
      return;
    }
    // get client logs
    LogEntries clientLogs = logs.get(LogType.CLIENT);
    if (clientLogs.getAll().size() > 0) {
      writeLogs(clientLogFile, clientLogs.getAll());
    }
  
    // get profiler logs if enabled
    if (Boolean.getBoolean(wdProps.getEnableProfilerLogsProp())) {
      LogEntries profilerLogs = logs.get(LogType.PROFILER);
      if (profilerLogs.getAll().size() > 0) {
        writeLogs(profilerLogFile, profilerLogs.getAll());
      }
    }
  
    // get performance logs if supported and enabled
    if (buildCapability.getWdBrowserName().equals(BrowserType.CHROME)
        && (buildCapability.isWdChromeEnableNetwork() || buildCapability.isWdChromeEnablePage()))
    {
      LogEntries perfLogs = logs.get(LogType.PERFORMANCE);
      if (perfLogs.getAll().size() > 0) {
        writeLogs(perfLogFile, perfLogs.getAll());
      }
    }
  }
  
  private void prepareFilesForWriting() {
    if (!Files.isDirectory(buildDir)) {
      throw new RuntimeException(buildDir.toAbsolutePath().toString() + " isn't a directory");
    }
    
    try {
      Path internalLogsDir = buildDir.resolve(wdProps.getInternalLogsDir());
      IOUtil.createDir(internalLogsDir);
      Path perfLogsDir = buildDir.resolve(wdProps.getBrowserPerfLogsDir());
      IOUtil.createDir(perfLogsDir);
  
      clientLogFile = internalLogsDir.resolve(wdProps.getClientLogsFile());
      profilerLogFile = internalLogsDir.resolve(wdProps.getProfilerLogsFile());
      perfLogFile = perfLogsDir.resolve(wdProps.getBrowserPerfLogsFile());
      // files will be created only when some logging is enabled.
    } catch (Throwable t) {
      LOG.error(t.getMessage(), t);
      error = true;
    }
    initDone = true;
  }
  
  private void writeLogs(Path file, List<LogEntry> logs) {
    try {
      Files.write(file, logs.stream().map(LogEntry::toString).collect(Collectors.toList())
          , StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE,
          StandardOpenOption.APPEND);
    } catch (IOException io) {
      LOG.error("couldn't write to file, will be taking logging system down.", io);
      error = true;
    }
  }
}
