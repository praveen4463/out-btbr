package com.zylitics.btbr.runner;

import com.google.cloud.storage.Storage;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.http.RequestBuildRun;
import com.zylitics.btbr.http.ResponseBuildRun;
import com.zylitics.btbr.http.ResponseStatus;
import com.zylitics.zwl.webdriver.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Types;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Requirements:
 * 1. postgres instance (version same as production) must be running on localhost:5432 with updated
 * {@link APICoreProperties.DataSource#getDbName()}
 * 2. esdb instance (version same as production) must be running on localhost:9200 with updated
 * mappings.
 * 3. A new buildId every time must be supplied using system property
 * {@link InContainerE2ETest#BUILD_ID_SYS_PROP}
 */
@SuppressWarnings("unused")
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
public class InContainerE2ETest {
  
  private static final Logger LOG = LoggerFactory.getLogger(InContainerE2ETest.class);
  
  private static final String API_BASE_PATH = "/{version}/builds";
  
  private static final String APP_VER_KEY = "app-short-version";
  
  private static final String BUILD_ID_SYS_PROP = "zl.btbr.e2e.buildId";
  
  @Autowired
  private WebTestClient client;
  
  @Autowired
  private Environment env;
  
  @Autowired
  private APICoreProperties apiCoreProps;
  
  @Autowired
  private Storage storage;
  
  @Autowired
  private NamedParameterJdbcTemplate jdbc;
  
  private String apiVersion;
  private String buildId;
  
  @BeforeEach
  void setup() {
    apiVersion = env.getProperty(APP_VER_KEY);
    buildId = System.getProperty(BUILD_ID_SYS_PROP);
    Preconditions.checkArgument(!Strings.isNullOrEmpty(buildId), "buildId should be supplied as" +
        " system property");
    Path buildDir = Paths.get(Configuration.SYS_DEF_TEMP_DIR, "build-" + buildId);
    if (Files.isDirectory(buildDir)) {
      throw new RuntimeException("buildId " + buildId + " has already executed once, use a new" +
          " buildId after creating it in db");
    }
  
    client = client.mutate().responseTimeout(Duration.ofSeconds(60)).build();
  }
  
  // note: update the sessionKey once it's received.
  @Test
  void normalBuildRunTest() throws Exception {
    int timeoutSec = 120;
    int waitForSec = 2;
    String fakeVMDeleteUrl = "http://10.12.1.2/beta/zones/us-central1/grids/fake-grid";
    
    LOG.info("Submitting buildId {} to runner for execution..", buildId);
  
    RequestBuildRun request = new RequestBuildRun();
    request.setBuildId(Integer.valueOf(buildId));
    request.setVmDeleteUrl(fakeVMDeleteUrl);
  
    ResponseBuildRun response = client.post()
        .uri(uriBuilder -> uriBuilder.path(API_BASE_PATH).build(apiVersion))
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().is2xxSuccessful()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ResponseBuildRun.class)
        .returnResult().getResponseBody();
  
    assertNotNull(response);
    assertEquals(ResponseStatus.RUNNING.name(), response.getStatus());
    assertFalse(Strings.isNullOrEmpty(response.getSessionId()));
    LOG.info("Build {} started running session: {}", buildId, response.getSessionId());
    
    // wait until vm delete date is updated to keep the vm running for this test
    String sql = "SELECT bt_build_vm_id FROM bt_build WHERE bt_build_id = :bt_build_id;";
    SqlParameterSource namedParams = new MapSqlParameterSource("bt_build_id",
        new SqlParameterValue(Types.INTEGER, buildId));
    Integer buildVMId = jdbc.queryForObject(sql, namedParams, (rs, rowNum) ->
        rs.getInt("bt_build_vm_id"));
    if (buildVMId == null) {
      throw new RuntimeException("can't proceed, buildVMId is null");
    }
    
    sql = "SELECT delete_date FROM bt_build_vm WHERE bt_build_vm_id = :bt_build_vm_id";
    namedParams = new MapSqlParameterSource("bt_build_vm_id",
        new SqlParameterValue(Types.INTEGER, buildVMId));
    Instant startTime = Instant.now();
    boolean vmDeleteDateUpdated = false;
    while (!vmDeleteDateUpdated || Instant.now().minusSeconds(timeoutSec).isAfter(startTime)) {
      Date deleteDate = jdbc.queryForObject(sql, namedParams, (rs, rowNum) ->
          rs.getDate("delete_date"));
      if (deleteDate != null) {
        vmDeleteDateUpdated = true;
      }
      //noinspection BusyWait
      Thread.sleep(TimeUnit.MILLISECONDS.convert(waitForSec, TimeUnit.SECONDS));
    }
    // This is an invalid warning, log an issue once you can.
    //noinspection ConstantConditions
    if (!vmDeleteDateUpdated) {
      throw new RuntimeException("Waited for " + waitForSec + " seconds but vm's deleted date" +
          " didn't update, timeout out.");
    }
    // after delete date is updated, wait for few more seconds before finishing test to give time
    // to vm delete url invocation.
    Thread.sleep(TimeUnit.MILLISECONDS.convert(5, TimeUnit.SECONDS));
  }
}
