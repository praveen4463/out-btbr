package com.zylitics.btbr.shot;

import com.google.cloud.storage.Storage;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
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
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
Threading notes: There are 3 threads here, 1. the main thread from client that started webDriver
session, 2. Shot capturing thread, 3. thread inside processShotExecutor that processes shots.
Whenever a non recoverable error occurs in 2 or 3, we want both of them die. Here is an explanation
of how it works.
I'm going to explain the whole process point by point from normal scenarios to error conditions.
To begin, webDriver thread starts 2, 2 starts 3 by giving it shots to process.
  a) In normal scenario, 2 captures shots until DELETE command is issued from client and submits
  captured shots to 3 which would process them. Once a DELETE issued, 2 halts but 3 continues if
  all shots in it hasn't yet processed. When blockUntilFinish is invoked after DELETE was sent,
  it will let 3 complete and blocks by issuing a shutdown followed by await. If 3 completes
  within allotted time, an EOS (end of shot) is saved as shot, db provider is asked to process its
  pending records before returning.
  b) In a), when 3 couldn't complete within allotted time, 3 is halted and all remaining tasks
  are cancelled, an error is logged to tell how many tasks couldn't complete. We let db-store to
  finish because it might have unsaved records. This is a rare exception and not fully handled.
  c) When 2 raises an exception due to problem in capturing shots or capturing their InputStream,
  it halts itself and shuts down processor, This is a rare exception and not fully handled.
  d) During process shot, if an error occurs in saving shot to cloud, we can save just the metadata
  and no shot. Metadata is saved so that we know that an ERROR occurred or EOS happened by looking
  into db. To do this, we check the current shot, if its no ERROR/EOS, it should be normal shot,
  in which case we want to save an ERROR shot metadata, if its ERROR or EOS itself we let that save
  as is. After saved, we mark that we've handled an error situation and close down db and executor.
  The closing of executor will let closing of 2 if its still running because it can't submit more
  shots to it.
 */

public final class CaptureShotHandlerImpl implements CaptureShotHandler {
  
  private static final Logger LOG = LoggerFactory.getLogger(CaptureShotHandlerImpl.class);
  
  private final ShotMetadataProvider shotMetadataProvider;
  
  private final Build build;
  
  private final String sessionKey;
  
  private final CurrentTestVersion currentTestVersion;
  
  private final CaptureDevice captureDevice;
  
  private final ExecutorService processShotExecutor;
  
  private final ShotCloudStore shotCloudStore;
  
  private final APICoreProperties.Shot shotProps;
  
  private final Thread shotCaptureThread;
  
  private long shotIncrement = 0;
  
  private volatile boolean stop = false;
  
  private volatile boolean errorProcessed = false;
  
  private CaptureShotHandlerImpl(APICoreProperties apiCoreProperties,
                                 ShotMetadataProvider shotMetadataProvider,
                                 Storage storage,
                                 Build build,
                                 String sessionKey,
                                 String bucketSessionStorage,
                                 CurrentTestVersion currentTestVersion) {
    Preconditions.checkNotNull(apiCoreProperties, "APICoreProperties can't be null");
    Preconditions.checkNotNull(shotMetadataProvider, "ShotMetadataProvider can't be null");
    Preconditions.checkNotNull(storage, "storage can't be null");
    Preconditions.checkNotNull(build, "Build can't be null");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(sessionKey), "sessionKey can't be empty");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(bucketSessionStorage),
        "bucketSessionStorage can't be empty");
    Preconditions.checkNotNull(currentTestVersion, "currentTestVersion can't be null");
    
    shotProps = apiCoreProperties.getShot();
    this.shotMetadataProvider = shotMetadataProvider;
    this.build = build;
    this.sessionKey = sessionKey;
    this.currentTestVersion = currentTestVersion;
  
    captureDevice = CaptureDevice.Factory.getDefault().create(shotProps.getExt());
    captureDevice.init();
  
    processShotExecutor = Executors.newSingleThreadExecutor(r -> {
      Thread executorThread = new Thread(r, "process_shot_executor_" + build.getBuildId());
      executorThread.setUncaughtExceptionHandler((t, e) -> LOG.error(e.getMessage(), e));
      return executorThread;
    });
  
    shotCloudStore = ShotCloudStore.Factory.getDefault().create(bucketSessionStorage, shotProps,
        storage);
  
    shotCaptureThread = new Thread(new ShotCaptureThread(), "shot_capture_thread_" +
        build.getBuildId());
    shotCaptureThread.setUncaughtExceptionHandler((t, e) -> LOG.error(e.getMessage(), e));
  }
  
  CaptureShotHandlerImpl(APICoreProperties apiCoreProperties,
                  ShotMetadataProvider shotMetadataProvider,
                  Build build,
                  String sessionKey,
                  CurrentTestVersion currentTestVersion,
                  CaptureDevice captureDevice,
                  ExecutorService processShotExecutor,
                  ShotCloudStore shotCloudStore) {
    shotProps = apiCoreProperties.getShot();
    this.shotMetadataProvider = shotMetadataProvider;
    this.build = build;
    this.sessionKey = sessionKey;
    this.currentTestVersion = currentTestVersion;
    this.captureDevice = captureDevice;
    this.processShotExecutor = processShotExecutor;
    this.shotCloudStore = shotCloudStore;
    shotCaptureThread = new Thread(new ShotCaptureThread());
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
      // dedicate shot thread for taking shots only and put the processing of it in an executor
      // so that shots are taken fluently without delay.
      InputStream shotStream = captureDevice.captureFull().getShotInputStream();
      shotIncrement+= 1;
      // attach metadata with each submission so that current request details are added with
      // currently taken shot.
      ShotMetadata metadata = getShotMetadata(Long.toString(shotIncrement),
          DateTimeUtil.getCurrentUTC());
      /*
      processor may be shutdown during attempt to submit a captured shot by current thread
      because of two reasons:
      1. In rare situations when a DELETE is issued and completes so quickly that shot capturing
      thread was inside this method for the total time, it may find the executor turned down
      because blockUntilFinish was invoked post stopShot.
      2. An error was occurred during shot-save in executor's thread, which in turn shutdown the
      executor.
      In both scenarios, we don't want an exception to be logged, 1) is a normal flow so EOS will
      be saved, 2) handled within the executor, additionally we'll let the current thread die.
       */
      if (!processShotExecutor.isShutdown()) {
        processShotExecutor.submit(processShot(shotStream, metadata));
      } else {
        Thread.currentThread().interrupt();
      }
    } catch (Exception e) {
      // An exception has occurred while capturing shots. This shouldn't happen and if it does, it
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
      if (!processShotExecutor.isShutdown()) {
        processShotExecutor.shutdown();
      }
      // just shutdown and don't worry about adding a error shot to executor, this is a an
      // exceptional case that is not handled fully here, deliberately.
    }
  }
  
  @Override
  public void blockUntilFinish() {
    // it's good to check if its shutdown before attempting, saves some processing.
    if (!processShotExecutor.isShutdown()) {
      processShotExecutor.shutdown();
    }
    try {
      long startTime = System.nanoTime();
      processShotExecutor.awaitTermination(shotProps.getMaxShotFinishSec(), TimeUnit.SECONDS);
      if (!processShotExecutor.isTerminated()) {
        List<Runnable> tasks = processShotExecutor.shutdownNow();
        LOG.error("Timeout while waiting for shot executor terminate, waited for (sec): {}" +
            ", total tasks remaining: {}", shotProps.getMaxShotFinishSec(), tasks.size());
        // we'll not save any error here as we've make sure this doesn't happen by giving a very
        // long timeout limit in hours. If this still happens for some rare tests, we'll increase
        // it further, but no need to add this as a normal error condition. This is done to limit
        // the number of places that may have problems during shot capture leading to less brittle
        // code.
        return; // return so that no other code like saving EOS run, finally still runs.
      }
      LOG.debug("Total time waited for shots after blocking in millis: {}"
          , TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
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
    processShot(new ByteArrayInputStream(new byte[1]), metadata).run();
  }
  
  private Runnable processShot(InputStream stream, ShotMetadata metadata) {
    return () -> {
      LOG.debug("storing shot {} to cloud", metadata.getShotName());
      if (!shotCloudStore.storeShot(metadata.getShotName(), stream)) {
        // Shot failed to store, this indicates we've some problem and have to stop everything.
        // If this invocation is for a normal shot (other than EOS/ERROR), create new metadata for
        // an ERROR and save in db otherwise we'll save what is provided (If EOS can't be saved, we
        // will save same metadata, so that we know an EOS was reached which means success)
        ShotMetadata metadataOnError = metadata;
        String shotIdentifier = getShotIdentifier(metadata.getShotName());
        if (!(shotIdentifier.equals(shotProps.getErrorShot())
            || shotIdentifier.equals(shotProps.getEosShot()))) {
          metadataOnError = getShotMetadata(shotProps.getErrorShot(), DateTimeUtil.getCurrentUTC());
        }
        shotMetadataProvider.saveAsync(metadataOnError);
        errorProcessed = true;  // error handled, mark it processed.
        // close db store now, so that it can flush any pending records and close down.
        shotMetadataProvider.processRemainingAndTearDown();
        
        // even if executor is terminated, this should be ok, we'll get 0 remaining task.
        List<Runnable> tasks = processShotExecutor.shutdownNow();
        int taskRemaining = tasks.size();
        LOG.error("shot process couldn't save shot: {} to cloud and shutting down, total shots" +
            " remaining: {}", metadata.getShotName(), taskRemaining);
        return;
      }
      LOG.debug("stored shot {} to cloud, going to push into db", metadata.getShotName());
      // I've tested that when this submission leads to bulk trigger, executor's thread returns
      // and next task is run, the bulk execution is handled by another thread at db store.
      shotMetadataProvider.saveAsync(metadata);
    };
  }
  
  private ShotMetadata getShotMetadata(String shotIdentifier, OffsetDateTime dateTime) {
    return new ShotMetadata()
        .setShotName(getShotName(shotIdentifier))
        .setBuildId(build.getBuildId())
        .setTestVersionId(currentTestVersion.getTestVersionId())
        .setBuildKey(build.getBuildKey())
        .setSessionKey(sessionKey)
        .setAtLineZwl(currentTestVersion.getControlAtLineInProgram())
        .setCreateDate(dateTime);
  }
  
  String getShotName(String shotIdentifier) {
    return sessionKey + "-" + build.getBuildKey() + "-" + shotIdentifier + "." + shotProps.getExt();
  }
  
  String getShotIdentifier(String shotName) {
    // sessionId may have '-' too thus its safe to get last index of it which will be just behind
    // identifier.
    return shotName.substring(shotName.lastIndexOf("-") + 1, shotName.lastIndexOf("."));
  }
  
  class ShotCaptureThread implements Runnable {
    
    @Override
    public void run() {
      while (!stop && !Thread.interrupted()) {
        capture();
      }
    }
  }
  
  public static class Factory implements CaptureShotHandler.Factory {
  
    @Override
    public CaptureShotHandler create(APICoreProperties apiCoreProperties,
                                     ShotMetadataProvider shotMetadataProvider, Storage storage,
                                     Build build, String sessionKey, String bucketSessionStorage,
                                     CurrentTestVersion currentTestVersion) {
      return new CaptureShotHandlerImpl(apiCoreProperties, shotMetadataProvider, storage, build,
          sessionKey, bucketSessionStorage, currentTestVersion);
    }
  }
}
