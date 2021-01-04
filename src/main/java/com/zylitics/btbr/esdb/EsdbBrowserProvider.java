package com.zylitics.btbr.esdb;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.runner.provider.BrowserProvider;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class EsdbBrowserProvider implements BrowserProvider {
  
  private final APICoreProperties apiCoreProperties;
  
  private final RestHighLevelClient client;
  
  @Autowired
  EsdbBrowserProvider(APICoreProperties apiCoreProperties, RestHighLevelClient client) {
    this.apiCoreProperties = apiCoreProperties;
    this.client = client;
  }
  
  @Override
  public Optional<String> getDriverVersion(String browser, String version) throws RuntimeException {
    try {
      SearchRequest searchRequest =
          new SearchRequest(apiCoreProperties.getEsdb().getBrowserIndex());
      SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
      sourceBuilder
          .query(QueryBuilders.boolQuery()
              .must(QueryBuilders.termQuery(BrowserIndexFields.NAME, browser))
              .must(QueryBuilders.termQuery(BrowserIndexFields.DISPLAY_VERSION, version)))
          .fetchSource(BrowserIndexFields.DRIVER_VERSION, null)
          .size(1);
      searchRequest.source(sourceBuilder);
      SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
      SearchHits searchHits = searchResponse.getHits();
      if (searchHits.getHits().length == 0) {
        return Optional.empty();
      }
      SearchHit hit = searchHits.getHits()[0];
      Map<String, Object> source = hit.getSourceAsMap();
      return Optional.of((String) source.get(BrowserIndexFields.DRIVER_VERSION));
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
