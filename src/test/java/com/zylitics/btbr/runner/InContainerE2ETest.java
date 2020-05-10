package com.zylitics.btbr.runner;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.esdb.ShotMetadataIndexFields;
import com.zylitics.btbr.http.RequestBuildRun;
import com.zylitics.btbr.http.ResponseBuildRun;
import com.zylitics.btbr.http.ResponseStatus;
import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.model.ShotMetadata;
import com.zylitics.btbr.model.TestVersion;
import com.zylitics.btbr.runner.provider.BuildProvider;
import com.zylitics.btbr.runner.provider.TestVersionProvider;
import com.zylitics.btbr.shot.ShotNameProvider;
import com.zylitics.zwl.webdriver.Configuration;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
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
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Types;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Requirements:
 * 1. postgres instance (version same as production) must be running on localhost:5432 with updated
 * {@link APICoreProperties.DataSource#getDbName()}
 * 2. esdb instance (version same as production) must be running on localhost:9200 with updated
 * mappings.
 * 3. A new buildId must be supplied each time using system property
 * {@link InContainerE2ETest#BUILD_ID_SYS_PROP}, refer to db scripts for creating
 * 4. GOOGLE_APPLICATION_CREDENTIALS env variable should be present pointing to service account file
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
  
  @Autowired
  private RestHighLevelClient restHighLevelClient;
  
  @Autowired
  private BuildProvider buildProvider;
  
  @Autowired
  private TestVersionProvider testVersionProvider;
  
  private String apiVersion;
  private int buildId;
  
  @BeforeEach
  void setup() {
    apiVersion = env.getProperty(APP_VER_KEY);
    buildId = Integer.getInteger(BUILD_ID_SYS_PROP, 0);
    Preconditions.checkArgument(buildId == 0, "buildId should be supplied as" +
        " system property");
  
    client = client.mutate().responseTimeout(Duration.ofSeconds(60)).build();
  }
  // more tests: functionality of stop, sequence of shots, failure test, debug mode of build.
  
  // note: update the sessionKey once it's received.
  @Test
  void straightBuildRunTest() throws Exception {
    int timeoutSec = 120;
    int sleepBetweenPollSec = 2;
    int maxExpectedShots = 1000;
    int maxExpectedProgramOutput = 1000;
    String fakeVMDeleteUrl = "http://10.12.1.2/beta/zones/us-central1/grids/fake-grid";
    
    LOG.debug("Submitting buildId {} to runner for execution..", buildId);
  
    RequestBuildRun request = new RequestBuildRun();
    request.setBuildId(buildId);
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
    String sessionId = response.getSessionId();
    assertFalse(Strings.isNullOrEmpty(sessionId));
    
    LOG.debug("Build {} started running session: {}", buildId, sessionId);
    
    // not updating sessionId in bt_build_wd_session as it's a test
  
    waitUntilBuildCompletes(timeoutSec, sleepBetweenPollSec);
    
    // Test is completed, check that everything worked by looking into db, es, cloud etc.
    
    // 1. check build succeeded
    String sql = "SELECT start_date, end_date, is_success, error FROM bt_build WHERE" +
        " bt_build_id = :bt_build_id";
    SqlParameterSource namedParams = new MapSqlParameterSource("bt_build_id",
        new SqlParameterValue(Types.INTEGER, buildId));
    SqlRowSet rowSet = jdbc.queryForRowSet(sql, namedParams);
    Date startDate = rowSet.getDate("start_date");
    Date endDate = rowSet.getDate("end_date");
    boolean isSuccess = rowSet.getBoolean("is_success");
    String error = rowSet.getString("error");
    assertNotNull(endDate);
    assertTrue(endDate.after(startDate));
    assertTrue(isSuccess);
    assertNull(error);
    
    // 2. check all versions succeeded in build status
    sql = "SELECT status, zwl_executing_line, start_date, end_date, error FROM bt_build_status" +
        " WHERE bt_build_id = :bt_build_id AND bt_test_version_id = :bt_test_version_id";
    Map<String, SqlParameterValue> params = new HashMap<>();
    params.put("bt_build_id", new SqlParameterValue(Types.INTEGER, buildId));
    List<TestVersion> testVersions = testVersionProvider.getTestVersions(buildId)
        .orElseThrow(RuntimeException::new);
    for (TestVersion testVersion : testVersions) {
      params.put("bt_test_version_id", new SqlParameterValue(Types.INTEGER,
          testVersion.getTestVersionId()));
      rowSet = jdbc.queryForRowSet(sql, params);
      TestStatus status = (TestStatus) rowSet.getObject("status");
      int zwlExecutingLine = rowSet.getInt("zwl_executing_line");
      startDate = rowSet.getDate("start_date");
      endDate = rowSet.getDate("end_date");
      error = rowSet.getString("error");
      assertEquals(TestStatus.SUCCESS, status);
      assertTrue(zwlExecutingLine > 0);
      assertNotNull(endDate);
      assertTrue(endDate.after(startDate));
      assertNull(error);
    }
    
    // 3. check we pushed some shots to cloud and esdb, note that we can't easily verify from here
    // how many shots were actually taken by the code, thus just checking what's in db and matching
    // it with cloud. Will verify that in unit test.
    Build build = buildProvider.getBuild(buildId).orElseThrow(RuntimeException::new);
    BuildCapability buildCapability = build.getBuildCapability();
  }
  
  private void verifyShotsProcessed(String sessionId,
                                    Build build,
                                    BuildCapability buildCapability,
                                    List<TestVersion> testVersions,
                                    int maxExpectedShots) throws Exception {
    // get all the shots pushed to cloud with this session as prefix, assuming shots start with it.
    Page<Blob> blobs = storage.list(buildCapability.getShotBucketSessionStorage(),
        Storage.BlobListOption.prefix(sessionId));
    Iterator<Blob> blobIterator = blobs.iterateAll().iterator();
    List<String> shotsFromCloud = new ArrayList<>();
    int totalShotsCloud = 0;
    while (blobIterator.hasNext()) {
      Blob blob = blobIterator.next();
      shotsFromCloud.add(blob.getName());
      totalShotsCloud += 1;
    }
    assertTrue(totalShotsCloud > 1); // verify we get atleast a shot (and an EOS)
    
    // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.4/java-rest-high-search.html
    // Taking db records as base to compare with cloud shots, db is local thus more reliable.
    Thread.sleep(1000); // wait for documents to index, typically es takes a second
    SearchRequest searchRequest = new SearchRequest(apiCoreProps.getEsdb().getShotMetadataIndex());
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
  
    List<String> shotsFromEsdb = new ArrayList<>();
    int totalShotsEsdb = 0;
    // get total number of shots per version
    for (TestVersion testVersion : testVersions) {
      int lastAtLineZwlForVersion = 0;
      sourceBuilder
          .query(QueryBuilders.termQuery(ShotMetadataIndexFields.BUILD_ID, build.getBuildId()))
          .query(QueryBuilders.termQuery(ShotMetadataIndexFields.TEST_VERSION_ID,
              testVersion.getTestVersionId()))
          .sort(ShotMetadataIndexFields.CREATE_DATE, SortOrder.ASC)
          .size(maxExpectedShots);
      searchRequest.source(sourceBuilder);
      SearchHits hits = getSearchHits(searchRequest);
      int totalShotVersion = hits.getHits().length;
      assertTrue(totalShotVersion > 0); // this version has some shots
      totalShotsEsdb += totalShotVersion;
      
      // iterate thru all the hits
      for (SearchHit hit : hits.getHits()) {
        Map<String, Object> source = hit.getSourceAsMap();
        String shotName = (String) source.get(ShotMetadataIndexFields.SHOT_NAME);
        shotsFromEsdb.add(shotName);
        // assert that line number is increasing
        int atLineZwl = (int) source.get(ShotMetadataIndexFields.AT_LINE_ZWL);
        assertTrue(atLineZwl >= lastAtLineZwlForVersion);
        lastAtLineZwlForVersion = atLineZwl;
        // assert that other fields are valid
        assertEquals(build.getBuildKey(), source.get(ShotMetadataIndexFields.BUILD_KEY));
        assertEquals(sessionId, source.get(ShotMetadataIndexFields.SESSION_KEY));
      }
      assertTrue(lastAtLineZwlForVersion > 0);
    }
    // assert that esdb and cloud have equal no of shots
    assertEquals(totalShotsEsdb, totalShotsCloud);
    // see whether cloud and esdb shots are same
    assertTrue(shotsFromEsdb.containsAll(shotsFromCloud));
    
    // finally verify that if we derive shots using data that make up shots upto what is store in
    // cloud and esdb (they have already matched), both match up.
    ShotNameProvider shotNameProvider = new ShotNameProvider(sessionId, build.getBuildKey(),
        apiCoreProps.getShot().getExt());
    List<String> derivedShots = new ArrayList<>();
    for (int i = 1; i < totalShotsCloud; i++) {
      derivedShots.add(shotNameProvider.getName(String.valueOf(i)));
    }
    derivedShots.add(shotNameProvider.getName(apiCoreProps.getShot().getEosShot()));
    // assert that what we derive and expected is present in the cloud (or esdb)
    assertTrue(derivedShots.containsAll(shotsFromCloud));
  }
  
  private SearchHits getSearchHits(SearchRequest searchRequest) throws IOException {
    SearchResponse searchResponse = restHighLevelClient.search(searchRequest,
        RequestOptions.DEFAULT);
    return searchResponse.getHits();
  }
  
  private int getBuildVMId() {
    String sql = "SELECT bt_build_vm_id FROM bt_build WHERE bt_build_id = :bt_build_id;";
    SqlParameterSource namedParams = new MapSqlParameterSource("bt_build_id",
        new SqlParameterValue(Types.INTEGER, buildId));
    Integer buildVMId = jdbc.queryForObject(sql, namedParams, (rs, rowNum) ->
        rs.getInt("bt_build_vm_id"));
    if (buildVMId == null) {
      throw new RuntimeException("Couldn't get buildVMId for build " + buildId);
    }
    return buildVMId;
  }
  
  private Date getVMDeleteDate(int buildVMId) {
    String sql = "SELECT delete_date FROM bt_build_vm WHERE bt_build_vm_id = :bt_build_vm_id";
    SqlParameterSource namedParams = new MapSqlParameterSource("bt_build_vm_id",
        new SqlParameterValue(Types.INTEGER, buildVMId));
    return jdbc.queryForObject(sql, namedParams, (rs, rowNum) -> rs.getDate("delete_date"));
  }
  
  // waits until vm delete date is updated to keep the vm running for these tests, if we don't wait,
  // tests will finish after getting a response back, causing spring to shutdown midway. Obviously
  // this can't happen on production because the server once started doesn't stops on it's own.
  private void waitUntilBuildCompletes(int timeoutSec, int sleepBetweenPollSec) throws Exception {
    int buildVMId = getBuildVMId();
    Instant startTime = Instant.now();
    boolean vmDeleteDateUpdated = false;
    while (!vmDeleteDateUpdated || Instant.now().minusSeconds(timeoutSec).isAfter(startTime)) {
      Date deleteDate = getVMDeleteDate(buildVMId);
      if (deleteDate != null) {
        vmDeleteDateUpdated = true;
      }
      //noinspection BusyWait
      Thread.sleep(TimeUnit.MILLISECONDS.convert(sleepBetweenPollSec, TimeUnit.SECONDS));
    }
    // This is an invalid warning, log an issue in intellij once you can.
    //noinspection ConstantConditions
    if (!vmDeleteDateUpdated) {
      throw new TimeoutException("Waited for " + timeoutSec + " seconds but vm's deleted date" +
          " didn't update");
    }
    // after delete date is updated, wait for few more seconds before finishing test to give time
    // to vm delete url invocation.
    Thread.sleep(TimeUnit.MILLISECONDS.convert(5, TimeUnit.SECONDS));
  }
}
