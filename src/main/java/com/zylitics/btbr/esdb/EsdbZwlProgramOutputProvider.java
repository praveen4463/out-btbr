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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

// BulkProcessor https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.4/java-rest-high-document-bulk.html
public class EsdbZwlProgramOutputProvider extends AbstractBulkSaveProvider<ZwlProgramOutput>
    implements ZwlProgramOutputProvider {
  
  private EsdbZwlProgramOutputProvider(APICoreProperties apiCoreProperties,
                                       RestHighLevelClient client,
                                       BuildCapability buildCapability) {
    super((listener) -> {
      BulkProcessor.Builder builder = BulkProcessor.builder((request, bulkListener) ->
          client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener), listener)
          .setConcurrentRequests(1) // keep it 1 so that bulk can execute on separate thread
          .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueSeconds(1),
              apiCoreProperties.getEsdb().getMaxRetries()));
      if (buildCapability.getProgramOutputFlushNo() > 0) {
        builder.setBulkActions(buildCapability.getProgramOutputFlushNo());
      } else if (buildCapability.getProgramOutputFlushMillis() > 0) {
        builder.setFlushInterval(new TimeValue(buildCapability.getProgramOutputFlushMillis(),
            TimeUnit.MILLISECONDS));
      } else {
        builder.setBulkActions(apiCoreProperties.getRunner().getProgramOutputFlushNo());
      }
      return builder.build();
    }, apiCoreProperties);
  }
  
  EsdbZwlProgramOutputProvider(APICoreProperties apiCoreProperties, BulkProcessor bulkProcessor) {
    super((l) -> bulkProcessor, apiCoreProperties);
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
  
  public static class Factory implements ZwlProgramOutputProvider.Factory {
  
    @Override
    public ZwlProgramOutputProvider create(APICoreProperties apiCoreProperties,
                                           RestHighLevelClient client,
                                           BuildCapability buildCapability) {
      return new EsdbZwlProgramOutputProvider(apiCoreProperties, client, buildCapability);
    }
  }
}
