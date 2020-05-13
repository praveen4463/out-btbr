package com.zylitics.btbr.shot;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.zylitics.btbr.config.APICoreProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness= Strictness.STRICT_STUBS)
class GCPShotCloudStoreTest {
  
  @Test
  @DisplayName("Verify reattempts happen when shots fail to upload")
  void verifyReattemptsOnFailure() {
    APICoreProperties.Shot shotProps = new APICoreProperties.Shot();
    shotProps.setContentType("image/png");
    String bucket = "some-bucket";
    String shotName = "some-shot";
    String storageException = "reaching api limit for today";
    int errorCode = 504; // one of retryable error code.
    Storage storage = mock(Storage.class);
    WriteChannel channel = mock(WriteChannel.class);
    GCPShotCloudStore gcp = new GCPShotCloudStore("some-bucket", shotProps, storage);
    InputStream stream = new ByteArrayInputStream("some-text".getBytes());
    
    BlobInfo blobInfo = BlobInfo.newBuilder(bucket, shotName)
        .setContentType(shotProps.getContentType()).build();
    when(storage.writer(blobInfo)).thenThrow(new StorageException(errorCode, storageException))
        .thenReturn(channel);
    
    assertTrue(gcp.storeShot(shotName, stream));
  }
}
