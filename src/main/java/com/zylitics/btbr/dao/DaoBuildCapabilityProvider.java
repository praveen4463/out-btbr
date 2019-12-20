package com.zylitics.btbr.dao;

import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.runner.BuildCapabilityProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.Optional;

@Repository
public class DaoBuildCapabilityProvider implements BuildCapabilityProvider {
  
  private final NamedParameterJdbcTemplate jdbc;
  
  @Autowired
  DaoBuildCapabilityProvider(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }
  
  @Override
  public Optional<BuildCapability> getBuildCapability(int buildCapabilityId) {
    String sql = "SELECT shot_bucket_session_storage, shot_take_test_shot" +
        ", command_result_flush_records, command_result_flush_millis, server_screen_size" +
        ", server_timezone_with_dst, wd_browser_name, wd_browser_version" +
        ", wd_platform_name, wd_accept_insecure_certs, wd_page_load_strategy, wd_set_window_rect" +
        ", wd_timeouts_script, wd_timeouts_page_load, wd_timeouts_implicit" +
        ", wd_strict_file_interactability, wd_unhandled_prompt_behavior, brw_is_full_screen" +
        ", chrome_enable_network, chrome_enable_page FROM bt_build_capability WHERE" +
        " bt_build_capability_id = :bt_build_capability_id;";
  
    SqlParameterSource namedParams = new MapSqlParameterSource("bt_build_capability_id"
        , new SqlParameterValue(Types.INTEGER, buildCapabilityId));
  
    BuildCapability buildCapability = jdbc.queryForObject(sql, namedParams, (rs, rowNum) ->
        new BuildCapability()
            .setShotBucketSessionStorage(rs.getString("shot_bucket_session_storage"))
            .setShotTakeTestShot(rs.getBoolean("shot_take_test_shot"))
            .setServerScreenSize(rs.getString("server_screen_size"))
            .setCommandResultFlushRecords(rs.getInt("command_result_flush_records"))
            .setCommandResultFlushMillis(rs.getLong("command_result_flush_millis"))
            .setServerTimeZoneWithDst(rs.getString("server_timezone_with_dst"))
            .setWdBrowserName(rs.getString("wd_browser_name"))
            .setWdBrowserVersion(rs.getString("wd_browser_version"))
            .setWdPlatformName(rs.getString("wd_platform_name"))
            .setWdAcceptInsecureCerts(rs.getBoolean("wd_accept_insecure_certs"))
            .setWdPageLoadStrategy(rs.getString("wd_page_load_strategy"))
            .setWdSetWindowRect(rs.getBoolean("wd_set_window_rect"))
            .setWdTimeoutsScript(rs.getInt("wd_timeouts_script"))
            .setWdTimeoutsPageLoad(rs.getInt("wd_timeouts_page_load"))
            .setWdTimeoutsImplicit(rs.getInt("wd_timeouts_implicit"))
            .setWdStrictFileInteractability(rs.getBoolean("wd_strict_file_interactability"))
            .setWdUnhandledPromptBehavior(rs.getString("wd_unhandled_prompt_behavior"))
            .setBrwIsFullScreen(rs.getBoolean("brw_is_full_screen"))
            .setChromeEnableNetwork(rs.getBoolean("chrome_enable_network"))
            .setChromeEnablePage(rs.getBoolean("chrome_enable_page")));
    return Optional.ofNullable(buildCapability);
  }
}
