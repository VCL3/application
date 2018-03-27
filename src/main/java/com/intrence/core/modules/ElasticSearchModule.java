/**
 * Created by wliu on 12/1/17.
 */
package com.intrence.core.modules;

import com.google.inject.AbstractModule;
import com.intrence.core.elasticsearch.ElasticSearchConfiguration;
import com.intrence.core.elasticsearch.ElasticSearchService;
import io.dropwizard.setup.Environment;

public class ElasticSearchModule extends AbstractModule {

    private final ElasticSearchConfiguration conf;
    private final Environment env;

    public ElasticSearchModule(ElasticSearchConfiguration conf, Environment env) {
        this.conf = conf;
        this.env = env;
    }

    @Override
    protected void configure() {
        bind(ElasticSearchConfiguration.class).toInstance(conf);
        bind(ElasticSearchService.class).asEagerSingleton();
    }
}
