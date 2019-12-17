package com.zylitics.btbr.support;

import com.google.cloud.kms.v1.CryptoKeyName;
import com.google.cloud.kms.v1.DecryptResponse;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.protobuf.ByteString;
import com.zylitics.btbr.SecretsManager;
import com.zylitics.btbr.resource.APICoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;

@Component
public class CloudKMSSecretsManager implements SecretsManager {
  
  private final APICoreProperties apiCoreProperties;
  private final Storage storage;
  private final KeyManagementServiceClient client;
  
  @SuppressWarnings("unused")
  @Autowired
  CloudKMSSecretsManager(APICoreProperties apiCoreProperties, Storage storage)
      throws IOException {
    this.apiCoreProperties = apiCoreProperties;
    this.storage = storage;
    client = KeyManagementServiceClient.create();
  }
  
  CloudKMSSecretsManager(APICoreProperties apiCoreProperties, Storage storage,
                                KeyManagementServiceClient client) {
    this.apiCoreProperties = apiCoreProperties;
    this.storage = storage;
    this.client = client;
  }
  
  @Override
  public String getSecretAsPlainText(String secretCloudFileName) {
    Assert.hasLength(secretCloudFileName, "secret cloud file name can't be empty");
  
    BlobId blobId = BlobId.of(apiCoreProperties.getKeyBucket(), secretCloudFileName);
    // we'll throw if there is any error for now as storage and kms both have retry built-in and
    // i don't expect storage to throw any error that needs retry from user while 'getting' blob.
    byte[] content = storage.readAllBytes(blobId);
    String resourceName = CryptoKeyName.format(apiCoreProperties.getProjectId(), "global",
        apiCoreProperties.getKeyRing(), apiCoreProperties.getKey());
    DecryptResponse decrypt = client.decrypt(resourceName, ByteString.copyFrom(content));
    // trim is important to remove unintended whitespaces.
    return decrypt.getPlaintext().toStringUtf8().trim();
  }
  
  @Override
  public void close() {
    client.close();
  }
}
