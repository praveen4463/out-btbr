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
      BlobInfo blobInfo = getBlobInfo(wdProps.getServerLogsBucket(),
          getBlobName(wdProps.getDriverLogsDir(), wdProps.getDriverLogsFile()),
          LOG_FILE_CONTENT_TYPE, false);
      storeBySize(blobInfo, driverLogsFile);
    }
    
    // get client logs
    LOG.debug("Looking for client logs to store");
    Path clientLogsFile =
        buildDir.resolve(wdProps.getInternalLogsDir()).resolve(wdProps.getClientLogsFile());
    if (Files.exists(clientLogsFile) && !Files.isDirectory(clientLogsFile)) {
      LOG.debug("Storing client logs at {}", clientLogsFile);
      BlobInfo blobInfo = getBlobInfo(wdProps.getServerLogsBucket(),
          getBlobName(wdProps.getInternalLogsDir(), wdProps.getClientLogsFile()),
          LOG_FILE_CONTENT_TYPE, false);
      storeBySize(blobInfo, clientLogsFile);
    }
  
    // get profiler logs
    LOG.debug("Looking for profiler logs to store");
    Path profilerLogsFile =
        buildDir.resolve(wdProps.getInternalLogsDir()).resolve(wdProps.getProfilerLogsFile());
    if (Files.exists(profilerLogsFile) && !Files.isDirectory(profilerLogsFile)) {
      LOG.debug("Storing profiler logs at {}", profilerLogsFile);
      BlobInfo blobInfo = getBlobInfo(wdProps.getServerLogsBucket(),
          getBlobName(wdProps.getInternalLogsDir(), wdProps.getProfilerLogsFile()),
          LOG_FILE_CONTENT_TYPE, false);
      storeBySize(blobInfo, profilerLogsFile);
    }
  
    // get performance logs
    LOG.debug("Looking for performance logs to store");
    Path perfLogsFile =
        buildDir.resolve(wdProps.getBrowserPerfLogsDir()).resolve(wdProps.getBrowserPerfLogsFile());
    if (Files.exists(perfLogsFile) && !Files.isDirectory(perfLogsFile)) {
      LOG.debug("Storing performance logs at {}", perfLogsFile);
      BlobInfo blobInfo = getBlobInfo(wdProps.getServerLogsBucket(),
          getBlobName(wdProps.getBrowserPerfLogsDir(), wdProps.getBrowserPerfLogsFile()),
          LOG_FILE_CONTENT_TYPE, false);
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
        BlobInfo blobInfo = getBlobInfo(wdProps.getElemShotsBucket(),
            buildDir.getFileName() + "/" + name, "image/png", true);
        storeBySize(blobInfo, path);
      }
    } catch (IOException io) {
      LOG.error("Couldn't list files from dir at " + elemShotDir.toAbsolutePath(), io);
    }
  }
  
  private BlobInfo getBlobInfo(String bucket, String name, String encoding,
                               boolean contentDisposition) {
    BlobInfo.Builder builder = BlobInfo.newBuilder(bucket, name).setContentEncoding(encoding)
        .setCacheControl("public, max-age=604800, immutable");
    if (contentDisposition) {
      builder.setContentDisposition("attachment; filename=\"" + name + "\"");
    }
    return builder.build();
  }
  
  // The directory structure in cloud store should be like:
  // Build_Dir/Asset_Dir/Actual_File
  private String getBlobName(String parentDirName, String fileName) {
    return buildDir.getFileName() + "/" + parentDirName + "/" + fileName;
  }
  
  private void storeBySize(BlobInfo blobInfo, Path file) {
    try {
      if (Files.size(file) > 1000_000) { storeLarge(blobInfo, file); }
      else { storeSmall(blobInfo, file); }
      LOG.debug("Stored blob " + blobInfo + " from " + file);
    } catch (IOException io) {
      LOG.error("couldn't get size of file at " + file.toAbsolutePath());
    }
  }
  
  private void storeSmall(BlobInfo blobInfo, Path file) {
    try {
      storage.create(blobInfo, Files.readAllBytes(file));
    } catch (Exception e) {
      LOG.error("File at " + file.toAbsolutePath() + " couldn't be stored.", e);
    }
  }
  
  // similar to what is in GCPShotCloudStore
  private void storeLarge(BlobInfo blobInfo, Path file) {
    String blobUploadErrInfo = ", blob " + blobInfo.getName();
    int reattempts = 0;
    long backOff = 1;
    try {
      byte[] bytes = Files.readAllBytes(file);
      while (reattempts < MAX_REATTEMPTS) {
        reattempts += 1;
        try (WriteChannel writer = storage.writer(blobInfo)) {
          try {
            // https://cloud.google.com/storage/docs/best-practices#uploading
            // as per recommendation, not doing chunk transfer but entire chunk at once.
            /*
            Avoid breaking a transfer into smaller chunks if possible and instead upload the entire content in a single chunk. Avoiding chunking removes added latency costs from committed offset queries for each chunk and improves throughput, as well as reducing QPS against Cloud Storage. However, you should consider uploading in chunks when:
            Your source data is being generated dynamically and you want to limit how much of it you need to buffer client-side in case the upload fails.
            Your clients have request size limitations, as is the case for many browsers.
            If your clients receive an error, they can query the server for the commit offset and resume uploading remaining bytes from that offset.
            */
            writer.write(ByteBuffer.wrap(bytes, 0, bytes.length));
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
      LOG.error("Couldn't read file at " + file.toAbsolutePath());
    }
  }
}
