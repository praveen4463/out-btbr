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

import java.io.IOException;

/*
This class is suitable to be a declared a @Component and could loaded by spring, but since it's
stateful, a Request scope is required before wiring it. This is not done because this class will
be used by the runner that executes by a separate thread that the one takes and executes api request
, spring disallows request/session scope beans from being accessed by any other thread than the one
which took the request. There could be workarounds, but I didn't want to go into them, will just
wire it via Factory. Reference: https://stackoverflow.com/a/21355974/1624454
 */
public class EsdbShotMetadataProvider extends AbstractBulkSaveProvider<ShotMetadata>
    implements ShotMetadataProvider {
  
  private EsdbShotMetadataProvider(APICoreProperties apiCoreProperties,
                                   RestHighLevelClient client) {
    super((listener ->
        BulkProcessor.builder((request, bulkListener) ->
            client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener), listener)
            .setBulkActions(apiCoreProperties.getShot().getShotMetadataFlushRecords())
            .setConcurrentRequests(1) // keep it 1 so that bulk can execute on separate thread
            .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueSeconds(1),
                apiCoreProperties.getEsdb().getMaxRetries()))
            .build()),
        apiCoreProperties);
  }
  
  EsdbShotMetadataProvider(APICoreProperties apiCoreProperties, BulkProcessor bulkProcessor) {
    super((l) -> bulkProcessor, apiCoreProperties);
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
          .field(ShotMetadataIndexFields.TEST_VERSION_ID, smd.getTestVersionId())
          .field(ShotMetadataIndexFields.BUILD_KEY, smd.getBuildKey())
          .field(ShotMetadataIndexFields.SESSION_KEY, smd.getSessionKey())
          .field(ShotMetadataIndexFields.AT_LINE_ZWL, smd.getAtLineZwl())
          .field(ShotMetadataIndexFields.CREATE_DATE, smd.getCreateDate());
    }
    builder.endObject();
    return builder;
  }
  
  @Override
  String getIndex() {
    return apiCoreProperties.getEsdb().getShotMetadataIndex();
  }
  
  public static class Factory implements ShotMetadataProvider.Factory {
  
    @Override
    public ShotMetadataProvider create(APICoreProperties apiCoreProperties,
                                       RestHighLevelClient client) {
      return new EsdbShotMetadataProvider(apiCoreProperties, client);
    }
  }
}
