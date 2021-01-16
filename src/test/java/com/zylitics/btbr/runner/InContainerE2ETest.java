package com.zylitics.btbr.runner;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.dao.SqlParamsBuilder;
import com.zylitics.btbr.esdb.ShotMetadataIndexFields;
import com.zylitics.btbr.http.RequestBuildRun;
import com.zylitics.btbr.http.ResponseBuildRun;
import com.zylitics.btbr.http.ResponseCommon;
import com.zylitics.btbr.http.ResponseStatus;
import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.model.BuildOutput;
import com.zylitics.btbr.model.TestVersion;
import com.zylitics.btbr.runner.provider.BuildProvider;
import com.zylitics.btbr.runner.provider.TestVersionProvider;
import com.zylitics.btbr.shot.ShotNameProvider;
import com.zylitics.btbr.util.DateTimeUtil;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.remote.BrowserType;
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
import java.sql.Types;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Requirements:
 * 1. postgres instance (version same as production) must be running on localhost:5432 with updated
 * {@link APICoreProperties.DataSource#getDbName()}
 * 2. esdb instance (version same as production) must be running on localhost:9200 with updated
 * mappings, clear all documents if you've dropped existing pg db and spin new to avoid any new pg
 * ids finding existing docs in esdb.
 * 3. A new buildId must be supplied for each test using unique system properties. Every test
 * requires it's buildId to be created specifically to let the test pass and be able to assert the
 * values it was made for, make sure this requirement is fulfilled. Refer to each test's description
 * for details of the requirements and then refer test db scripts for creating them.
 * 4. GOOGLE_APPLICATION_CREDENTIALS env variable should be present pointing to service account file
 * 5. Must be run sequentially, parallel execution is not supported in api
 * Notes:
 * 1. When all tests are run at once, just one application context is created and same instance of
 *    Runner is used for all request, this asserts that the api works when same vm is used for multiple
 *    tests, such as IDE tests.
 */
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
public class InContainerE2ETest {
  
  private static final Logger LOG = LoggerFactory.getLogger(InContainerE2ETest.class);
  
  private static final String ERROR_REGEX = "(?i).*(exception|error).*";
  
  private static final String STOP_ERROR_REGEX = "(?i).*(stop|stopped).*";
  
  private static final String API_BASE_PATH = "/{version}/builds";
  
  private static final String APP_VER_KEY = "app-short-version";
  
  private static final String STRAIGHT_TEST_BUILD_ID_SYS_PROP = "zl.btbr.e2e.straightTestBuildId";
  
  private static final String STOP_TEST_BUILD_ID_SYS_PROP = "zl.btbr.e2e.stopTestBuildId";
  
  private static final String ZWL_ERROR_TEST_BUILD_ID_SYS_PROP = "zl.btbr.e2e.zwlErrorTestBuildId";
  
  private static final String ZWL_ERROR_TEST_ABORT_BUILD_ID_SYS_PROP =
      "zl.btbr.e2e.zwlErrorTestAbortBuildId";
  
  // Front end api should get user's preference/current location to get the offset. This is
  // important otherwise postgres will return timestamp converted to it's own local timezone.
  private static final String DESIRED_OFFSET = "UTC";
  
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
  private String buildDirName;
  
  @BeforeEach
  void setup() {
    apiVersion = env.getProperty(APP_VER_KEY);
    client = client.mutate().responseTimeout(Duration.ofSeconds(60)).build();
  }
  
  /*
  BuildId requirements:
  - Requires a buildId that must have all logs set at Trace level and ON, it includes
  -   All browser/performance logs at build caps level
  -   All internal logs using system properties
  - It must have valid tests that have no bugs, preferably already tested. At least two tests
    are required
  - buildVm must be set on delete_from_runner=true so that we can track build finish
   */
  @Test
  void straightBuildRunTest() throws Exception {
    buildId = Integer.getInteger(STRAIGHT_TEST_BUILD_ID_SYS_PROP, 0);
    Preconditions.checkArgument(buildId > 0, "straightTestBuildId should be supplied as system" +
        " property");
    setBuildDirName();
    
    int timeoutSec = 120;
    int sleepBetweenPollSec = 2;
    int maxExpectedBuildOutput = 1000;
    boolean testsHaveAnyElementShot = false;
  
    Build build = buildProvider.getBuild(buildId).orElseThrow(RuntimeException::new);
    BuildCapability buildCapability = build.getBuildCapability();
    
    String sessionId = startBuild();
    waitUntilBuildCompletes(build.getBuildVMId(), timeoutSec, sleepBetweenPollSec);
    
    // Test is completed, check that everything worked by looking into db, es, cloud etc.
    LOG.debug("build {} is now completed, going to assert results", buildId);
    // 1. check build succeeded
    LOG.debug("asserting build success");
    com.zylitics.btbr.test.model.Build buildDetails = getBuildDetails();
    assertNotNull(buildDetails.getEndDate());
    assertTrue(buildDetails.getEndDate().isAfter(buildDetails.getStartDate()));
    assertTrue(buildDetails.isSuccess());
    assertNull(buildDetails.getError());
    LOG.debug("build success assert done");
    
    // get the total time elapsed during test version run, will help knowing a few things. Don't
    // take build time as it starts little before first version could start.
    int buildRunTimeSec = 0;
    
    // 2. check all versions succeeded in build status
    LOG.debug("asserting test versions success");
    List<TestVersion> testVersions = testVersionProvider.getTestVersions(buildId)
        .orElseThrow(RuntimeException::new);
    assertTrue(testVersions.size() >= 2);
    for (TestVersion testVersion : testVersions) {
      LOG.debug("asserting testVersion {}", testVersion.getTestVersionId());
      if (testVersion.getCode().contains("captureElementScreenshot")) {
        testsHaveAnyElementShot = true;
      }
      com.zylitics.btbr.test.model.BuildStatus bsDetails =
          getBuildStatusDetails(testVersion.getTestVersionId());
      assertNotNull(bsDetails.getStartDate());
      assertNotNull(bsDetails.getEndDate());
      assertTrue(bsDetails.getEndDate().isAfter(bsDetails.getStartDate()));
      buildRunTimeSec += bsDetails.getStartDate().until(bsDetails.getEndDate(), ChronoUnit.SECONDS);
      assertEquals(TestStatus.SUCCESS, bsDetails.getStatus());
      assertTrue(bsDetails.getZwlExecutingLine() >= 0);
      // could also be 0 when program completes too fast and no line push goes into db, will check
      // them properly in unit tests.
      assertNull(bsDetails.getError());
      LOG.debug("completed asserting testVersion {}", testVersion.getTestVersionId());
    }
    LOG.debug("Sleeping until esdb has index documents");
    Thread.sleep(1000); // wait for documents to index, typically esdb takes a second
    
    // 3. check we pushed some shots to cloud and esdb
    LOG.debug("asserting shots in cloud and esdb");
    int maxExpectedShots = buildRunTimeSec * 3; // every second 3 shots
    int minExpectedShots = buildRunTimeSec / 2; // 1 shot every 2 seconds
    LOG.debug("Expecting minimum {} and maximum {} shots", minExpectedShots, maxExpectedShots);
    assertShotsProcessed(sessionId, build, testVersions, maxExpectedShots, minExpectedShots);
    
    // 4. check some build output was saved
    LOG.debug("asserting build output");
    assertBuildOutput(build, testVersions, maxExpectedBuildOutput);
    
    // 5. check logs and element screenshots were uploaded to cloud
    LOG.debug("asserting logs in cloud");
    assertLogsUploaded(buildCapability, testsHaveAnyElementShot);
    
    // 6. check build allDoneDate is updated and is greater than endDate
    LOG.debug("asserting allDoneDate");
    LocalDateTime allDoneDate = getBuildAllDoneDate();
    assertNotNull(allDoneDate);
    assertTrue(allDoneDate.isAfter(buildDetails.getEndDate()));
    
    // 7. check minutesConsumed is updated
    LOG.debug("asserting minutesConsumed");
    int minutesConsumed = getMinutesConsumed(build.getUserId());
    assertTrue(minutesConsumed > 0);
    
    // 8. check build request completed
    LOG.debug("asserting build request completed");
    assertTrue(didBuildRequestComplete());
    
    // no need to check vm delete date updated cause it's already done in waitUntilBuildCompletes.
  }
  
  /**
   * Submits the build to runner which in turn starts a new session and assigns a new thread for
   * asynchronous execution. After receiving a session from runner, the sessionId is updated in db.
   * @return the new sessionId for this build
   */
  private String startBuild() {
    LOG.debug("Submitting buildId {} to runner for execution..", buildId);
    RequestBuildRun request = new RequestBuildRun();
    request.setBuildId(buildId);
    
    ResponseBuildRun response = client.post()
        .uri(uriBuilder -> uriBuilder.path(API_BASE_PATH).build(apiVersion))
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ResponseBuildRun.class)
        .returnResult().getResponseBody();
    
    assertNotNull(response);
    assertEquals(ResponseStatus.RUNNING.name(), response.getStatus());
    String sessionId = response.getSessionId();
    assertFalse(Strings.isNullOrEmpty(sessionId));
    
    LOG.debug("Build {} started running session: {}", buildId, sessionId);
    updateSession(sessionId);
    return sessionId;
  }
  
  private void assertShotsProcessed(String sessionId,
                                    Build build,
                                    List<TestVersion> testVersions,
                                    int maxExpectedShots,
                                    int minExpectedShots) throws Exception {
    ShotNameProvider shotNameProvider = new ShotNameProvider(sessionId, build.getBuildKey(),
        apiCoreProps.getShot().getExt());
    
    // get all the shots pushed to cloud with this session as prefix, assuming shots start with it.
    Page<Blob> blobs = storage.list(build.getShotBucketSessionStorage(),
        Storage.BlobListOption.prefix(sessionId));
    Iterator<Blob> blobIterator = blobs.iterateAll().iterator();
    List<String> shotsFromCloud = new ArrayList<>();
    while (blobIterator.hasNext()) {
      Blob blob = blobIterator.next();
      shotsFromCloud.add(blob.getName());
    }
    assertTrue(shotsFromCloud.size() >= minExpectedShots);
    LOG.debug("found total {} shots in cloud", shotsFromCloud.size());
  
    LOG.debug("starting shots search in esdb");
    // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.4/java-rest-high-search.html
    List<String> shotsFromEsdb = new ArrayList<>();
    int lastShotNumericIdentifier = 0;
    // search shots per version
    for (TestVersion testVersion : testVersions) {
      int lastAtLineZwlForVersion = 0; // reset line no. to 0 upon starting iteration for a version
      SearchRequest searchRequest =
          new SearchRequest(apiCoreProps.getEsdb().getShotMetadataIndex());
      SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
      sourceBuilder
          .query(QueryBuilders.boolQuery()
              .must(QueryBuilders.termQuery(ShotMetadataIndexFields.BUILD_ID, build.getBuildId()))
              .must(QueryBuilders.termQuery(ShotMetadataIndexFields.TEST_VERSION_ID,
                  testVersion.getTestVersionId())))
          .sort(ShotMetadataIndexFields.CREATE_DATE, SortOrder.ASC)
          .size(maxExpectedShots);
      searchRequest.source(sourceBuilder);
      SearchHits hits = getSearchHits(searchRequest);
      assertTrue(hits.getHits().length > 0); // this version has some shots
      LOG.debug("found total {} shots in esdb for testVersion {}", hits.getHits().length,
          testVersion.getTestVersionId());
      
      // iterate thru all the hits
      for (SearchHit hit : hits.getHits()) {
        Map<String, Object> source = hit.getSourceAsMap();
        String shotName = (String) source.get(ShotMetadataIndexFields.SHOT_NAME);
        shotsFromEsdb.add(shotName);
        // assert that shot identifiers when numeric are in increasing order of 1
        try {
          int shotIdentifier = Integer.parseInt(shotNameProvider.getIdentifier(shotName));
          assertEquals(shotIdentifier, lastShotNumericIdentifier + 1);
          lastShotNumericIdentifier = shotIdentifier;
        } catch (NumberFormatException ignore) {}
        // assert that line number is increasing by comparing it with last store line number
        int atLineZwl = (int) source.get(ShotMetadataIndexFields.AT_LINE_ZWL);
        assertTrue(atLineZwl >= lastAtLineZwlForVersion);
        // some shots may be taken at the same line thus ">="
        lastAtLineZwlForVersion = atLineZwl;
        // assert that other fields are valid
        assertEquals(build.getBuildKey(), source.get(ShotMetadataIndexFields.BUILD_KEY));
        assertEquals(sessionId, source.get(ShotMetadataIndexFields.SESSION_KEY));
      }
      // assert that line number didn't remain 0 and increased
      assertTrue(lastAtLineZwlForVersion > 0);
      LOG.debug("lastAtLineZwlForVersion for testVersion {} is {}", testVersion.getTestVersionId(),
          lastAtLineZwlForVersion);
    }
    // assert that the last recorded numeric identifier is equals to all shots - 1, assuming last
    // shot is always an end-of-shot string identifier.
    assertEquals(lastShotNumericIdentifier, shotsFromEsdb.size() - 1);
    // once iteration ends, shotsFromEsdb contains shots for all versions.
    // assert that esdb and cloud have equal no of shots
    assertEquals(shotsFromEsdb.size(), shotsFromCloud.size());
    // see whether cloud and esdb shots are same
    assertTrue(shotsFromEsdb.containsAll(shotsFromCloud));
    
    // create shot names manually using the shot name components we've and identifier from the
    // number of shots retrieved from cloud, assert that they match with shot names shotsFromEsdb.
    List<String> derivedShots = new ArrayList<>();
    for (int i = 1; i <= shotsFromCloud.size(); i++) {
      derivedShots.add(shotNameProvider.getName(String.valueOf(i)));
    }
    derivedShots.add(shotNameProvider.getName(apiCoreProps.getShot().getEosShot()));
    // assert that what we derive and expected is present in the cloud (or esdb)
    assertTrue(derivedShots.containsAll(shotsFromEsdb));
  }
  
  private void assertBuildOutput(Build build,
                                 List<TestVersion> testVersions,
                                 int maxExpectedBuildOutput) {
    String sql = "SELECT output, ended FROM bt_build_output\n" +
        "WHERE bt_build_id = :bt_build_id AND bt_test_version_id = :bt_test_version_id\n" +
        "ORDER BY bt_build_output_id LIMIT :limit";
    for (TestVersion testVersion : testVersions) {
      List<BuildOutput> buildOutputs = jdbc.query(sql, new SqlParamsBuilder()
      .withInteger("bt_build_id", build.getBuildId())
      .withInteger("bt_test_version_id", testVersion.getTestVersionId())
      .withInteger("limit", maxExpectedBuildOutput).build(), (rs, rowNum) ->
          new BuildOutput().setOutput(rs.getString("output")).setEnded(rs.getBoolean("ended")));
      int total = buildOutputs.size();
      assertTrue(total > 0); // this version has some output
      LOG.debug("total build output for testVersion {} is {}", testVersion.getTestVersionId(),
          total);
      // no output is empty
      assertFalse(buildOutputs.stream().anyMatch(b -> Strings.isNullOrEmpty(b.getOutput())));
      // the last output must contain ended = true for each version
      assertTrue(buildOutputs.get(total - 1).isEnded());
    }
  }
  
  private void assertLogsUploaded(BuildCapability buildCapability,
                                  boolean testsHaveAnyElementShot) {
    // all logs are placed with a dir structure described in LocalAssetsToCloudHandler, for cloud
    // there is no dir structure. The dir structure we give, is treated as name of blob, so we
    // can give dir structure as prefix for blob (upto immediate parent of log files) to search
    // all blobs under that dir or a specific file name for specific blob.
    Iterator<Blob> blobs;
    APICoreProperties.Webdriver wdProps = apiCoreProps.getWebdriver();
    if (testsHaveAnyElementShot) {
      LOG.debug("Asserting presence of element shots");
      // it's ok to check we uploaded some element shot, don't try asserting on the numbers.
      blobs = storage.list(wdProps.getElemShotsBucket(), Storage.BlobListOption
          .prefix(buildDirName)).iterateAll().iterator();
      assertTrue(blobs.hasNext()); // at least one blob is there
      while (blobs.hasNext()) {
        Blob elementShot = blobs.next();
        assertTrue(elementShot.getSize() >= 10); // a few bytes
        assertTrue(elementShot.getName().endsWith("png"));
        LOG.debug("Element shot {} found", elementShot.getName());
      }
    }
  
    String serverLogsBucket = wdProps.getServerLogsBucket();
    
    // lets assume our tests haven't silent the driver logs
    LOG.debug("Asserting presence of driver logs");
    blobs = storage.list(serverLogsBucket, Storage.BlobListOption.prefix(buildDirName + "/" +
        wdProps.getDriverLogsDir() + "/" + wdProps.getDriverLogsFile())).iterateAll().iterator();
    assertTrue(blobs.hasNext());
    Blob driverLog = blobs.next();
    assertTrue(driverLog.getSize() >= 10);
    
    assertTrue(Boolean.getBoolean(wdProps.getEnableProfilerLogsProp()));
    LOG.debug("Asserting presence of profiler logs");
    blobs = storage.list(serverLogsBucket, Storage.BlobListOption.prefix(buildDirName + "/" +
        wdProps.getInternalLogsDir() + "/" + wdProps.getProfilerLogsFile()))
        .iterateAll().iterator();
    assertTrue(blobs.hasNext());
    Blob profilerLog = blobs.next();
    assertTrue(profilerLog.getSize() >= 10);
  
    assertTrue(buildCapability.getWdBrowserName().equals(BrowserType.CHROME)
        && (buildCapability.isWdChromeEnableNetwork() || buildCapability.isWdChromeEnablePage()));
    LOG.debug("Asserting presence of performance logs");
    blobs = storage.list(serverLogsBucket, Storage.BlobListOption.prefix(buildDirName + "/" +
        wdProps.getBrowserPerfLogsDir() + "/" + wdProps.getBrowserPerfLogsFile()))
        .iterateAll().iterator();
    assertTrue(blobs.hasNext());
    Blob browserPerfLog = blobs.next();
    assertTrue(browserPerfLog.getSize() >= 10);
  }
  
  /*
  BuildId requirements:
  - Prefers a buildId that has all logs disabled, so that unnecessary cloud upload doesn't happen.
  - buildId must have at least two tests, all tests must be bug free.
   */
  @Test
  void stopRequestTest() throws Exception {
    buildId = Integer.getInteger(STOP_TEST_BUILD_ID_SYS_PROP, 0);
    Preconditions.checkArgument(buildId > 0, "stopTestBuildId should be supplied as system" +
        " property");
    setBuildDirName();
    
    int timeoutSec = 30;
    int sleepBetweenPollSec = 1;
    // submit a new build to be able to stop it
    startBuild();
    // build is running on server, send a stop request now
    LOG.debug("Sending a stop request for build {}", buildId);
    ResponseCommon response = client.delete()
        .uri(uriBuilder -> uriBuilder.path(API_BASE_PATH)
            .pathSegment("{buildId}")
            .build(apiVersion, buildId))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody(ResponseCommon.class)
        .returnResult().getResponseBody();
    assertNotNull(response);
    assertEquals(ResponseStatus.STOPPING.name(), response.getStatus());
    LOG.debug("Stop is in progress, waiting until it's done before validating");
    waitUntilBuildCompletes(
        buildProvider.getBuild(buildId).orElseThrow(RuntimeException::new).getBuildVMId(),
        timeoutSec, sleepBetweenPollSec);
    LOG.debug("Stop is now completed, going to assert results");
    // 1. check build failed
    assertBuildFailure(STOP_ERROR_REGEX);
    
    // 2. check all versions failed in build status
    LOG.debug("asserting test versions failure on stop");
    List<TestVersion> testVersions = testVersionProvider.getTestVersions(buildId)
        .orElseThrow(RuntimeException::new);
    assertTrue(testVersions.size() >= 2);
    for (int i = 0; i < testVersions.size(); i++) {
      TestVersion testVersion = testVersions.get(i);
      LOG.debug("asserting testVersion {}", testVersion.getTestVersionId());
      com.zylitics.btbr.test.model.BuildStatus bsDetails =
          getBuildStatusDetails(testVersion.getTestVersionId());
      // all version should have STOP status because we sent it immediately
      assertEquals(TestStatus.STOPPED, bsDetails.getStatus());
      if (i == 0) {
        // first version must have been running and stopped because the stop was sent immediately
        // , assert that it has start/end dates.
        LOG.debug("asserting first version got stopped while running");
        assertNotNull(bsDetails.getStartDate());
        assertNotNull(bsDetails.getEndDate());
        continue;
      }
      LOG.debug("asserting other versions have null values other than status");
      // we don't put any start/end/error to versions that couldn't run
      assertNull(bsDetails.getStartDate());
      assertNull(bsDetails.getEndDate());
    }
  }
  
  /*
  BuildId requirements:
  - abort_on_failure must be set to false in build caps
  - Requires a buildId that must have logs set at Trace level and ON, it includes
  -   All browser/performance logs at build caps level
  -   All internal logs using system properties
  - It must have at least one test that must fail with a ZwlLangException exception, and one test
    is bug free that follows the failing test so that it can be asserted that build is continued
    after a failure.
   */
  @Test
  void zwlErrorTest() throws Exception {
    buildId = Integer.getInteger(ZWL_ERROR_TEST_BUILD_ID_SYS_PROP, 0);
    Preconditions.checkArgument(buildId > 0, "zwlErrorTestBuildId should be supplied as system" +
        " property");
    setBuildDirName();
    
    Build build = buildProvider.getBuild(buildId).orElseThrow(RuntimeException::new);
    BuildCapability buildCapability = build.getBuildCapability();
  
    assertFalse(build.isAbortOnFailure());
    
    int timeoutSec = 120;
    int sleepBetweenPollSec = 2;
    boolean testsHaveAnyElementShot = false;
    
    // start a new build
    startBuild();
    waitUntilBuildCompletes(build.getBuildVMId(), timeoutSec, sleepBetweenPollSec);
    
    // 1. check build failed
    assertBuildFailure(ERROR_REGEX);
    
    // 2. check at least one versions passes after a failure to assert that we continued build
    // after a zwl exception.
    LOG.debug("asserting min one test versions passes after a zwl failure");
    List<TestVersion> testVersions = testVersionProvider.getTestVersions(buildId)
        .orElseThrow(RuntimeException::new);
    assertTrue(testVersions.size() >= 2);
    boolean versionFailed = false;
    boolean successPostFailure = false;
    for (TestVersion testVersion : testVersions) {
      int tvId = testVersion.getTestVersionId();
      LOG.debug("on testVersion {}", tvId);
      if (testVersion.getCode().contains("captureElementScreenshot")) {
        testsHaveAnyElementShot = true;
      }
      com.zylitics.btbr.test.model.BuildStatus bsDetails =
          getBuildStatusDetails(testVersion.getTestVersionId());
      assertNotNull(bsDetails.getStartDate());
      assertNotNull(bsDetails.getEndDate());
      assertTrue(bsDetails.getEndDate().isAfter(bsDetails.getStartDate()));
      if (bsDetails.getStatus() == TestStatus.ERROR) {
        LOG.debug("failed testVersion {} found", tvId);
        assertNotNull(bsDetails.getError());
        assertNotNull(bsDetails.getErrorFrom());
        assertNotNull(bsDetails.getErrorTo());
        versionFailed = true;
        continue;
      }
      if (versionFailed && bsDetails.getStatus() == TestStatus.SUCCESS) {
        LOG.debug("found a post failure passed testVersion {}", tvId);
        successPostFailure = true;
      }
    }
    assertTrue(versionFailed);
    assertTrue(successPostFailure);
    
    // 3. check logs and element screenshots were uploaded to cloud
    LOG.debug("asserting logs in cloud");
    assertLogsUploaded(buildCapability, testsHaveAnyElementShot);
  }
  
  /*
  BuildId requirements:
  - abort_on_failure must be set to true in build caps
  - It must have at least two tests, the first test must fail with a ZwlLangException exception,
    following tests can be any, as they are not going to be run.
   */
  @Test
  void zwlAbortOnErrorTest() throws Exception {
    buildId = Integer.getInteger(ZWL_ERROR_TEST_ABORT_BUILD_ID_SYS_PROP, 0);
    Preconditions.checkArgument(buildId > 0, "zwlErrorTestAbortBuildId should be supplied as" +
        " system property");
    setBuildDirName();
    
    Build build = buildProvider.getBuild(buildId).orElseThrow(RuntimeException::new);
    
    assertTrue(build.isAbortOnFailure());
    
    int timeoutSec = 30;
    int sleepBetweenPollSec = 1;
    
    // start a new build
    startBuild();
    waitUntilBuildCompletes(build.getBuildVMId(), timeoutSec, sleepBetweenPollSec);
    
    // 1. check build failed
    assertBuildFailure(ERROR_REGEX);
    
    // 2. check first version failed and others Aborted
    LOG.debug("asserting all versions Aborted after a zwl failure");
    List<TestVersion> testVersions = testVersionProvider.getTestVersions(buildId)
        .orElseThrow(RuntimeException::new);
    assertTrue(testVersions.size() >= 2);
    for (int i = 0; i < testVersions.size(); i++) {
      TestVersion testVersion = testVersions.get(i);
      LOG.debug("asserting testVersion {}", testVersion.getTestVersionId());
      com.zylitics.btbr.test.model.BuildStatus bsDetails =
          getBuildStatusDetails(testVersion.getTestVersionId());
      if (i == 0) {
        LOG.debug("asserting first version failed");
        assertEquals(TestStatus.ERROR, bsDetails.getStatus());
        assertNotNull(bsDetails.getStartDate());
        assertNotNull(bsDetails.getEndDate());
        assertNotNull(bsDetails.getError());
        assertTrue(bsDetails.getError().matches(ERROR_REGEX));
        continue;
      }
      LOG.debug("asserting other versions aborted");
      // we don't put any start/end/error to versions that couldn't run
      assertEquals(TestStatus.ABORTED, bsDetails.getStatus());
      assertNull(bsDetails.getStartDate());
      assertNull(bsDetails.getEndDate());
      assertNull(bsDetails.getError());
    }
  }
  
  private void assertBuildFailure(String errorMsgRegex) {
    LOG.debug("asserting build failure on stop");
    com.zylitics.btbr.test.model.Build buildDetails = getBuildDetails();
    assertNotNull(buildDetails.getEndDate());
    assertTrue(buildDetails.getEndDate().isAfter(buildDetails.getStartDate()));
    assertFalse(buildDetails.isSuccess());
    assertNotNull(buildDetails.getError());
    assertTrue(buildDetails.getError().matches(errorMsgRegex));
    LOG.debug("build failure on stop assert done");
  }
  
  private SearchHits getSearchHits(SearchRequest searchRequest) throws IOException {
    SearchResponse searchResponse = restHighLevelClient.search(searchRequest,
        RequestOptions.DEFAULT);
    return searchResponse.getHits();
  }
  
  private void updateSession(String sessionId) {
    // update sessionId
    String sql = "UPDATE bt_build SET session_key = :session_key\n" +
        "WHERE bt_build_id = :bt_build_id;";
    Map<String, SqlParameterValue> updateParams = new HashMap<>();
    updateParams.put("session_key", new SqlParameterValue(Types.VARCHAR, sessionId));
    updateParams.put("bt_build_id", new SqlParameterValue(Types.INTEGER, buildId));
    assertEquals(1, jdbc.update(sql, updateParams));
  }
  
  // waits until vm delete date is updated to keep the vm running for these tests, if we don't wait,
  // tests will finish after getting a response back, causing spring to shutdown midway. Obviously
  // this can't happen on production because the server once started doesn't stops on it's own.
  private void waitUntilBuildCompletes(int buildVMId, int timeoutSec,
                                       int sleepBetweenPollSec) throws Exception {
    Instant startTime = Instant.now();
    while (true) {
      String sql = "SELECT delete_date FROM bt_build_vm WHERE bt_build_vm_id = :bt_build_vm_id;";
      SqlParameterSource namedParams = new MapSqlParameterSource("bt_build_vm_id",
          new SqlParameterValue(Types.INTEGER, buildVMId));
      // queryForObject throws exception if row doesn't exist, use wisely. 'query' is always safe
      // as we can check for existence of row.
      if (jdbc.queryForObject(sql, namedParams, (rs, rowNum) -> rs.getDate("delete_date")) != null)
      {
        LOG.debug("Delete date was updated");
        break;
      }
      if (startTime.until(Instant.now(), ChronoUnit.SECONDS) >= timeoutSec) {
        throw new TimeoutException("Waited for " + timeoutSec + " seconds but vm's deleted date" +
            " didn't update");
      }
      //noinspection BusyWait
      Thread.sleep(TimeUnit.MILLISECONDS.convert(sleepBetweenPollSec, TimeUnit.SECONDS));
    }
    // after delete date is updated, wait for few more seconds before finishing test to give time
    // to vm delete url invocation.
    Thread.sleep(5000);
  }
  
  private com.zylitics.btbr.test.model.Build getBuildDetails() {
    String sql = "SELECT start_date AT TIME ZONE :tz AS start_date," +
        " end_date AT TIME ZONE :tz AS end_date, is_success, error" +
        " FROM bt_build WHERE bt_build_id = :bt_build_id";
    Map<String, SqlParameterValue> params = new HashMap<>();
    params.put("bt_build_id", new SqlParameterValue(Types.INTEGER, buildId));
    params.put("tz", new SqlParameterValue(Types.VARCHAR, DESIRED_OFFSET));
    SqlRowSet rowSet = jdbc.queryForRowSet(sql, params);
    assertTrue(rowSet.next());
    return new com.zylitics.btbr.test.model.Build()
        .setStartDate(DateTimeUtil.sqlTimestampToLocal(rowSet.getTimestamp("start_date")))
        .setEndDate(DateTimeUtil.sqlTimestampToLocal(rowSet.getTimestamp("end_date")))
        .setSuccess(rowSet.getBoolean("is_success"))
        .setError(rowSet.getString("error"));
  }
  
  private boolean didBuildRequestComplete() {
    String sql = "SELECT completed FROM bt_build_request br\n" +
        "INNER JOIN bt_build bu ON (br.bt_build_request_id = bu.bt_build_request_id)\n" +
        "WHERE bu.bt_build_id = :bt_build_id";
    Map<String, SqlParameterValue> params = new HashMap<>();
    params.put("bt_build_id", new SqlParameterValue(Types.INTEGER, buildId));
    return jdbc.query(sql, params, (rs, rowNum) -> rs.getBoolean(1)).get(0);
  }
  
  private LocalDateTime getBuildAllDoneDate() {
    String sql = "SELECT all_done_date AT TIME ZONE :tz AS all_done_date" +
        " FROM bt_build WHERE bt_build_id = :bt_build_id;";
    Map<String, SqlParameterValue> params = new HashMap<>();
    params.put("bt_build_id", new SqlParameterValue(Types.INTEGER, buildId));
    params.put("tz", new SqlParameterValue(Types.VARCHAR, DESIRED_OFFSET));
    SqlRowSet rowSet = jdbc.queryForRowSet(sql, params);
    assertTrue(rowSet.next());
    return DateTimeUtil.sqlTimestampToLocal(rowSet.getTimestamp("all_done_date"));
  }
  
  private int getMinutesConsumed(int userId) {
    String sql = "SELECT minutes_consumed FROM quota AS q INNER JOIN zluser AS z" +
        " ON (q.organization_id = z.organization_id) WHERE z.zluser_id = :zluser_id;";
    Map<String, SqlParameterValue> params = new HashMap<>();
    params.put("zluser_id", new SqlParameterValue(Types.INTEGER, userId));
    SqlRowSet rowSet = jdbc.queryForRowSet(sql, params);
    assertTrue(rowSet.next());
    return rowSet.getInt("minutes_consumed");
  }
  
  private com.zylitics.btbr.test.model.BuildStatus getBuildStatusDetails(int testVersionId) {
    String sql = "SELECT status, zwl_executing_line," +
        " start_date AT TIME ZONE :tz AS start_date, end_date AT TIME ZONE :tz AS end_date," +
        " error, error_from_pos, error_to_pos FROM bt_build_status" +
        " WHERE bt_build_id = :bt_build_id AND bt_test_version_id = :bt_test_version_id;";
    Map<String, SqlParameterValue> params = new HashMap<>();
    params.put("bt_build_id", new SqlParameterValue(Types.INTEGER, buildId));
    params.put("bt_test_version_id", new SqlParameterValue(Types.INTEGER, testVersionId));
    params.put("tz", new SqlParameterValue(Types.VARCHAR, DESIRED_OFFSET));
    SqlRowSet rowSet = jdbc.queryForRowSet(sql, params);
    assertTrue(rowSet.next());
    return new com.zylitics.btbr.test.model.BuildStatus()
        .setStatus(TestStatus.valueOf(rowSet.getString("status")))
        .setZwlExecutingLine(rowSet.getInt("zwl_executing_line"))
        .setStartDate(DateTimeUtil.sqlTimestampToLocal(rowSet.getTimestamp("start_date")))
        .setEndDate(DateTimeUtil.sqlTimestampToLocal(rowSet.getTimestamp("end_date")))
        .setError(rowSet.getString("error"))
        .setErrorFrom(rowSet.getString("error_from_pos"))
        .setErrorTo(rowSet.getString("error_to_pos"));
  }
  
  private void setBuildDirName() {
    buildDirName = "build-" + buildId;
  }
}
