package com.zylitics.btbr.esdb;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.model.ZwlProgramOutput;
import com.zylitics.btbr.runner.provider.ZwlProgramOutputProvider;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/*
 * This is a stateful component.
 * A RequestScoped bean, this will be injected into the controller once but will act as a proxy.
 * Whenever a new request comes, a new instance of this bean will be created with a reference to the
 * incoming request. Every call on the injected proxy will be delegated to it's own target instance
 * by matching the current request.
 * https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#beans-factory-scopes-other-injection
 * !! Since a new VM is created for every build/request, it doesn't matter whether this is a
 * singleton or RequestScope bean (Once it is injected into main controller, it will be used only
 * once for the life of application instance). It is still marked RequestScope so that it is
 * semantically valid to be injected into the controller together with the singleton beans.
 */
@Component
@RequestScope
// BulkProcessor https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.4/java-rest-high-document-bulk.html
class EsdbZwlProgramOutputProvider extends AbstractBulkSaveProvider<ZwlProgramOutput>
    implements ZwlProgramOutputProvider {
  
  private final APICoreProperties apiCoreProperties;
  private final RestHighLevelClient client;
  
  @Autowired
  EsdbZwlProgramOutputProvider(APICoreProperties apiCoreProperties,
                                 RestHighLevelClient client) {
    this.apiCoreProperties = apiCoreProperties;
    this.client = client;
  }
  
  EsdbZwlProgramOutputProvider(APICoreProperties apiCoreProperties, BulkProcessor bulkProcessor) {
    this.apiCoreProperties = apiCoreProperties;
    this.client = null; // won't be required, so this is fine.
    setBulkProcessor(bulkProcessor);
  }
  
  @Override
  public void setBuildCapability(BuildCapability buildCapability) {
    BulkProcessor.Builder builder = BulkProcessor.builder(
        (request, bulkListener) ->
            client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
        new Listener())
        .setConcurrentRequests(1) // keep it 1 so that bulk can execute on separate thread
        .setBackoffPolicy(
            BackoffPolicy.exponentialBackoff(TimeValue.timeValueSeconds(1),
                apiCoreProperties.getEsdb().getMaxRetries()));
    if (buildCapability.getProgramOutputFlushNo() > 0) {
      builder.setBulkActions(buildCapability.getProgramOutputFlushNo());
    } else if (buildCapability.getProgramOutputFlushMillis() > 0) {
      builder.setFlushInterval(new TimeValue(buildCapability.getProgramOutputFlushMillis(),
          TimeUnit.MILLISECONDS));
    } else {
      builder.setBulkActions(apiCoreProperties.getRunner().getProgramOutputFlushNo());
    }
    setBulkProcessor(builder.build());
  }
  
  // !!! make sure that field names and their field types are same as what is in index
  // 'zwl_program_output'
  @Override
  XContentBuilder getAsXContentBuilder(ZwlProgramOutput zpo) throws IOException {
    XContentBuilder builder = XContentFactory.jsonBuilder();
    builder.startObject();
    {
      builder
          .field(ZwlProgramOutputIndexFields.BUILD_ID, zpo.getBuildId())
          .field(ZwlProgramOutputIndexFields.TEST_VERSION_ID, zpo.getTestVersionId())
          .field(ZwlProgramOutputIndexFields.OUTPUT, zpo.getOutput())
          .field(ZwlProgramOutputIndexFields.ENDED, zpo.isEnded())
          .field(ZwlProgramOutputIndexFields.CREATE_DATE, zpo.getCreateDate());
    }
    builder.endObject();
    return builder;
  }
  
  @Override
  String getIndex() {
    return apiCoreProperties.getEsdb().getZwlProgramOutputIndex();
  }
}
