package com.zylitics.btbr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.concurrent.ThreadSafe;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * All setters in this class allow only first time access by container, after that no values can
 * be mutated.
 * @author Praveen Tiwari
 *
 */
@ThreadSafe
@Component
@ConfigurationProperties(prefix="api-core")
@Validated
@SuppressWarnings("unused")
public class APICoreProperties {
  
  @NotBlank
  private String projectId;
  
  public String getProjectId() {
    return projectId;
  }
  
  public void setProjectId(String projectId) {
    if (this.projectId == null) {
      this.projectId = projectId;
    }
  }
  
  @Valid
  private CloudKms cloudKms = new CloudKms();
  
  public CloudKms getCloudKms() { return cloudKms; }
  
  @Valid
  private Esdb esdb = new Esdb();
  
  public Esdb getEsdb() { return esdb; }
  
  public static class CloudKms {
  
    @NotBlank
    private String keyRing;
  
    @NotBlank
    private String key;
  
    @NotBlank
    private String keyBucket;
  
    public String getKeyRing() {
      return keyRing;
    }
  
    public void setKeyRing(String keyRing) {
      if (this.keyRing == null) {
        this.keyRing = keyRing;
      }
    }
  
    public String getKey() {
      return key;
    }
  
    public void setKey(String key) {
      if (this.key == null) {
        this.key = key;
      }
    }
  
    public String getKeyBucket() {
      return keyBucket;
    }
  
    public void setKeyBucket(String keyBucket) {
      if (this.keyBucket == null) {
        this.keyBucket = keyBucket;
      }
    }
  }
  
  public static class Esdb {
    
    @NotBlank
    private String authUser;
    
    @NotBlank
    private String authUserSecretCloudFile;
    
    @Min(1)
    private Integer maxRetries;
    
    @NotBlank
    private String shotMetadataIndex;
    
    @NotBlank
    private String btBuildResultIndex;
  
    public String getAuthUser() {
      return authUser;
    }
  
    public void setAuthUser(String authUser) {
      if (this.authUser == null) {
        this.authUser = authUser;
      }
    }
  
    public String getAuthUserSecretCloudFile() {
      return authUserSecretCloudFile;
    }
  
    public void setAuthUserSecretCloudFile(String authUserSecretCloudFile) {
      if (this.authUserSecretCloudFile == null) {
        this.authUserSecretCloudFile = authUserSecretCloudFile;
      }
    }
  
    public int getMaxRetries() {
      return maxRetries;
    }
  
    public void setMaxRetries(int maxRetries) {
      if (this.maxRetries == null) {
        this.maxRetries = maxRetries;
      }
    }
  
    public String getShotMetadataIndex() {
      return shotMetadataIndex;
    }
  
    public void setShotMetadataIndex(String shotMetadataIndex) {
      if (this.shotMetadataIndex == null) {
        this.shotMetadataIndex = shotMetadataIndex;
      }
    }
  
    public String getBtBuildResultIndex() {
      return btBuildResultIndex;
    }
  
    public void setBtBuildResultIndex(String btBuildResultIndex) {
      if (this.btBuildResultIndex == null) {
        this.btBuildResultIndex = btBuildResultIndex;
      }
    }
  }
}
