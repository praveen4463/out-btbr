package com.zylitics.btbr.shot;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.ShotMetadata;
import com.zylitics.btbr.runner.CurrentTestVersion;
import com.zylitics.btbr.runner.provider.ShotMetadataProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness= Strictness.STRICT_STUBS)
class CaptureShotHandlerImplTest {
  
  private static final Logger LOG = LoggerFactory.getLogger(CaptureShotHandlerImplTest.class);
  
  private final Build build =  new Build().setBuildId(1).setBuildKey("bkey");
  private final String sessionKey = "skey";
  private final CurrentTestVersion currentTestVersion = new CurrentTestVersion()
      .setTestVersionId(1)
      .setControlAtLineInProgram(1);
  private final APICoreProperties.Shot shotProps = getShotProps();
  
  @Test
  @DisplayName("Verify normal capture and save shot flow")
  void verifyNormalFlow() throws Exception {
    List<String> shotsCloud = new ArrayList<>();
    List<ShotMetadata> shotsDb = new ArrayList<>();
    ShotNameProvider shotNameProvider = new ShotNameProvider(sessionKey, build.getBuildKey(),
        shotProps.getExt());
  
    ShotCloudStore shotCloudStore = mock(ShotCloudStore.class);
    ShotMetadataProvider shotMetadataProvider = mock(ShotMetadataProvider.class);
    
    CaptureShotHandlerImpl captureHandler = new CaptureShotHandlerImpl(shotProps,
        shotMetadataProvider,
        build,
        sessionKey,
        currentTestVersion,
        getCaptureDevice(),
        shotCloudStore);
    
    when(shotCloudStore.storeShot(anyString(), any(InputStream.class))).thenAnswer(i -> {
      shotsCloud.add(i.getArgument(0));
      return true;
    });
    
    doAnswer(i -> {
      ShotMetadata shotMetadata = i.getArgument(0);
      shotsDb.add(shotMetadata);
      return null;
    }).when(shotMetadataProvider).saveAsync(any(ShotMetadata.class));
    
    // start the shots
    captureHandler.startShot();
    // let some clicks
    Thread.sleep(50);
    // stop shots
    captureHandler.stopShot();
    captureHandler.blockUntilFinish();
    // assert
    assertTrue(shotsCloud.size() > 1);
    assertTrue(shotsDb.size() > 1);
  
    boolean endReached = false;
    for (int i = 0; i < shotsCloud.size(); i++) {
      String currentIdentifier = shotNameProvider.getIdentifier(shotsCloud.get(i));
      String oneAheadIdentifier = null;
      try {
        oneAheadIdentifier = shotNameProvider.getIdentifier(shotsCloud.get(i + 1));
        assertShotIncreasingBy1(currentIdentifier, oneAheadIdentifier);
      } catch (NumberFormatException nfe) {
        // we're one behind the last shot
        assertEquals(shotsCloud.size() - 2 , i);
        // it must denote an end
        assertEquals(shotProps.getEosShot(), oneAheadIdentifier);
        endReached = true;
        break;
      }
    }
    assertTrue(endReached);
  
    endReached = false;
    for (int i = 0; i < shotsDb.size(); i++) {
      ShotMetadata currentMetadata = shotsDb.get(i);
      assertEquals(build.getBuildId(), currentMetadata.getBuildId());
      assertEquals(currentTestVersion.getTestVersionId(), currentMetadata.getTestVersionId());
      assertEquals(build.getBuildKey(), currentMetadata.getBuildKey());
      assertEquals(sessionKey, currentMetadata.getSessionKey());
      
      String currentIdentifier = shotNameProvider.getIdentifier(currentMetadata.getShotName());
      ShotMetadata oneAheadMetadata;
      String oneAheadIdentifier = null;
      try {
        oneAheadMetadata = shotsDb.get(i + 1);
        // zwlLine didn't change
        assertEquals(currentMetadata.getAtLineZwl(), oneAheadMetadata.getAtLineZwl());
        // we can get only millisecond precision in time in java 8 thus not using isBefore here
        // since many shots in this test could be taken in same millisecond.
        // https://stackoverflow.com/a/49203836/1624454
        assertFalse(currentMetadata.getCreateDate().isAfter(oneAheadMetadata.getCreateDate()));
        
        oneAheadIdentifier = shotNameProvider.getIdentifier(oneAheadMetadata.getShotName());
        assertShotIncreasingBy1(currentIdentifier, oneAheadIdentifier);
      } catch (NumberFormatException nfe) {
        // we're one behind the last shot
        assertEquals(shotsDb.size() - 2 , i);
        // it must denote an end
        assertEquals(shotProps.getEosShot(), oneAheadIdentifier);
        endReached = true;
        // don't break and let IndexOut.. exception occur so that metadata of last shot that
        // doesn't require comparision with one-ahead is done
      } catch (IndexOutOfBoundsException ignore) {}
    }
    assertTrue(endReached);
  }
  
  @Test
  @DisplayName("Verify shot process halts if error in uploading to cloud")
  void verifyShotProcessHalts() throws Exception {
    List<String> shotsCloud = new ArrayList<>();
    List<ShotMetadata> shotsDb = new ArrayList<>();
    ShotNameProvider shotNameProvider = new ShotNameProvider(sessionKey, build.getBuildKey(),
        shotProps.getExt());
  
    ShotCloudStore shotCloudStore = mock(ShotCloudStore.class);
    ShotMetadataProvider shotMetadataProvider = mock(ShotMetadataProvider.class);
  
    CaptureShotHandlerImpl captureHandler = new CaptureShotHandlerImpl(shotProps,
        shotMetadataProvider,
        build,
        sessionKey,
        currentTestVersion,
        getCaptureDevice(),
        shotCloudStore);
  
    when(shotCloudStore.storeShot(anyString(), any(InputStream.class)))
        .thenAnswer(i -> {
          shotsCloud.add(i.getArgument(0));
          return true;
        })
        .thenReturn(false);
  
    doAnswer(i -> {
      ShotMetadata shotMetadata = i.getArgument(0);
      shotsDb.add(shotMetadata);
      return null;
    }).when(shotMetadataProvider).saveAsync(any(ShotMetadata.class));
  
    // start the shots
    captureHandler.startShot();
    // let everything shutdown
    Thread.sleep(100);
    // assert
    assertEquals(1, shotsCloud.size());
    assertEquals(2, shotsDb.size()); // added shot for error
    // both cloud and db got a valid first shot
    assertEquals(1, Integer.parseInt(shotNameProvider.getIdentifier(shotsCloud.get(0))));
    assertEquals(1, Integer.parseInt(shotNameProvider.getIdentifier(shotsDb.get(0).getShotName())));
    // after first shot, cloud store had some problem, thus we should save some error shot to db
    assertEquals(shotProps.getErrorShot(),
        shotNameProvider.getIdentifier(shotsDb.get(1).getShotName()));
  }
  
  // oneAheadIdentifier identifier when numeric is +1 to currentIdentifier, means identifiers
  // are in numerical increasing order and are increased by 1
  private void assertShotIncreasingBy1(String currentIdentifier, String oneAheadIdentifier) {
    assertEquals(Integer.parseInt(currentIdentifier), Integer.parseInt(oneAheadIdentifier) - 1);
  }
  
  private APICoreProperties.Shot getShotProps() {
    APICoreProperties.Shot shotProps = new APICoreProperties.Shot();
    shotProps.setExt("png");
    shotProps.setEosShot("eos");
    shotProps.setErrorShot("error");
    shotProps.setMaxShotFinishSec(10);
    return shotProps;
  }
  
  private CaptureDevice getCaptureDevice() throws Exception {
    CaptureDevice captureDevice = mock(CaptureDevice.class);
    InputStream shotStream = new ByteArrayInputStream(new byte[1]);
    when(captureDevice.captureFull()).thenReturn(() -> shotStream);
    return captureDevice;
  }
}
