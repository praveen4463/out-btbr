package com.zylitics.btbr.dao;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.runner.provider.QuotaProvider;
import com.zylitics.btbr.util.CollectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Repository
class DaoQuotaProvider extends AbstractDaoProvider implements QuotaProvider {
  
  @Autowired
  DaoQuotaProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public int updateConsumed(Build build, LocalDateTime currentUTC) {
    Preconditions.checkNotNull(build, "build can't be null");
    Preconditions.checkNotNull(currentUTC, "currentUTC can't be null");
    
    // first get total time of this build. We won't take build.startTime but it's createDate so that
    // if we had to boot up a new vm for this build, the boot time can be included in this build time
    // as GCP start charging from that point. If we didn't boot a vm and used existing, this should
    // be fine too as some seconds will also get consumed by this build after we update this in remaining
    // processed like marking labels etc.
    long minutesConsumed = Duration.between(build.getCreateDateUTC(), currentUTC).toMinutes();
  
    String sql = "UPDATE quota AS q SET" +
        " minutes_consumed = COALESCE(minutes_consumed, 0) + :minutes_consumed" +
        " FROM zluser AS z WHERE q.billing_cycle_actual_end IS NULL" +
        " AND z.zluser_id = :zluser_id AND q.organization_id = z.organization_id;";
  
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(1));
  
    params.put("zluser_id", new SqlParameterValue(Types.INTEGER, build.getUserId()));
  
    params.put("minutes_consumed", new SqlParameterValue(Types.INTEGER, minutesConsumed));
  
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
  
    return jdbc.update(sql, namedParams);
  }
}
