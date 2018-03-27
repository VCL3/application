package com.intrence.core.persistence.jdbi;

import com.codahale.metrics.MetricRegistry;
//import com.codahale.metrics.jdbi.InstrumentedTimingCollector;
import org.skife.jdbi.v2.ColonPrefixNamedParamStatementRewriter;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.logging.SLF4JLog;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JDBI {

    /**
     * Build a DBI instance which pulls connections from `ds` and reports metrics to `metrics`.
     *
     * It will log SQL to `com.groupon.jtier.jdbi.JDBI` logger at the DEBUG level.
     *
     * It registers argument factories and container factories for a number of commonly used
     * things, such as Guava's optional, Java's optional, java.time.Instant (to timestamp), and
     * Guava's immutable set and list.
     */
    public static DBI build(DataSource ds, MetricRegistry metrics) {
        DBI dbi = new DBI(ds);
//        dbi.setTimingCollector(new InstrumentedTimingCollector(metrics));
        dbi.setSQLLog(new SLF4JLog(LoggerFactory.getLogger(JDBI.class), SLF4JLog.Level.DEBUG));
        dbi.setStatementRewriter(new NamePrependingStatementRewriter(new ColonPrefixNamedParamStatementRewriter()));

        dbi.registerArgumentFactory(new GuavaOptionalArgumentFactory());
        dbi.registerArgumentFactory(new JavaOptionalArgumentFactory());
        dbi.registerArgumentFactory(new InstantArgumentFactory());

        dbi.registerContainerFactory(new GuavaOptionalContainerFactory());
        dbi.registerContainerFactory(new JavaOptionalContainerFactory());
        dbi.registerContainerFactory(new ImmutableListContainerFactory());
        dbi.registerContainerFactory(new ImmutableSetContainerFactory());
        return dbi;
    }
}
