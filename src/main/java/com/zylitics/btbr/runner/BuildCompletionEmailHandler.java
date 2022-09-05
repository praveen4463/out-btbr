package com.zylitics.btbr.runner;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.sendgrid.helpers.mail.objects.Email;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.EmailInfo;
import com.zylitics.btbr.model.FailedTestDetail;
import com.zylitics.btbr.model.TestVersion;
import com.zylitics.btbr.runner.provider.EmailPreferenceProvider;
import com.zylitics.btbr.service.EmailService;
import com.zylitics.btbr.service.SendTemplatedEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class BuildCompletionEmailHandler {
  
  private static final Logger LOG = LoggerFactory.getLogger(BuildCompletionEmailHandler.class);
  
  private final APICoreProperties apiCoreProperties;
  
  private final EmailService emailService;
  
  private final EmailPreferenceProvider emailPreferenceProvider;
  
  BuildCompletionEmailHandler(APICoreProperties apiCoreProperties,
                              EmailService emailService,
                              EmailPreferenceProvider emailPreferenceProvider) {
    this.apiCoreProperties = apiCoreProperties;
    this.emailService = emailService;
    this.emailPreferenceProvider = emailPreferenceProvider;
  }
  
  void handle(Build build,
              boolean isSuccess,
              long totalPassed,
              long totalFailed,
              List<FailedTestDetail> failedTestVersionsDetail) {
    APICoreProperties.Email emailProps = apiCoreProperties.getEmail();
    
    int orgId = build.getOrganization().getOrganizationId();
    
    List<String> emails = isSuccess
        ? emailPreferenceProvider.getEmailsForBuildSuccess(orgId)
        : emailPreferenceProvider.getEmailsForBuildFailure(orgId);
    
    if (emails.size() == 0) {
      return;
    }
    
    String templateId = isSuccess
        ? emailProps.getEmailBuildSuccessTmpId()
        : emailProps.getEmailBuildFailedTmpId();
    
    List<Email> tos = new ArrayList<>();
    emails.forEach(e -> tos.add(new Email(e)));
  
    EmailInfo emailInfo = new EmailInfo()
        .setFromName(emailProps.getEmailSenderName())
        .setFrom(emailProps.getSupportEmail())
        .setTos(tos);
    
    // Build template data
    
    String buildIdentifier = "#" + build.getBuildId();
    if (!Strings.isNullOrEmpty(build.getBuildName())) {
      buildIdentifier += " " + build.getBuildName();
    }
    
    String linkToBuild = String.format("%s/%s?project=%s&simple_view=1",
        apiCoreProperties.getFrontEndBaseUrl() + emailProps.getBuildsPage(),
        build.getBuildId(),
        build.getProjectId());
    
    String linkToEmailSettings = String.format("%s?project=%s",
        apiCoreProperties.getFrontEndBaseUrl() + emailProps.getEmailPrefPage(),
        build.getProjectId());
    
    StringBuilder error = new StringBuilder();
    
    for (FailedTestDetail failedTestDetail : failedTestVersionsDetail) {
      TestVersion testVersion = failedTestDetail.getTestVersion();
      // !!For timestamp, we'll convert it to EST for now for all emails.
      // TODO: later put a pref record in db for timezone and convert to that one.
      String failedAt = failedTestDetail.getTimestamp()
          .withZoneSameInstant(ZoneId.of("America/Montreal"))
          .format(DateTimeFormatter.ofPattern("MMM d, h:mm:ss a")) + " EST";
      error.append(
          String.format("<p class=\"test-name\">%s > %s</p>" +
              "<p class=\"error-detail\">Failed at: %s</p>" +
              "<pre><div class=\"error\">%s</div></pre>",
              testVersion.getFile().getName(),
              testVersion.getTest().getName(),
              failedAt,
              failedTestDetail.getError())
          );
    }
  
    ImmutableMap.Builder<String, Object> templateDataBuilder = ImmutableMap.builder();
    templateDataBuilder.put("build_identifier", buildIdentifier);
    templateDataBuilder.put("link_to_build", linkToBuild);
    templateDataBuilder.put("link_to_emails_settings_def_proj", linkToEmailSettings);
    templateDataBuilder.put("passed", totalPassed);
    templateDataBuilder.put("failed", totalFailed);
    templateDataBuilder.put("error", error);
    
    SendTemplatedEmail sendTemplatedEmail = new SendTemplatedEmail(emailInfo,
        templateId,
        templateDataBuilder.build());
    
    emailService.sendAsync(sendTemplatedEmail, null,
        (v) -> LOG.error("Priority: Couldn't send a build completion email to org: " + orgId));
  }
}
