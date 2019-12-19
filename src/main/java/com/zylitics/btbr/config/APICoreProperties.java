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
  private DataSource dataSource = new DataSource();
  
  public DataSource getDataSource() { return dataSource; }
  
  @Valid
  private CloudKms cloudKms = new CloudKms();
  
  public CloudKms getCloudKms() { return cloudKms; }
  
  @Valid
  private Esdb esdb = new Esdb();
  
  public Esdb getEsdb() { return esdb; }
  
  @Valid
  private Runner runner = new Runner();
  
  public Runner getRunner() { return runner; }
  
  public static class DataSource {
    
    @NotBlank
    private String dbName;
  
    @NotBlank
    private String userName;
  
    @NotBlank
    private String userSecretCloudFile;
  
    @NotBlank
    private String connNameCloudFile;
    
    @Min(1)
    private Short minIdleConnPool;
  
    public String getDbName() {
      return dbName;
    }
  
    public void setDbName(String dbName) {
      if (this.dbName == null) {
        this.dbName = dbName;
      }
    }
  
    public String getUserName() {
      return userName;
    }
  
    public void setUserName(String userName) {
      if (this.userName == null) {
        this.userName = userName;
      }
    }
  
    public String getUserSecretCloudFile() {
      return userSecretCloudFile;
    }
  
    public void setUserSecretCloudFile(String userSecretCloudFile) {
      if (this.userSecretCloudFile == null) {
        this.userSecretCloudFile = userSecretCloudFile;
      }
    }
  
    public String getConnNameCloudFile() {
      return connNameCloudFile;
    }
  
    public void setConnNameCloudFile(String connNameCloudFile) {
      if (this.connNameCloudFile == null) {
        this.connNameCloudFile = connNameCloudFile;
      }
    }
  
    public Short getMinIdleConnPool() {
      return minIdleConnPool;
    }
  
    public void setMinIdleConnPool(Short minIdleConnPool) {
      if (this.minIdleConnPool == null) {
        this.minIdleConnPool = minIdleConnPool;
      }
    }
  }
  
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
    private Short maxRetries;
    
    @NotBlank
    private String shotMetadataIndex;
    
    @NotBlank
    private String btBuildResultIndex;
    
    @NotBlank
    private String envVarHost;
  
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
  
    public Short getMaxRetries() {
      return maxRetries;
    }
  
    public void setMaxRetries(Short maxRetries) {
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
  
    public String getEnvVarHost() {
      return envVarHost;
    }
  
    public void setEnvVarHost(String envVarHost) {
      if (this.envVarHost == null) {
        this.envVarHost = envVarHost;
      }
    }
  }
  
  public static class Runner {
    
    @Min(10)
    private Integer maxTestCommandLoad;
  
    public Integer getMaxTestCommandLoad() {
      return maxTestCommandLoad;
    }
  
    public void setMaxTestCommandLoad(Integer maxTestCommandLoad) {
      if (this.maxTestCommandLoad == null) {
        this.maxTestCommandLoad = maxTestCommandLoad;
      }
    }
  }
}
