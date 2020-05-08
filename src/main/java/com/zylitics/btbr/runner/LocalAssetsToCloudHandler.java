package com.zylitics.btbr.runner;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.common.base.Preconditions;
import com.zylitics.btbr.config.APICoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.*;

public class LocalAssetsToCloudHandler {
  
  private static final Logger LOG = LoggerFactory.getLogger(LocalAssetsToCloudHandler.class);
  
  private static final String LOG_FILE_CONTENT_TYPE = "text/plain";
  
  private static final int MAX_BACKOFF_SEC = 8;
  
  private static final int MAX_REATTEMPTS = 4;
  
  private final APICoreProperties.Webdriver wdProps;
  
  private final Storage storage;
  
  private final Path buildDir;
  
  public LocalAssetsToCloudHandler(APICoreProperties.Webdriver wdProps,
                                   Storage storage,
                                   Path buildDir) {
    Preconditions.checkNotNull(wdProps, "wdProps can't be null");
    Preconditions.checkNotNull(storage, "storage can't be null");
    Preconditions.checkNotNull(buildDir, "buildDir can't be null");
    
    this.wdProps = wdProps;
    this.storage = storage;
    this.buildDir = buildDir;
  }
  
  public void store() {
    if (!Files.isDirectory(buildDir)) {
      throw new RuntimeException("Directory " + buildDir + " doesn't exists. Can't store logs");
    }
    storeElemShots();
  
    // get driver logs
    LOG.debug("Looking for driver logs to store");
    Path driverLogsFile =
        buildDir.resolve(wdProps.getDriverLogsDir()).resolve(wdProps.getDriverLogsFile());
    if (Files.exists(driverLogsFile) && !Files.isDirectory(driverLogsFile)) {
      LOG.debug("Storing driver logs at {}", driverLogsFile);
      BlobInfo blobInfo = BlobInfo.newBuilder(wdProps.getServerLogsBucket(),
          getBlobName(wdProps.getDriverLogsDir(), wdProps.getDriverLogsFile()))
          .setContentType(LOG_FILE_CONTENT_TYPE).build();
      storeBySize(blobInfo, driverLogsFile);
    }
    
    // get client logs
    LOG.debug("Looking for client logs to store");
    Path clientLogsFile =
        buildDir.resolve(wdProps.getInternalLogsDir()).resolve(wdProps.getClientLogsFile());
    if (Files.exists(clientLogsFile) && !Files.isDirectory(clientLogsFile)) {
      LOG.debug("Storing client logs at {}", clientLogsFile);
      BlobInfo blobInfo = BlobInfo.newBuilder(wdProps.getServerLogsBucket(),
          getBlobName(wdProps.getInternalLogsDir(), wdProps.getClientLogsFile()))
          .setContentType(LOG_FILE_CONTENT_TYPE).build();
      storeBySize(blobInfo, clientLogsFile);
    }
  
    // get profiler logs
    LOG.debug("Looking for profiler logs to store");
    Path profilerLogsFile =
        buildDir.resolve(wdProps.getInternalLogsDir()).resolve(wdProps.getProfilerLogsFile());
    if (Files.exists(profilerLogsFile) && !Files.isDirectory(profilerLogsFile)) {
      LOG.debug("Storing profiler logs at {}", profilerLogsFile);
      BlobInfo blobInfo = BlobInfo.newBuilder(wdProps.getServerLogsBucket(),
          getBlobName(wdProps.getInternalLogsDir(), wdProps.getProfilerLogsFile()))
          .setContentType(LOG_FILE_CONTENT_TYPE).build();
      storeBySize(blobInfo, profilerLogsFile);
    }
  
    // get performance logs
    LOG.debug("Looking for performance logs to store");
    Path perfLogsFile =
        buildDir.resolve(wdProps.getBrowserPerfLogsDir()).resolve(wdProps.getBrowserPerfLogsFile());
    if (Files.exists(perfLogsFile) && !Files.isDirectory(perfLogsFile)) {
      LOG.debug("Storing performance logs at {}", perfLogsFile);
      BlobInfo blobInfo = BlobInfo.newBuilder(wdProps.getServerLogsBucket(),
          getBlobName(wdProps.getBrowserPerfLogsDir(), wdProps.getBrowserPerfLogsFile()))
          .setContentType(LOG_FILE_CONTENT_TYPE).build();
      storeBySize(blobInfo, perfLogsFile);
    }
  }
  
  private void storeElemShots() {
    // get element shots
    LOG.debug("Looking for element shots to store");
    Path elemShotDir = buildDir.resolve(wdProps.getElementShotDir());
    if (!Files.isDirectory(elemShotDir)) {
      return;
    }
    try (DirectoryStream<Path> pathStream = Files.newDirectoryStream(elemShotDir, "*.png")) {
      for (Path path : pathStream) {
        String name = path.getFileName().toString();
        LOG.debug("Storing element shot {}", name);
        BlobInfo blobInfo = BlobInfo.newBuilder(wdProps.getServerLogsBucket()
            , getBlobName(wdProps.getElementShotDir(), name))
            .setContentEncoding("image/png").build();
        storeBySize(blobInfo, path);
      }
    } catch (IOException io) {
      LOG.error("Couldn't list files from dir at " + elemShotDir.toAbsolutePath().toString(), io);
    }
  }
  
  // The directory structure in cloud store should be like:
  // Build_Dir/Asset_Dir/Actual_File
  private String getBlobName(String parentDirName, String fileName) {
    return buildDir + "/" + parentDirName + "/" + fileName;
  }
  
  private void storeBySize(BlobInfo blobInfo, Path file) {
    try {
      if (Files.size(file) > 1000_000) { storeLarge(blobInfo, file); }
      else { storeSmall(blobInfo, file); }
    } catch (IOException io) {
      LOG.error("couldn't get size of file at " + file.toAbsolutePath().toString());
    }
  }
  
  private void storeSmall(BlobInfo blobInfo, Path file) {
    try {
      storage.create(blobInfo, Files.readAllBytes(file));
    } catch (Exception e) {
      LOG.error("File at " + file.toAbsolutePath().toString() + " couldn't be stored.", e);
    }
  }
  
  // similar to what is in GCPShotCloudStore
  private void storeLarge(BlobInfo blobInfo, Path file) {
    String blobUploadErrInfo = ", blob " + blobInfo.getName();
    int reattempts = 0;
    int backOff = 1;
    try (InputStream stream = Files.newInputStream(file)) {
      while (reattempts < MAX_REATTEMPTS) {
        reattempts += 1;
        try (WriteChannel writer = storage.writer(blobInfo)) {
          byte[] buffer = new byte[1024];
          int limit;
          try {
            while ((limit = stream.read(buffer)) >= 0) {
              writer.write(ByteBuffer.wrap(buffer, 0, limit));
            }
            return;
          } catch (Exception ex) {
            LOG.error("Exception during channel write, not reattempting and quitting" +
                blobUploadErrInfo, ex);
            break;
          }
        } catch (StorageException se) {
          LOG.error(se.getMessage(), se);
          if (!se.isRetryable()) {
            break;
          }
          // go on to retry exponentially
        } catch (Exception ex) {
          LOG.error("Exception during upload, quitting" + blobUploadErrInfo, ex);
          break;
        }
        try {
          Thread.sleep(backOff * 1000);
        } catch (InterruptedException ie) {
          // we're asked to quit whole thing
          break;
        }
        if (backOff < MAX_BACKOFF_SEC) {
          backOff = backOff * 2;
          if (backOff > MAX_BACKOFF_SEC) {
            backOff = MAX_BACKOFF_SEC;
          }
        }
      }
      // All reattempts failed, quit.
      if (reattempts == MAX_REATTEMPTS) {
        LOG.error("Max reattempt reached while uploading " + blobUploadErrInfo);
      }
    } catch (IOException io) {
      LOG.error("Couldn't get InputStream from file at " + file.toAbsolutePath().toString());
    }
  }
}
