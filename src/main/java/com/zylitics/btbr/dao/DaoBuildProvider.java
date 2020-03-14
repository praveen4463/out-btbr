package com.zylitics.btbr.dao;

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
    String sql = "SELECT" +
        " bu.build_key" +
        ", bc.shot_bucket_session_storage, bc.shot_take_test_shot" +
        ", bc.program_output_flush_no, bc.program_output_flush_millis, bc.server_screen_size" +
        ", bc.server_timezone_with_dst, bc.wd_browser_name, bc.wd_browser_version" +
        ", bc.wd_platform_name, bc.wd_accept_insecure_certs, bc.wd_page_load_strategy" +
        ", bc.wd_set_window_rect, bc.wd_timeouts_script, bc.wd_timeouts_page_load" +
        ", bc.wd_timeouts_implicit, bc.wd_timeouts_element_access" +
        ", bc.wd_strict_file_interactability" +
        ", bc.wd_unhandled_prompt_behavior, bc.brw_is_full_screen" +
        ", bc.chrome_enable_network, bc.chrome_enable_page, bc.build_abort_on_failure" +
        " FROM bt_build AS bu INNER JOIN bt_build_capability AS bc" +
        " ON (bu.bt_build_capability_id = bc.bt_build_capability_id)" +
        " where bu.bt_build_id = :bt_build_id;";
    
    SqlParameterSource namedParams = new MapSqlParameterSource("bt_build_id",
        new SqlParameterValue(Types.INTEGER, buildId));
    
    Build build = jdbc.queryForObject(sql, namedParams, (rs, rowNum) ->
        new Build()
            .setBuildId(buildId)
            .setBuildKey(rs.getString("build_key"))
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
                .setWdTimeoutsImplicit(rs.getInt("wd_timeouts_implicit"))
                .setWdTimeoutsElementAccess(rs.getInt("wd_timeouts_element_access"))
                .setWdStrictFileInteractability(rs.getBoolean("wd_strict_file_interactability"))
                .setWdUnhandledPromptBehavior(rs.getString("wd_unhandled_prompt_behavior"))
                .setBrwIsFullScreen(rs.getBoolean("brw_is_full_screen"))
                .setChromeEnableNetwork(rs.getBoolean("chrome_enable_network"))
                .setChromeEnablePage(rs.getBoolean("chrome_enable_page"))
                .setBuildAbortOnFailure(rs.getBoolean("build_abort_on_failure"))));
    return Optional.ofNullable(build);
  }
  
  @Override
  public int updateBuild(Build build) {
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
