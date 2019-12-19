package com.zylitics.btbr;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zylitics.btbr.config.APICoreProperties;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.io.IOException;

// TODO: I am not too sure what DataAccessExceptions should be re-tried, let's first watch logs and
// decide if retry can help recovering from them. Hikari automatically retires until connection
// timeout so probably we could retry on lock failure, deadlock etc. Any code that invokes methods
// on NamedParameterJdbcTemplate or JdbcTemplate can throw subclasses of this exception.
// Perhaps the best way to do it would be to extend NamedParameterJdbcTemplate and the methods we're
// using. Detect errors there, reattempt if necessary and throw if failed.
// https://docs.spring.io/spring/docs/current/spring-framework-reference/data-access.html#dao-exceptions
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
  RestHighLevelClient restHighLevelClient(APICoreProperties apiCoreProperties,
                                          SecretsManager secretsManager) {
    APICoreProperties.Esdb esdb = apiCoreProperties.getEsdb();
    
    String esDBHostFromEnv = System.getenv(esdb.getEnvVarHost());
    Assert.hasLength(esDBHostFromEnv, esdb.getEnvVarHost() + " env. variable is not set.");
    
    String secret = secretsManager.getSecretAsPlainText(esdb.getAuthUserSecretCloudFile());
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY,
        new UsernamePasswordCredentials(esdb.getAuthUser(), secret));
  
    // TODO: see if we need to disable preemptive auth so that credentials are not sent with every
    // request https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/_basic_authentication.html
    
    return new RestHighLevelClient(RestClient.builder(HttpHost.create(esDBHostFromEnv))
        .setHttpClientConfigCallback(httpClientBuilder ->
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)));
  }
  
  // https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/cloud-sql/postgres/servlet/src/main/java/com/example/cloudsql/ConnectionPoolContextListener.java
  // https://github.com/brettwooldridge/HikariCP
  // https://github.com/pgjdbc/pgjdbc#connection-properties
  // Using com.google.cloud.sql.postgres.SocketFactory to connect pgdb on unix socket rather than
  // TCP.
  // Boot won't autoconfigure DataSource if a bean is already declared, so we're good on that front.
  @Bean
  @Profile({"production", "e2e"})
  DataSource hikariDataSource(APICoreProperties apiCoreProperties, SecretsManager secretsManager) {
    APICoreProperties.DataSource ds = apiCoreProperties.getDataSource();
    String connectionName = secretsManager.getSecretAsPlainText(ds.getConnNameCloudFile());
    String userPwd = secretsManager.getSecretAsPlainText(ds.getUserSecretCloudFile());
  
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(String.format("jdbc:postgresql:///%s", ds.getDbName()));
    config.setUsername(ds.getUserName());
    config.setPassword(userPwd);
    config.addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory");
    config.addDataSourceProperty("cloudSqlInstance", connectionName);
    config.setMinimumIdle(ds.getMinIdleConnPool());
    // TODO (optional): This note is to remember that we can customize pgjdbc driver by sending
    // various options via query string or addDataSourceProperty. see here:
    // https://github.com/pgjdbc/pgjdbc#connection-properties
    return new HikariDataSource(config);
  }
  
  // https://docs.spring.io/spring/docs/current/spring-framework-reference/data-access.html#jdbc-NamedParameterJdbcTemplate
  // when instantiated, it created a JDBCTemplate and wraps it to use it for processing queries, we
  // can get the wrapped JDBCTemplate using getJdbcOperations()
  @Bean
  @Profile({"production", "e2e"})
  NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
    return new NamedParameterJdbcTemplate(dataSource);
  }
  
  // https://docs.spring.io/spring/docs/current/spring-framework-reference/data-access.html#tx-prog-template-settings
  @Bean
  @Profile({"production", "e2e"})
  TransactionTemplate transactionTemplate(PlatformTransactionManager platformTransactionManager) {
    TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
    // TODO (optional): specify any transaction settings.
    transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
    return transactionTemplate;
  }
  
  /** published when all beans are loaded */
  @SuppressWarnings("unused")
  @EventListener(ContextRefreshedEvent.class)
  void onContextRefreshedEvent(SecretsManager secretsManager) throws IOException {
    // we should close SecretsManager once all beans that required it are loaded.
    secretsManager.close();
    // TODO: remove this once confirmed that it's working
    System.err.println("onContextRefreshedEvent published");
  }
  
  /** published when ApplicationContext is stopped */
  @SuppressWarnings("unused")
  @EventListener(ContextClosedEvent.class)
  void onContextClosedEvent(RestHighLevelClient restHighLevelClient) throws IOException {
    // RestHighLevelClient lives until the application runs.
    restHighLevelClient.close();
    // TODO: remove this once confirmed that it's working
    System.err.println("onContextClosedEvent published");
  }
}
