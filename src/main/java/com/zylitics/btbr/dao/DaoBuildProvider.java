package com.zylitics.btbr.dao;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.model.*;
import com.zylitics.btbr.runner.TestStatus;
import com.zylitics.btbr.runner.provider.BuildProvider;
import com.zylitics.btbr.runner.provider.BuildUpdateOnComplete;
import com.zylitics.btbr.util.CollectionUtil;
import com.zylitics.btbr.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
class DaoBuildProvider extends AbstractDaoProvider implements BuildProvider {
  
  @Autowired
  DaoBuildProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public Optional<Build> getBuild(int buildId) {
    Preconditions.checkArgument(buildId > 0, "buildId is required");
    
    String sql = "SELECT" +
        " bu.build_key" +
        ", bu.name build_name" +
        ", bu.bt_build_vm_id" +
        ", bu.server_screen_size" +
        ", bu.server_timezone_with_dst" +
        ", bu.create_date AT TIME ZONE 'UTC' AS create_date" +
        ", bu.final_status" +
        ", bu.shot_bucket_session_storage" +
        ", bu.abort_on_failure" +
        ", bu.retryFailedTestsUpto" +
        ", bu.capture_shots" +
        ", bu.capture_driver_logs" +
        ", bu.notify_on_completion" +
        ", bu.aet_keep_single_window" +
        ", bu.aet_update_url_blank" +
        ", bu.aet_reset_timeouts" +
        ", bu.aet_delete_all_cookies" +
        ", bu.source_type" +
        ", bu.bt_build_request_id" +
        ", project.zluser_id" +
        ", project.bt_project_id" +
        ", org.organization_id" +
        ", org.git_enabled" +
        ", org.git_provider" +
        ", bc.wd_browser_name" +
        ", bc.wd_browser_version" +
        ", bc.wd_platform_name" +
        ", bc.wd_me_device_resolution" +
        ", bc.wd_accept_insecure_certs" +
        ", bc.wd_page_load_strategy" +
        ", bc.wd_set_window_rect" +
        ", bc.wd_timeouts_script" +
        ", bc.wd_timeouts_page_load" +
        ", bc.wd_timeouts_element_access" +
        ", bc.wd_strict_file_interactability" +
        ", bc.wd_unhandled_prompt_behavior" +
        ", bc.wd_ie_element_scroll_behavior" +
        ", bc.wd_ie_enable_persistent_hovering" +
        ", bc.wd_ie_require_window_focus" +
        ", bc.wd_ie_disable_native_events" +
        ", bc.wd_ie_destructively_ensure_clean_session" +
        ", bc.wd_ie_log_level" +
        ", bc.wd_chrome_verbose_logging" +
        ", bc.wd_chrome_silent_output" +
        ", bc.wd_chrome_enable_network" +
        ", bc.wd_chrome_enable_page" +
        ", bc.wd_firefox_log_level" +
        ", bc.wd_brw_start_maximize" +
        " FROM bt_build AS bu INNER JOIN bt_build_captured_capabilities AS bc" +
        " ON (bu.bt_build_id = bc.bt_build_id)" +
        " INNER JOIN bt_project AS project" +
        " ON (bu.bt_project_id = project.bt_project_id)" +
        " JOIN organization org USING (organization_id) " +
        " WHERE bu.bt_build_id = :bt_build_id;";
    
    SqlParameterSource namedParams = new MapSqlParameterSource("bt_build_id",
        new SqlParameterValue(Types.INTEGER, buildId));
    
    List<Build> build = jdbc.query(sql, namedParams, (rs, rowNum) ->
        new Build()
            .setBuildId(buildId)
            .setBuildKey(rs.getString("build_key"))
            .setBuildName(rs.getString("build_name"))
            .setBuildVMId(rs.getInt("bt_build_vm_id"))
            .setServerScreenSize(rs.getString("server_screen_size"))
            .setServerTimezone(rs.getString("server_timezone_with_dst"))
            .setCreateDateUTC(DateTimeUtil.sqlTimestampToLocal(rs.getTimestamp("create_date")))
            // cast rather than getBoolean because this method always returns 'false' as default
            // value whereas we want to see a null if it's null.
            .setFinalStatus(rs.getString("final_status") != null
                ? TestStatus.valueOf(rs.getString("final_status")) : null)
            .setShotBucketSessionStorage(rs.getString("shot_bucket_session_storage"))
            .setAbortOnFailure(rs.getBoolean("abort_on_failure"))
            .setRetryFailedTestsUpto(rs.getInt("retryFailedTestsUpto"))
            .setCaptureShots(rs.getBoolean("capture_shots"))
            .setCaptureDriverLogs(rs.getBoolean("capture_driver_logs"))
            .setNotifyOnCompletion(rs.getBoolean("notify_on_completion"))
            .setAetKeepSingleWindow(rs.getBoolean("aet_keep_single_window"))
            .setAetUpdateUrlBlank(rs.getBoolean("aet_update_url_blank"))
            .setAetResetTimeouts(rs.getBoolean("aet_reset_timeouts"))
            .setAetDeleteAllCookies(rs.getBoolean("aet_delete_all_cookies"))
            .setSourceType(BuildSourceType.valueOf(rs.getString("source_type")))
            .setBuildRequestId(rs.getLong("bt_build_request_id"))
            .setUserId(rs.getInt("zluser_id"))
            .setProjectId(rs.getInt("bt_project_id"))
            .setOrganization(new Organization()
                .setOrganizationId(rs.getInt("organization_id"))
                .setGitEnabled(rs.getBoolean("git_enabled"))
                .setGitProvider(rs.getString("git_provider") != null
                    ? GitProvider.valueOf(rs.getString("git_provider")) : null))
            .setBuildCapability(new BuildCapability()
                .setWdBrowserName(rs.getString("wd_browser_name"))
                .setWdBrowserVersion(rs.getString("wd_browser_version"))
                .setWdPlatformName(rs.getString("wd_platform_name"))
                .setWdMeDeviceResolution(rs.getString("wd_me_device_resolution"))
                .setWdAcceptInsecureCerts(rs.getBoolean("wd_accept_insecure_certs"))
                .setWdPageLoadStrategy(rs.getString("wd_page_load_strategy"))
                .setWdSetWindowRect(rs.getBoolean("wd_set_window_rect"))
                .setWdTimeoutsScript(rs.getInt("wd_timeouts_script"))
                .setWdTimeoutsPageLoad(rs.getInt("wd_timeouts_page_load"))
                .setWdTimeoutsElementAccess(rs.getInt("wd_timeouts_element_access"))
                .setWdStrictFileInteractability(rs.getBoolean("wd_strict_file_interactability"))
                .setWdUnhandledPromptBehavior(rs.getString("wd_unhandled_prompt_behavior"))
                .setWdIeElementScrollBehavior(rs.getString("wd_ie_element_scroll_behavior"))
                .setWdIeEnablePersistentHovering(rs.getBoolean("wd_ie_enable_persistent_hovering"))
                .setWdIeRequireWindowFocus(rs.getBoolean("wd_ie_require_window_focus"))
                .setWdIeDisableNativeEvents(rs.getBoolean("wd_ie_disable_native_events"))
                .setWdIeDestructivelyEnsureCleanSession(
                    rs.getBoolean("wd_ie_destructively_ensure_clean_session"))
                .setWdIeLogLevel(rs.getString("wd_ie_log_level"))
                .setWdChromeVerboseLogging(rs.getBoolean("wd_chrome_verbose_logging"))
                .setWdChromeSilentOutput(rs.getBoolean("wd_chrome_silent_output"))
                .setWdChromeEnableNetwork(rs.getBoolean("wd_chrome_enable_network"))
                .setWdChromeEnablePage(rs.getBoolean("wd_chrome_enable_page"))
                .setWdFirefoxLogLevel(rs.getString("wd_firefox_log_level"))
                .setWdBrwStartMaximize(rs.getBoolean("wd_brw_start_maximize"))));
    if (build.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(build.get(0));
  }
  
  @Override
  public int updateOnStart(int buildId, OffsetDateTime startDate) {
    Preconditions.checkArgument(buildId > 0, "buildId is required");
    
    String sql = "UPDATE bt_build SET start_date = :start_date WHERE bt_build_id = :bt_build_id";
  
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(2));
  
    params.put("bt_build_id", new SqlParameterValue(Types.INTEGER, buildId));
  
    params.put("start_date", new SqlParameterValue(Types.TIMESTAMP_WITH_TIMEZONE, startDate));
  
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
  
    return jdbc.update(sql, namedParams);
  }
  
  @Override
  public int updateOnComplete(BuildUpdateOnComplete buildUpdateOnComplete) {
    Preconditions.checkNotNull(buildUpdateOnComplete, "buildUpdateOnComplete can't be null");
    
    String sql = "UPDATE bt_build SET end_date = :end_date, final_status = :final_status" +
        ", error = :error WHERE bt_build_id = :bt_build_id";
  
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(4));
  
    params.put("bt_build_id", new SqlParameterValue(Types.INTEGER,
        buildUpdateOnComplete.getBuildId()));
  
    params.put("end_date", new SqlParameterValue(Types.TIMESTAMP_WITH_TIMEZONE
        , buildUpdateOnComplete.getEndDate()));
  
    params.put("final_status", new SqlParameterValue(Types.OTHER,
        buildUpdateOnComplete.getFinalStatus()));
  
    params.put("error", new SqlParameterValue(Types.OTHER, buildUpdateOnComplete.getError()));
  
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
  
    return jdbc.update(sql, namedParams);
  }
  
  @Override
  public int updateOnAllTasksDone(int buildId, OffsetDateTime allDoneDate) {
    Preconditions.checkArgument(buildId > 0, "buildId is required");
    Preconditions.checkNotNull(allDoneDate, "allDoneDate can't be null");
  
    String sql = "UPDATE bt_build SET all_done_date = :all_done_date WHERE" +
        " bt_build_id = :bt_build_id";
  
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(2));
  
    params.put("bt_build_id", new SqlParameterValue(Types.INTEGER, buildId));
  
    params.put("all_done_date", new SqlParameterValue(Types.TIMESTAMP_WITH_TIMEZONE, allDoneDate));
  
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
  
    return jdbc.update(sql, namedParams);
  }
}
