package com.zylitics.btbr.esdb;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.ShotMetadata;
import com.zylitics.btbr.runner.provider.ShotMetadataProvider;
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

@Component
@RequestScope
class EsdbShotMetadataProvider extends AbstractBulkSaveProvider<ShotMetadata>
    implements ShotMetadataProvider {
  
  private final APICoreProperties apiCoreProperties;
  
  @Autowired
  EsdbShotMetadataProvider(APICoreProperties apiCoreProperties, RestHighLevelClient client) {
    this.apiCoreProperties = apiCoreProperties;
  
    BulkProcessor bulkProcessor = BulkProcessor.builder(
        (request, bulkListener) ->
            client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
        new Listener())
        .setBulkActions(apiCoreProperties.getRunner().getShotMetadataFlushRecords())
        .setConcurrentRequests(1) // keep it 1 so that bulk can execute on separate thread
        .setBackoffPolicy(
            BackoffPolicy.exponentialBackoff(TimeValue.timeValueSeconds(1)
                , apiCoreProperties.getEsdb().getMaxRetries()))
        .build();
    setBulkProcessor(bulkProcessor);
  }
  
  EsdbShotMetadataProvider(APICoreProperties apiCoreProperties, BulkProcessor bulkProcessor) {
    this.apiCoreProperties = apiCoreProperties;
    setBulkProcessor(bulkProcessor);
  }
  
  // !!! make sure that field names and their field types are same as what is in index
  // 'bt_shot_metadata'
  @Override
  XContentBuilder getAsXContentBuilder(ShotMetadata smd) throws IOException {
    XContentBuilder builder = XContentFactory.jsonBuilder();
    builder.startObject();
    {
      builder
          .field(ShotMetadataIndexFields.SHOT_NAME, smd.getShotName())
          .field(ShotMetadataIndexFields.BUILD_ID, smd.getBuildId())
          .field(ShotMetadataIndexFields.BUILD_KEY, smd.getBuildKey())
          .field(ShotMetadataIndexFields.SESSION_KEY, smd.getSessionKey())
          .field(ShotMetadataIndexFields.TEST_COMMAND_ID, smd.getTestCommandId())
          .field(ShotMetadataIndexFields.METHOD, smd.getMethod())
          .field(ShotMetadataIndexFields.WEB_DRIVER_COMMAND, smd.getWebDriverCommand())
          .timeField(ShotMetadataIndexFields.CREATE_DATE, smd.getCreateDate());
    }
    builder.endObject();
    return builder;
  }
  
  @Override
  String getIndex() {
    return apiCoreProperties.getEsdb().getShotMetadataIndex();
  }
}
