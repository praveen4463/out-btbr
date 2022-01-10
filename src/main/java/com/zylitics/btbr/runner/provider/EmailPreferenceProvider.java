package com.zylitics.btbr.runner.provider;

import java.util.List;

public interface EmailPreferenceProvider {
  
  List<String> getEmailsForBuildFailure(int organizationId);
  
  List<String> getEmailsForBuildSuccess(int organizationId);
}
