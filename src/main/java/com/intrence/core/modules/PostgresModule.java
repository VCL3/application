/**
 * Created by wliu on 11/30/17.
 */
package com.intrence.core.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.intrence.core.persistence.common.CollectionArgumentFactory;
import com.intrence.core.persistence.common.JsonArgumentFactory;
import com.intrence.core.persistence.dao.ProductDao;
import com.intrence.core.persistence.jdbi.JDBI;
import com.intrence.core.persistence.mapper.ProductMapper;
import com.intrence.core.persistence.postgres.PostgresConfig;
import com.intrence.core.persistence.postgres.PostgresConfigProvider;
import io.dropwizard.jdbi.args.JodaDateTimeArgumentFactory;
import io.dropwizard.jdbi.args.JodaDateTimeMapper;
import org.skife.jdbi.v2.DBI;

import javax.sql.DataSource;

public class PostgresModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PostgresConfig.class).toProvider(PostgresConfigProvider.class);
        bind(ProductMapper.class).asEagerSingleton();
    }

    @Singleton
    @Provides
    public ProductDao providesProductDao(PostgresConfig postgresConfig,
                                         JsonArgumentFactory jsonArgumentFactory,
                                         CollectionArgumentFactory collectionArgumentFactory,
                                         JodaDateTimeArgumentFactory jodaDateTimeArgumentFactory,
                                         JodaDateTimeMapper jodaDateTimeMapper,
                                         ProductMapper productMapper) {
        return buildProductDao(postgresConfig.buildTransactionPooledDataSource(), jsonArgumentFactory, collectionArgumentFactory, jodaDateTimeArgumentFactory, jodaDateTimeMapper, productMapper);
    }

    private ProductDao buildProductDao(DataSource dataSource,
                                       JsonArgumentFactory jsonArgumentFactory,
                                       CollectionArgumentFactory collectionArgumentFactory,
                                       JodaDateTimeArgumentFactory jodaDateTimeArgumentFactory,
                                       JodaDateTimeMapper jodaDateTimeMapper,
                                       ProductMapper productMapper) {
        DBI dbi = JDBI.build(dataSource, null);
        dbi.registerArgumentFactory(jsonArgumentFactory);
        dbi.registerArgumentFactory(collectionArgumentFactory);
        dbi.registerArgumentFactory(jodaDateTimeArgumentFactory);
        dbi.registerColumnMapper(jodaDateTimeMapper);
        dbi.registerMapper(productMapper);
        return dbi.onDemand(ProductDao.class);
    }

}
