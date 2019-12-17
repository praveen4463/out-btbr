package com.zylitics.btbr;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class Launcher {
  
  public static void main(String[] args) {
    SpringApplication.run(Launcher.class, args);
  }
  
  @Bean
  @Profile({"production", "e2e"})
  Storage storage() {
    return StorageOptions.getDefaultInstance().getService();
  }
  
  @Bean
  @Profile({"production", "e2e"})
  RestHighLevelClient restHighLevelClient() {
  
  }
}
