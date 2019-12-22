package com.zylitics.btbr.esdb;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildCapability;
import com.zylitics.btbr.model.BuildCommandResult;
import com.zylitics.btbr.runner.provider.BuildCommandResultProvider;
import org.elasticsearch.action.bulk.*;
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
 * A RequestScoped bean, this will be injected into the controller once but will act as a proxy
 * whenever a new request comes, a new instance of this bean will be created with a reference to the
 * incoming request. Every call on the injected proxy will be delegated to it's own target instance
 * by matching the current request.
 * https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#beans-factory-scopes-other-injection
 */
@Component
@RequestScope
// BulkProcessor https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.4/java-rest-high-document-bulk.html
class EsdbBuildCommandResultProvider extends AbstractBulkSaveProvider<BuildCommandResult>
    implements BuildCommandResultProvider {
  
  private final APICoreProperties apiCoreProperties;
  private final RestHighLevelClient client;
  
  @Autowired
  EsdbBuildCommandResultProvider(APICoreProperties apiCoreProperties,
                                 RestHighLevelClient client) {
    this.apiCoreProperties = apiCoreProperties;
    this.client = client;
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
    if (buildCapability.getCommandResultFlushRecords() != 0) {
      builder.setBulkActions(buildCapability.getCommandResultFlushRecords());
    } else if (buildCapability.getCommandResultFlushMillis() != 0) {
      builder.setFlushInterval(new TimeValue(buildCapability.getCommandResultFlushMillis(),
          TimeUnit.MILLISECONDS));
    } else {
      builder.setBulkActions(apiCoreProperties.getRunner().getCommandResultFlushRecords());
    }
    setBulkProcessor(builder.build());
  }
  
  // !!! make sure that field names and their field types are same as what is in index
  // 'bt_build_command_result'
  @Override
  XContentBuilder getAsXContentBuilder(BuildCommandResult bcr) throws IOException {
    XContentBuilder builder = XContentFactory.jsonBuilder();
    builder.startObject();
    {
      builder
          .field(BCRIndexFields.BUILD_ID, bcr.getBuildId())
          .field(BCRIndexFields.TEST_VERSION_ID, bcr.getTestVersionId())
          .field(BCRIndexFields.TEST_COMMAND_ID, bcr.getTestCommandId())
          .field(BCRIndexFields.TOOK_MILLIS, bcr.getTookMillis())
          .field(BCRIndexFields.IS_SUCCESS, bcr.isSuccess())
          .field(BCRIndexFields.ERROR, bcr.getError());
    }
    builder.endObject();
    return builder;
  }
  
  @Override
  String getIndex() {
    return apiCoreProperties.getEsdb().getBcrIndex();
  }
}
