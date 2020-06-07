package com.zylitics.btbr.dao;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.runner.provider.BuildProvider;
import com.zylitics.btbr.runner.provider.BuildUpdateOnComplete;
import com.zylitics.btbr.util.CollectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Types;
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
    Preconditions.checkArgument(buildId > 0, "build is required");
    
    String sql = "SELECT" +
        " bu.build_key" +
        ", bu.bt_build_vm_id" +
        ", bu.is_success" +
        ", project.zluser_id" +
        ", bc.shot_bucket_session_storage" +
        ", bc.shot_take_test_shot" +
        ", bc.program_output_flush_no" +
        ", bc.program_output_flush_millis" +
        ", bc.wd_browser_name" +
        ", bc.wd_browser_version" +
        ", bc.wd_platform_name" +
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
        ", bc.wd_ie_introduce_flakiness_by_ignoring_security_domains" +
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
        ", bc.build_abort_on_failure" +
        ", bc.build_aet_keep_single_window" +
        ", bc.build_aet_update_url_blank" +
        ", bc.build_aet_reset_timeouts" +
        ", bc.build_aet_delete_all_cookies" +
        " FROM bt_build AS bu INNER JOIN bt_build_capability AS bc" +
        " ON (bu.bt_build_capability_id = bc.bt_build_capability_id)" +
        " INNER JOIN bt_project AS project" +
        " ON (bu.bt_project_id = project.bt_project_id)" +
        " where bu.bt_build_id = :bt_build_id;";
    
    SqlParameterSource namedParams = new MapSqlParameterSource("bt_build_id",
        new SqlParameterValue(Types.INTEGER, buildId));
    
    List<Build> build = jdbc.query(sql, namedParams, (rs, rowNum) ->
        new Build()
            .setBuildId(buildId)
            .setBuildKey(rs.getString("build_key"))
            .setBuildVMId(rs.getInt("bt_build_vm_id"))
            // cast rather than getBoolean because this method always returns 'false' as default
            // value whereas we want to see a null if it's null.
            .setSuccess((Boolean) rs.getObject("is_success"))
            .setUserId(rs.getInt("zluser_id"))
            .setBuildCapability(new BuildCapability()
                .setShotBucketSessionStorage(rs.getString("shot_bucket_session_storage"))
                .setShotTakeTestShot(rs.getBoolean("shot_take_test_shot"))
                .setProgramOutputFlushNo(rs.getInt("program_output_flush_no"))
                .setProgramOutputFlushMillis(rs.getLong("program_output_flush_millis"))
                .setWdBrowserName(rs.getString("wd_browser_name"))
                .setWdBrowserVersion(rs.getString("wd_browser_version"))
                .setWdPlatformName(rs.getString("wd_platform_name"))
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
                .setWdIeIntroduceFlakinessByIgnoringSecurityDomains(rs.getBoolean(
                    "wd_ie_introduce_flakiness_by_ignoring_security_domains"))
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
                .setWdBrwStartMaximize(rs.getBoolean("wd_brw_start_maximize"))
                .setBuildAbortOnFailure(rs.getBoolean("build_abort_on_failure"))
                .setBuildAetKeepSingleWindow(rs.getBoolean("build_aet_keep_single_window"))
                .setBuildAetUpdateUrlBlank(rs.getBoolean("build_aet_update_url_blank"))
                .setBuildAetResetTimeouts(rs.getBoolean("build_aet_reset_timeouts"))
                .setBuildAetDeleteAllCookies(rs.getBoolean("build_aet_delete_all_cookies"))));
    if (build.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(build.get(0));
  }
  
  @Override
  public int updateOnComplete(BuildUpdateOnComplete buildUpdateOnComplete) {
    Preconditions.checkNotNull(buildUpdateOnComplete, "buildUpdateOnComplete can't be null");
    
    String sql = "UPDATE bt_build SET end_date = :end_date, is_success = :is_success" +
        ", error = :error WHERE bt_build_id = :bt_build_id";
  
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(4));
  
    params.put("bt_build_id", new SqlParameterValue(Types.INTEGER,
        buildUpdateOnComplete.getBuildId()));
  
    params.put("end_date", new SqlParameterValue(Types.TIMESTAMP_WITH_TIMEZONE
        , buildUpdateOnComplete.getEndDate()));
  
    params.put("is_success", new SqlParameterValue(Types.BOOLEAN,
        buildUpdateOnComplete.isSuccess()));
  
    params.put("error", new SqlParameterValue(Types.OTHER, buildUpdateOnComplete.getError()));
  
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
  
    return jdbc.update(sql, namedParams);
  }
}
