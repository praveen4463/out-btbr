package com.zylitics.btbr.dao;

import com.zylitics.btbr.runner.provider.EmailPreferenceProvider;
import com.zylitics.btbr.util.CommonUtil;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

public class DaoEmailPreferenceProvider extends AbstractDaoProvider
    implements EmailPreferenceProvider {
  
  DaoEmailPreferenceProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public List<String> getEmailsForBuildFailure(int organizationId) {
    return getEmailsByEmailPref(organizationId, "build_failure");
  }
  
  @Override
  public List<String> getEmailsForBuildSuccess(int organizationId) {
    return getEmailsByEmailPref(organizationId, "build_success");
  }
  
  private List<String> getEmailsByEmailPref(int organizationId, String fieldName) {
    String sql = "SELECT email FROM zluser\n" +
        "JOIN email_preference USING(zluser_id)\n" +
        "WHERE organization_id = :organization_id\n" +
        String.format("AND %s = true", fieldName);
    return jdbc.query(sql,
        new SqlParamsBuilder().withInteger("organization_id", organizationId).build(),
        CommonUtil.getSingleString());
  }
}
