package com.zylitics.btbr.esdb;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.ShotMetadata;
import com.zylitics.btbr.shot.ShotNameProvider;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness= Strictness.STRICT_STUBS)
class EsdbShotMetadataProviderTest {
  
  private static final Logger LOG = LoggerFactory.getLogger(EsdbShotMetadataProviderTest.class);
  
  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Verify bulk flush requests on the given bulk trigger after")
  void verifyBulkTriggers() {
    int triggerBulkAfter = 3;
    Listener bulkProcessorListener = new Listener();
    // !!! most methods in this class are final, thus using mockito extension by dropping the text
    // file in classpath. https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html#Mocking_Final
    // if we don't use it, real implementations are called.
    RestHighLevelClient client = mock(RestHighLevelClient.class);
    
    // in actual code, the method bulkAsync executes the bulk request on a separate thread,
    // but here this will be on main thread, thus just one thread is used.
    doAnswer(invocationOnMock -> {
      Object[] args = invocationOnMock.getArguments();
      ActionListener<BulkResponse> listener = (ActionListener<BulkResponse>) args[2];
      
      // see org.elasticsearch.action.bulk.RetryHandler, its onResponse will iterate through
      // responses and finishHim will invoke onResponse on the listener given to it from
      // org.elasticsearch.action.bulk.BulkRequestHandler.execute (see retry.withBackoff)
      // It then invokes bulkProcessorListener.afterBulk
      BulkItemResponse itemResponse = mock(BulkItemResponse.class);
      when(itemResponse.isFailed()).thenReturn(false);
      BulkItemResponse[] responses = { itemResponse };
      // ideally there should be 'triggerBulkAfter' responses but we don't have to evaluate them in
      // test so lesser is ok.
      BulkResponse response = new BulkResponse(responses, 100);
      
      listener.onResponse(response);
      return null;
    }).when(client).bulkAsync(any(BulkRequest.class), eq(RequestOptions.DEFAULT),
        any(ActionListener.class));
    
    BulkProcessor bulkProcessor = BulkProcessor.builder(
        (request, bulkListener) ->
            client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener), bulkProcessorListener)
        .setBulkActions(triggerBulkAfter)
        .setConcurrentRequests(0) // we're not doing async bulk operation here being mocked
        .setBackoffPolicy(BackoffPolicy.noBackoff())
        .build();
  
    APICoreProperties apiCoreProperties = new APICoreProperties();
    APICoreProperties.Esdb esdb = new APICoreProperties.Esdb();
    esdb.setShotMetadataIndex("shotMetadata");
    
    EsdbShotMetadataProvider shotMetadataProvider = new EsdbShotMetadataProvider(apiCoreProperties,
        bulkProcessor);
    for (int i = 0; i < triggerBulkAfter; i++) {
      shotMetadataProvider.saveAsync(getShotMetadata());
      // once last request is added, bulk will invoke BulkRequestHandler.execute
    }
    
    // we can check without wait as bulk is executed on the same thread, i.e synchronously.
    BulkRequest successRequest = bulkProcessorListener.getSuccessRequest();
    BulkResponse response = bulkProcessorListener.getResponse();
    assertNotNull(successRequest);
    assertNotNull(response);
    
    assertFalse(response.hasFailures());
    
    assertEquals(triggerBulkAfter, successRequest.numberOfActions());
    
    assertNull(bulkProcessorListener.getFailureRequest());
    
    // verify that closeAndBlock successfully flushes pending requests by adding one request.
  
    shotMetadataProvider.saveAsync(getShotMetadata());
  
    shotMetadataProvider.processRemainingAndTearDown();
    
    successRequest = bulkProcessorListener.getSuccessRequest();
    
    assertEquals(1, successRequest.numberOfActions());
  }
  
  private ShotMetadata getShotMetadata() {
    String sessionKey = UUID.randomUUID().toString();
    return new ShotMetadata()
        .setShotName(
            new ShotNameProvider(UUID.randomUUID().toString(), "36xgdgdsAD", "png").getName("1"))
        .setBuildId(1)
        .setTestVersionId(1)
        .setBuildKey("36xgdgdsAD")
        .setSessionKey(sessionKey)
        .setAtLineZwl(1)
        .setCreateDate(OffsetDateTime.now());
  }
  
  private static class Listener implements BulkProcessor.Listener {
    
    private BulkRequest successRequest;
    private BulkResponse response;
    private BulkRequest failureRequest;
    
    @Override
    public void beforeBulk(long executionId, BulkRequest request) {
      // ignored
    }
    
    @Override
    public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
      LOG.debug("invoked afterBulk!!");
      successRequest = request;
      this.response = response;
    }
    
    @Override
    public void afterBulk(long executionId, BulkRequest request, Throwable t) {
      failureRequest = request;
    }
    
    BulkRequest getSuccessRequest() {
      return successRequest;
    }
    
    BulkRequest getFailureRequest() {
      return failureRequest;
    }
    
    BulkResponse getResponse() {
      return response;
    }
  }
}
