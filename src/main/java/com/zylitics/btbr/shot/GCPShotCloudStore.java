package com.zylitics.btbr.shot;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.zylitics.btbr.config.APICoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

final class GCPShotCloudStore implements ShotCloudStore {
  
  private static final Logger LOG = LoggerFactory.getLogger(GCPShotCloudStore.class);
  
  private static final int MAX_BACKOFF_SEC = 32;
  
  private static final int MAX_REATTEMPTS = 10;
  
  private final String bucket;
  private final APICoreProperties.Shot shotProps;
  private final Storage storage;
  
  private volatile boolean closed = false;
  
  GCPShotCloudStore(String bucket, APICoreProperties.Shot shotProps, Storage storage) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(bucket), "bucket can't be empty");
    Preconditions.checkNotNull(shotProps, "shotProps can't be null");
    Preconditions.checkNotNull(storage, "storage can't be null");
    
    this.bucket = bucket;
    this.shotProps = shotProps;
    this.storage = storage;
  }
  
  // could be accessed by more than one thread at a time.
  @Override
  public boolean storeShot(String name, InputStream stream) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "shot name can't be empty");
    Preconditions.checkNotNull(stream, "shot stream can't be null");
    
    if (closed) {
      return false;
    }
    // our shots are ~1MB, prefer uploading in chunks as described here,
    // https://github.com/googleapis/google-cloud-java/blob/b47858f62e4944ca2efc4301e2e0a1c5f22dc728
    // /google-cloud-examples/src/main/java/com/google/cloud/examples/storage/StorageExample.java#L295
    // Set a cache control header so that client browser caches a downloaded shot.
    BlobInfo blobInfo = BlobInfo.newBuilder(bucket, name)
        .setContentType(shotProps.getContentType()).setCacheControl("max-age=31536000").build();
    String blobUploadErrInfo = ", blob " + name;
    
    int reattempts = 0;
    int backOff = 1;
    while (reattempts < MAX_REATTEMPTS) {
      reattempts += 1;
      // if try block throws exception together with resource statement, resource statement's
      // exception will be suppressed. We'll try to catch block's exception within block.
      // https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html#suppressed-exceptions
      
      // While acquiring a WriteChannel, a new blob is created at GCP and it's content can be
      // written via this channel. see com.google.cloud.storage.StorageImpl.writer. An instance of
      // BlobWriteChannel is created. See BlobWriteChannel.open where the new blob creation is
      // handled with reattempts and a Storage Exception is thrown if that fails.
      try (WriteChannel writer = storage.writer(blobInfo)) {
        byte[] buffer = new byte[1024];
        int limit;
        try {
          while ((limit = stream.read(buffer)) >= 0) {
            // see com.google.cloud.BaseWriteChannel.write method. Once a WriteChannel is opened,
            // this method writes the available bytes from buffer. No GCP related exception can
            // throw during the write operation. We may expect only 'channel closed' issue during
            // the write in which case an IOException is thrown.
            writer.write(ByteBuffer.wrap(buffer, 0, limit));
          }
          return true;
        } catch (IOException io) {
          // TODO: see if we need to reattempt on this after working on logs, I feel on abrupt
          //  channel closing due to any reason like network issue should already be handled by the
          //  client, but if not, we can try opening channel again by 'not returning'.
          LOG.error("IOException exception during channel write, not reattempting and quitting" +
              blobUploadErrInfo, io);
          break; // TODO: for now assume this is non-recoverable
        } catch (Exception ex) {
          // non-recoverable issue such as buffer underflow, stream read error etc, abort everything
          LOG.error("unexpected exception during channel write, quitting" + blobUploadErrInfo, ex);
          break;
        }
      } catch (StorageException se) {
        // handle gcp specific errors, note that we can get StorageException during channel closure
        // too, see BaseWriteChannel.close and BlobWriteChannel.flushBuffer
        LOG.error(se.getMessage(), se);
        if (!se.isRetryable()) {
          // see StorageException.RETRYABLE_ERRORS to see what errors are marked for retry. See all
          // statuses here https://cloud.google.com/storage/docs/json_api/v1/status-codes
          break; // give up as we can't retry
          // TODO: same as below, we'll have to see whether we're giving up in response to a 'close'
          //  exception, keep a watch on log. We shouldn't give up on close exception cause a
          //  closure it just for one shot.
          
          // TODO: we might've to retry on some non-retry-able marked errors too after seeing errors
        }
        // go on to retry exponentially
      } catch (IOException io) {
        // during channel close, an IOException is occurred. This could be normal closure after blob
        // is uploaded is during write a abrupt closure was initiated. If this happen during write,
        // the write operation should get an IOException as well. If happen after
        // write, we may ignore this. But I'm uncertain about this, thus I'm going to call it a
        // failure for now and will decide what to do after seeing logs.
        LOG.error("Exception occurred during closing of storage's WriteChannel, calling it a" +
            " failure and quitting " +blobUploadErrInfo, io);
        // TODO: review this after working on logs
        break;
      } catch (Exception ex) {
        // shouldn't reach here
        LOG.error("unexpected exception during upload, quitting" + blobUploadErrInfo, ex);
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
      LOG.error("Max reattempt reached while uploading" + blobUploadErrInfo);
    }
    closed = true;
    return false;
  }
  
  public static class Factory implements ShotCloudStore.Factory {
  
    @Override
    public ShotCloudStore create(String bucket, APICoreProperties.Shot shotProps, Storage storage) {
      return new GCPShotCloudStore(bucket, shotProps, storage);
    }
  }
}
