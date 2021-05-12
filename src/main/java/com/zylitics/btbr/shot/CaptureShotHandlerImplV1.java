package com.zylitics.btbr.shot;

import com.google.cloud.storage.Storage;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.ShotMetadata;
import com.zylitics.btbr.runner.CaptureShotHandler;
import com.zylitics.btbr.runner.CurrentTestVersion;
import com.zylitics.btbr.runner.provider.ShotMetadataProvider;
import com.zylitics.btbr.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public final class CaptureShotHandlerImplV1 implements CaptureShotHandler {
  
  private static final Logger LOG = LoggerFactory.getLogger(CaptureShotHandlerImpl.class);
  
  private final ShotMetadataProvider shotMetadataProvider;
  
  private final Build build;
  
  private final String sessionKey;
  
  private final ShotNameProvider shotNameProvider;
  
  private final CurrentTestVersion currentTestVersion;
  
  private final CaptureDevice captureDevice;
  
  private final ShotCloudStore shotCloudStore;
  
  private final APICoreProperties.Shot shotProps;
  
  private final Thread shotCaptureThread;
  
  private long shotIncrement = 0;
  
  private volatile boolean stop = false;
  
  private volatile boolean errorProcessed = false;
  
  private final CountDownLatch shotCaptureThreadLatch = new CountDownLatch(1);
  
  private final ByteArrayInputStream eosImageBytes;
  
  private CaptureShotHandlerImplV1(APICoreProperties.Shot shotProps,
                                 ShotMetadataProvider shotMetadataProvider,
                                 Storage storage,
                                 Build build,
                                 String sessionKey,
                                 CurrentTestVersion currentTestVersion,
                                 ByteArrayInputStream eosImageBytes) {
    this(shotProps,
        shotMetadataProvider,
        build,
        sessionKey,
        currentTestVersion,
        CaptureDevice.Factory.getDefault().create(shotProps.getExt()),
        ShotCloudStore.Factory.getDefault().create(build.getShotBucketSessionStorage(), shotProps,
            storage),
        eosImageBytes);
  }
  
  CaptureShotHandlerImplV1(APICoreProperties.Shot shotProps,
                         ShotMetadataProvider shotMetadataProvider,
                         Build build,
                         String sessionKey,
                         CurrentTestVersion currentTestVersion,
                         CaptureDevice captureDevice,
                         ShotCloudStore shotCloudStore,
                         ByteArrayInputStream eosImageBytes) {
    this.shotProps = shotProps;
    this.shotMetadataProvider = shotMetadataProvider;
    this.build = build;
    this.sessionKey = sessionKey;
    shotNameProvider = new ShotNameProvider(sessionKey, build.getBuildKey(), shotProps.getExt());
    this.currentTestVersion = currentTestVersion;
    this.captureDevice = captureDevice;
    captureDevice.init();
    this.shotCloudStore = shotCloudStore;
    shotCaptureThread = new Thread(new ShotCaptureThread(), "shot_capture_thread_" +
        build.getBuildId());
    shotCaptureThread.setUncaughtExceptionHandler((t, e) -> LOG.error(e.getMessage(), e));
    this.eosImageBytes = eosImageBytes;
  }
  
  @Override
  public void startShot() {
    // start thread if not yet, as everything is set. This starts taking shots.
    if (shotCaptureThread.getState() == Thread.State.NEW) {
      shotCaptureThread.start();
    } else {
      LOG.warn("Thread ShotCaptureThread is already running, calling start more than once?");
    }
  }
  
  @Override
  public void stopShot() {
    stop = true;
  }
  
  private void capture() {
    try {
      // capture and put shots in the same thread
      InputStream shotStream = captureDevice.captureFull().getShotInputStream();
      shotIncrement+= 1;
      ShotMetadata metadata = getShotMetadata(Long.toString(shotIncrement),
          DateTimeUtil.getCurrentUTC());
      processShot(shotStream, metadata);
    } catch (Exception e) {
      // An exception has occurred while capturing shots. This is mostly in capturing shots because
      // none other code throws an exception but handles intrinsically.
      // Exception in capturing shouldn't happen and if it does, it
      // would mean we didn't test a new windows version, new patch etc before bringing that to
      // production, thus I am handling this exception correctly by sending an error shot etc. This
      // is being done to limit the number of places that may have problems during shot capture
      // leading to less brittle code. This is being considered a rare case that will require
      // a quick resolution.
      // TODO: we must write integration test on production machines and incorporate them in
      //  automation tests of runners so that shot feature is tested whenever there is update to
      //  runner's resources.
      Thread.currentThread().interrupt();
      LOG.error("An unexpected error occurred while capturing shots, bringing system down", e);
      // just close and don't worry about adding a error shot to executor, this is a an
      // exceptional case that is not handled fully here, deliberately.
    }
  }
  
  @Override
  public void blockUntilFinish() {
    try {
      shotCaptureThreadLatch.await(); // let shot capture thread complete it's execution
      if (!errorProcessed) {
        saveEOSShot();
      }
    } catch (InterruptedException e) {
      LOG.error("Interrupted while waiting for shots process to finish", e);
    } finally {
      // let db-store flush it's pending records after and EOS or ERROR is processed.
      shotMetadataProvider.processRemainingAndTearDown();
    }
  }
  
  // accessed by just one thread
  private void saveEOSShot() {
    ShotMetadata metadata = getShotMetadata(shotProps.getEosShot(), DateTimeUtil.getCurrentUTC());
    if (eosImageBytes.available() == 0) {
      eosImageBytes.reset();
    }
    processShot(eosImageBytes, metadata);
  }
  
  private void processShot(InputStream stream, ShotMetadata metadata) {
    LOG.debug("storing shot {} to cloud", metadata.getShotName());
    if (!shotCloudStore.storeShot(metadata.getShotName(), stream)) {
      // Shot failed to store, this indicates we've some problem and have to stop everything.
      // If this invocation is for a normal shot (other than EOS/ERROR), create new metadata for
      // an ERROR and save in db otherwise we'll save what is provided (If EOS can't be saved, we
      // will save same metadata, so that we know an EOS was reached which means success)
      ShotMetadata metadataOnError = metadata;
      String shotIdentifier = shotNameProvider.getIdentifier(metadata.getShotName());
      if (!(shotIdentifier.equals(shotProps.getErrorShot())
          || shotIdentifier.equals(shotProps.getEosShot()))) {
        metadataOnError = getShotMetadata(shotProps.getErrorShot(), DateTimeUtil.getCurrentUTC());
      }
      shotMetadataProvider.saveAsync(metadataOnError);
      errorProcessed = true;  // error handled, mark it processed.
      // close db store now, so that it can flush any pending records and close down.
      shotMetadataProvider.processRemainingAndTearDown();
      // stop taking more shots, if this call is made after stopping shots, it doesn't have any
      // effect.
      stop = true;
      LOG.error("shot process couldn't save shot: {} to cloud and shutting down",
          metadata.getShotName());
      return;
    }
    LOG.debug("stored shot {} to cloud, going to push into db", metadata.getShotName());
    // I've tested that when this submission leads to bulk trigger, executor's thread returns
    // and next task is run, the bulk execution is handled by another thread at db store.
    shotMetadataProvider.saveAsync(metadata);
  }
  
  private ShotMetadata getShotMetadata(String shotIdentifier, OffsetDateTime dateTime) {
    return new ShotMetadata()
        .setShotName(shotNameProvider.getName(shotIdentifier))
        .setBuildId(build.getBuildId())
        .setTestVersionId(currentTestVersion.getTestVersionId())
        .setBuildKey(build.getBuildKey())
        .setSessionKey(sessionKey)
        .setAtLineZwl(currentTestVersion.getControlAtLineInProgram())
        .setCreateDate(dateTime);
  }
  
  class ShotCaptureThread implements Runnable {
    
    @Override
    public void run() {
      while (!stop && !Thread.interrupted()) {
        capture();
      }
      // release threads waiting on this thread
      shotCaptureThreadLatch.countDown();
    }
  }
  
  public static class Factory implements CaptureShotHandler.Factory {
  
    // Taken here rather than in Impl's constructor cause we instantiate it once per application
    // run while impl gets instantiated per request.
    private final ByteArrayInputStream eosImageBytes;
    
    public Factory() {
      eosImageBytes = new ByteArrayInputStream(readEOSImage());
    }
  
    private byte[] readEOSImage() {
      try {
        String path = "/EOS.png";
        URL url = getClass().getResource(path);
        Objects.requireNonNull(url);
        //noinspection UnstableApiUsage
        return Resources.toByteArray(url);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    
    @Override
    public CaptureShotHandler create(APICoreProperties.Shot shotProps,
                                     ShotMetadataProvider shotMetadataProvider, Storage storage,
                                     Build build, String sessionKey,
                                     CurrentTestVersion currentTestVersion) {
      Preconditions.checkNotNull(shotProps, "shotProps can't be null");
      Preconditions.checkNotNull(shotMetadataProvider, "shotMetadataProvider can't be null");
      Preconditions.checkNotNull(storage, "storage can't be null");
      Preconditions.checkNotNull(build, "build can't be null");
      Preconditions.checkArgument(!Strings.isNullOrEmpty(sessionKey), "sessionKey can't be empty");
      Preconditions.checkNotNull(currentTestVersion, "currentTestVersion can't be null");
      
      return new CaptureShotHandlerImplV1(shotProps, shotMetadataProvider, storage, build,
          sessionKey, currentTestVersion, eosImageBytes);
    }
  }
}
