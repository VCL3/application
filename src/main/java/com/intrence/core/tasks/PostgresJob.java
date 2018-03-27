/**
 * Created by wliu on 11/30/17.
 */
package com.intrence.core.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.intrence.core.elasticsearch.ElasticSearchConfiguration;
import com.intrence.core.elasticsearch.ElasticSearchService;
import com.intrence.core.persistence.dao.ProductDao;
import com.intrence.core.modules.PostgresModule;
import com.intrence.core.persistence.postgres.PostgresConfig;
import com.intrence.models.model.Price;
import com.intrence.models.model.Product;
import org.elasticsearch.action.get.GetResponse;

import java.util.UUID;

public class PostgresJob {

    public static void main(String[] args) throws Exception {


        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\n" +
                "    \"clusterName\": \"elasticsearch_wliu\",\n" +
                "    \"port\": 9300,\n" +
                "    \"nodesToConnect\": \"localhost\"\n" +
                "}";
        ElasticSearchConfiguration conf = objectMapper.readValue(json, ElasticSearchConfiguration.class);

        ElasticSearchService elasticSearchService = new ElasticSearchService(conf);

        GetResponse res = elasticSearchService.getDocument("product", "doc", "1");

        System.out.println(res.getSourceAsString());

    }

}
