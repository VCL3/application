/**
 * Created by wliu on 12/18/17.
 */
package com.intrence.core.elasticsearch;

import org.elasticsearch.action.get.GetResponse;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ElasticSearchServiceTest {

    private static final ElasticSearchConfiguration conf = mock(ElasticSearchConfiguration.class);
    private static final ElasticSearchService elasticSearchService = new ElasticSearchService(conf);

//    @Test
    public void testConnection() throws Exception {
        when(conf.getClusterName()).thenReturn("elasticsearch_wliu");
        when(conf.getPort()).thenReturn(43331);
        when(conf.getNodesToConnect()).thenReturn("localhost");

        Assert.assertNotNull(elasticSearchService);
        String[] indices = elasticSearchService.getAllIndices();
        System.out.println(indices);

//        GetResponse res = elasticSearchService.getDocument("product", "doc", "1");
//        System.out.println(res);

    }

}
