package com.zylitics.btbr.dao;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.runner.provider.BuildProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.HashMap;
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
        ", bu.zluser_id" +
        ", bc.shot_bucket_session_storage" +
        ", bc.shot_take_test_shot" +
        ", bc.program_output_flush_no" +
        ", bc.program_output_flush_millis" +
        ", bc.server_screen_size" +
        ", bc.server_timezone_with_dst" +
        ", bc.wd_browser_name" +
        ", bc.wd_browser_version" +
        ", bc.wd_platform_name" +
        ", bc.wd_accept_insecure_certs" +
        ", bc.wd_page_load_strategy" +
        ", bc.wd_set_window_rect" +
        ", bc.wd_timeouts_script" +
        ", bc.wd_timeouts_page_load" +
        ", bc.wd_timeouts_implicit" +
        ", bc.wd_timeouts_element_access" +
        ", bc.wd_strict_file_interactability" +
        ", bc.wd_unhandled_prompt_behavior" +
        ", bc.wd_ie_element_scroll_behavior" +
        ", bc.wd_ie_enable_persistent_hovering" +
        ", bc.wd_ie_introduce_flakiness_by_ignoring_security_domains" +
        ", bc.wd_ie_require_window_focus" +
        ", bc.wd_ie_disable_native_events" +
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
        " where bu.bt_build_id = :bt_build_id;";
    
    SqlParameterSource namedParams = new MapSqlParameterSource("bt_build_id",
        new SqlParameterValue(Types.INTEGER, buildId));
    
    Build build = jdbc.queryForObject(sql, namedParams, (rs, rowNum) ->
        new Build()
            .setBuildId(buildId)
            .setBuildKey(rs.getString("build_key"))
            .setBuildVMId(rs.getInt("bt_build_vm_id"))
            .setUserId(rs.getInt("zluser_id"))
            .setBuildCapability(new BuildCapability()
                .setShotBucketSessionStorage(rs.getString("shot_bucket_session_storage"))
                .setShotTakeTestShot(rs.getBoolean("shot_take_test_shot"))
                .setServerScreenSize(rs.getString("server_screen_size"))
                .setProgramOutputFlushNo(rs.getInt("program_output_flush_no"))
                .setProgramOutputFlushMillis(rs.getLong("program_output_flush_millis"))
                .setServerTimeZoneWithDst(rs.getString("server_timezone_with_dst"))
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
                .setWdIeLogLevel(rs.getString("wd_ie_log_level"))
                .setWdChromeVerboseLogging(rs.getBoolean("wd_chrome_verbose_logging"))
                .setWdChromeSilentOutput(rs.getBoolean("wd_chrome_silent_output"))
                .setWdChromeEnableNetwork(rs.getBoolean("wd_chrome_enable_network"))
                .setWdChromeEnablePage(rs.getBoolean("wd_chrome_enable_page"))
                .setWdIeLogLevel(rs.getString("wd_firefox_log_level"))
                .setWdBrwStartMaximize(rs.getBoolean("wd_brw_start_maximize"))
                .setBuildAbortOnFailure(rs.getBoolean("build_abort_on_failure"))
                .setBuildAetKeepSingleWindow(rs.getBoolean("build_aet_keep_single_window"))
                .setBuildAetUpdateUrlBlank(rs.getBoolean("build_aet_update_url_blank"))
                .setBuildAetResetTimeouts(rs.getBoolean("build_aet_reset_timeouts"))
                .setBuildAetDeleteAllCookies(rs.getBoolean("build_aet_delete_all_cookies"))));
    return Optional.ofNullable(build);
  }
  
  @Override
  public int updateBuild(Build build) {
    Preconditions.checkNotNull(build, "build can't be null");
    Preconditions.checkNotNull(build.getEndDate(), "endDate can't be null");
    
    String sql = "UPDATE bt_build SET end_date = :end_date, is_success = :is_success" +
        ", error = :error WHERE bt_build_id = :bt_build_id";
  
    Map<String, SqlParameterValue> params = new HashMap<>(6);
    
    params.put("bt_build_id", new SqlParameterValue(Types.INTEGER, build.getBuildId()));
    
    params.put("end_date", new SqlParameterValue(Types.TIMESTAMP_WITH_TIMEZONE
        , build.getEndDate()));
    
    params.put("is_success", new SqlParameterValue(Types.BOOLEAN, build.isSuccess()));
    
    params.put("error", new SqlParameterValue(
        Strings.isNullOrEmpty(build.getError()) ? Types.NULL : Types.VARCHAR, build.getError()));
        
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
    
    return jdbc.update(sql, namedParams);
  }
}
