package com.intrence.core.elasticsearch;

import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.support.replication.ReplicationRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.List;

public class ElasticSearchService implements Managed {

    private static Logger LOGGER = LoggerFactory.getLogger(ElasticSearchService.class);

    private Client elasticsearchClient;

    @Inject
    public ElasticSearchService(ElasticSearchConfiguration elasticSearchConfiguration) {

        String clusterName = elasticSearchConfiguration.getClusterName();
        Integer discoveryPort = elasticSearchConfiguration.getPort();
        String nodesToConnect = elasticSearchConfiguration.getNodesToConnect();

        Settings settings = Settings.builder()
                .put("cluster.name", clusterName)
//                .put("discovery.zen.ping.multicast.port", discoveryPort)
                .build();

        try {
            elasticsearchClient = new PreBuiltTransportClient(settings).addTransportAddress(new TransportAddress(InetAddress.getByName(nodesToConnect), 9300));
        } catch (Exception e) {
            throw new RuntimeException("ElasticSearchException: Failed to connect to elasticsearch node(s): " + nodesToConnect + ", " + e);
        }
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {
        elasticsearchClient.close();
    }

    public String[] getAllIndices() {
       return elasticsearchClient.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().getConcreteAllIndices();
    }

    /**
     * Creates the given index when it doesn't exists.
     *
     * @param index name of the index to be created if it does not exists already
     * @param settings Json String - settings for the index
     * @param type name of the mapping type
     * @param mapping Json string - mapping for type
     */
    public void ensureIndexExists(String index, String settings , String type, String mapping) {
        if (!checkIndexExists(index)) {
            CreateIndexRequestBuilder createIndexRequestBuilder = elasticsearchClient.admin().indices()
                    .prepareCreate(index).setSettings(settings);
            if (mapping != null && type != null) {
                createIndexRequestBuilder.addMapping(type,mapping);
            }
            createIndexRequestBuilder.execute().actionGet();
        }
    }

    public boolean deleteIndex(String indexName) {
        if(checkIndexExists(indexName)) {
            elasticsearchClient.admin().indices().delete(new DeleteIndexRequest(indexName)).actionGet();
            return true;
        } else {
            return false;
        }
    }

    public boolean checkIndexExists(String index) {
        return elasticsearchClient.admin().indices().prepareExists(index).execute().actionGet().isExists();
    }


    public GetResponse getDocument(String index, String documentType, String id) {
        LOGGER.debug(String.format("Get Document index=%s, id=%s", index, id));
        return executeESRequest(buildGetRequest(index, documentType, id));
    }

    public IndexResponse upsertDocument(String index, String documentType, String id, String body, boolean refreshIndex) {
        LOGGER.debug(String.format("Upsert Document %s, index = %s, id = %s, refreshIndex=%s", body, index, id, refreshIndex));
        return executeESRequest(buildIndexRequest(index, documentType, id, body, refreshIndex));
    }

    public SearchHits searchDocuments(String index, String documentType, QueryBuilder query, SortBuilder sortBy,
                                      int offset, int limit) {
        return searchDocuments(index, documentType, query, sortBy, offset, limit, null, null, true);
    }

    public SearchHits searchDocuments(String index, String documentType, QueryBuilder query, SortBuilder sortBy,
                                      int offset, int limit, boolean fetchSource) {
        return searchDocuments(index, documentType, query, sortBy, offset, limit, null, null, fetchSource);
    }

    public SearchHits searchDocuments(String index, String documentType, QueryBuilder query, SortBuilder sortBy,
                                      int offset, int limit, String[] fieldsToInclude, String[] fieldsToExclude,
                                      boolean fetchSource) {
        SearchRequestBuilder searchBuilder = elasticsearchClient.prepareSearch(index);
        searchBuilder.setTypes(documentType);
        searchBuilder.setQuery(query);
        searchBuilder.setFetchSource(fetchSource);
        searchBuilder.setFetchSource(fieldsToInclude, fieldsToExclude);

        if(sortBy != null) {
            searchBuilder.addSort(sortBy);
        }
        searchBuilder.setFrom(offset);
        if(limit > 0) {
            searchBuilder.setSize(limit);
        }

        LOGGER.debug(String.format("Searching for documents in index = %s, type = %s", index, documentType));

        try {
            return searchBuilder.execute().actionGet().getHits();
        } catch (Exception e) {
            throw new RuntimeException("ElasticSearchException: could not find documents.", e);
        }

    }

    public boolean deleteDocument(String index, String documentType, String id) {
        DeleteResponse deleteResponse = executeESRequest(buildDeleteRequest(index, documentType, id));
        return deleteResponse.isFragment();
    }

    public BulkResponse doBulkProcessing(BulkRequestBuilder bulkRequestBuilder) {
        return executeESRequest(bulkRequestBuilder);
    }

    private <T extends ActionResponse> T executeESRequest(ActionRequestBuilder<?, T, ?> requestBuilder) {

        try {
            T response = requestBuilder.execute().actionGet();
            return response;
        } catch (Exception e) {
            throw new RuntimeException("ElasticSearchException: ",e);
        }
    }

    public DeleteRequestBuilder buildDeleteRequest(String index, String documentType, String id) {
        return elasticsearchClient.prepareDelete(index, documentType, id);
    }

    public GetRequestBuilder buildGetRequest(String index, String documentType, String id) {
        return elasticsearchClient.prepareGet(index, documentType, id);
    }

    public BulkRequestBuilder  buildBulkRequest(List<ReplicationRequestBuilder<?,?,?>> requestBuilders) {
        BulkRequestBuilder bulkRequest = elasticsearchClient.prepareBulk();
        for(ReplicationRequestBuilder<?,?,?> requestBuilder : requestBuilders) {
            if (requestBuilder instanceof IndexRequestBuilder) {
                bulkRequest.add((IndexRequestBuilder) requestBuilder);
            } else if (requestBuilder instanceof DeleteRequestBuilder) {
                bulkRequest.add((DeleteRequestBuilder) requestBuilder);
            } else {
                throw new RuntimeException("ElasticSearchException: Bulk API would only support Indexing and Delete requests");
            }
        }

        return bulkRequest;
    }

    public IndexRequestBuilder buildIndexRequest(String index, String documentType, String id, String body, boolean refreshIndex) {
        if (StringUtils.isBlank(body)) {
            throw new IllegalArgumentException("ElasticSearchException: Document body required");
        }
        IndexRequestBuilder indexRequestBuilder = elasticsearchClient.prepareIndex(index, documentType, id);
        indexRequestBuilder.setSource(body);
//        indexRequestBuilder.setRefresh(refreshIndex);
        return indexRequestBuilder;
    }

}
