package com.intrence.core.persistence.postgres;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Configuration class designed to be deserialized from YAML or JSON by Jackson's ObjectMapper. Used to build DataSource
 * instances against DaaS PostgreSQL instances. <p> This is should be the primary way make use of DaaS PostgreSQL
 * databases.
 */
@JsonDeserialize(builder = PostgresConfig.Builder.class)
public class PostgresConfig {

    private static final Logger log = LoggerFactory.getLogger(PostgresConfig.class);
    private static final AtomicInteger sessionPoolCounter = new AtomicInteger(0);
    private static final AtomicInteger transactionPoolCounter = new AtomicInteger(0);

    private static final String MIN_POSTGRES_VERSION = "9.4.6";
    public static final String POSTGRES_STRING = "postgres";
    public static final String APP_STRING = "app";
    public static final String DBA_STRING = "dba";
    public static final String DATATBASE_STRING = "database";
    public static final String HOST_STRING = "host";
    public static final String CONNECTION_PROPERTIES_STRING = "properties";
    public static final String ADMIN_PORT_STRING = "adminPort";
    public static final String SESSION_POOL_SIZE_STRING = "sessionPoolSize";
    public static final String SESSION_PORT_STRING = "sessionPort";
    public static final String TRANSACTION_POOL_SIZE_STRING = "transactionPoolSize";
    public static final String TRANSACTION_PORT_STRING = "transactionPort";


    private Optional<PostgresCredential> app;
    private Optional<PostgresCredential> dba;
    private String database;
    private String host;
    private Map<String, String> connectionProperties = Maps.newConcurrentMap();
    private String sslMode;
    private volatile int adminPort = 15432;
    private volatile int sessionPoolSize = 2;
    private volatile int sessionPort = 6432;
    private volatile int transactionPoolSize = 50;
    private volatile int transactionPort = 5432;
    private DataSource adminDataSource;
    private DataSource sessionDataSource;
    private DataSource txDataSource;

    public PostgresConfig(final String host,
                          final String database,
                          final PostgresCredential app,
                          final PostgresCredential dba,
                          final String sslMode,
                          final Map<String, String> properties) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(database), "`database` must be set");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(host), "`host` must be set");
        Preconditions.checkArgument((app != null) || (dba != null), "one of `dba` or `app` must be set");

        if (properties != null) {
            this.connectionProperties.putAll(properties);
        }

        this.host = host;
        this.database = database;
        this.app = Optional.ofNullable(app);
        this.dba = Optional.ofNullable(dba);
        this.sslMode = sslMode;
    }

    private PostgresConfig(Builder builder) {
        this.app = builder.app;
        this.dba = builder.dba;
        this.database = builder.database;
        this.host = builder.host;
        this.connectionProperties = builder.connectionProperties;
        this.sslMode = builder.sslMode;
        this.adminPort = builder.adminPort;
        this.sessionPoolSize = builder.sessionPoolSize;
        this.sessionPort = builder.sessionPort;
        this.transactionPoolSize = builder.transactionPoolSize;
        this.transactionPort = builder.transactionPort;
    }

    /**
     * Creates an un-pooled DataSource with the dba user. This data source should only be used for DDL and such, not
     * application work!
     */
    public synchronized DataSource buildAdminDataSource() {
        if (this.adminDataSource != null) {
            return this.adminDataSource;
        }
        Preconditions.checkState(this.dba.isPresent(), "need dba credentials to create admin datasource");

        final PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setPrepareThreshold(0);
        ds.setServerName(this.host);
        ds.setDatabaseName(this.database);
        ds.setUser(this.dba.get().getUser());
        ds.setPassword(this.dba.get().getPass());
        ds.setPortNumber(this.adminPort);
        ds.setAssumeMinServerVersion(MIN_POSTGRES_VERSION);
        configureSslMode(ds);
        addConnectionProperties(ds);
        this.adminDataSource = ds;
        return this.adminDataSource;
    }

    /**
     * Creates a pooled DataSource which communicates with pgbouncer set to session mode. In this model a connection
     * held by this pool will hold a connection all the way through pgbouncer to the underlying database. The pgbouncer
     * connection to the database will be reserved to the connection on this pool for its lifetime.
     * <p>
     * The configuration value, `sessionPoolSize` controls how large the created pool will be. Be careful to size it
     * appropriately to allow all the various pools establishing connections to be able to create their pools.
     */
    public DataSource buildSessionPooledDataSource() {
        return buildSessionPooledDataSource(Optional.empty(), Optional.empty());
    }

    /**
     * Creates a pooled DataSource which communicates with pgbouncer set to session mode. In this model a connection
     * held by this pool will hold a connection all the way through pgbouncer to the underlying database. The pgbouncer
     * connection to the database will be reserved to the connection on this pool for its lifetime.
     * <p>
     * The configuration value, `sessionPoolSize` controls how large the created pool will be. Be careful to size it
     * appropriately to allow all the various pools establishing connections to be able to create their pools.
     */
    public DataSource buildSessionPooledDataSource(final MetricRegistry metrics, final HealthCheckRegistry health) {
        return buildSessionPooledDataSource(Optional.of(metrics), Optional.of(health));
    }

    /**
     * Creates a pooled DataSource which communicates with pgbouncer set to session mode. In this model a connection
     * held by this pool will hold a connection all the way through pgbouncer to the underlying database. The pgbouncer
     * connection to the database will be reserved to the connection on this pool for its lifetime. <p> The
     * configuration value, `sessionPoolSize` controls how large the created pool will be. Be careful to size it
     * appropriately to allow all the various pools establishing connections to be able to create their pools.
     */
    public synchronized DataSource buildSessionPooledDataSource(final Optional<MetricRegistry> metrics,
                                                                final Optional<HealthCheckRegistry> health) {
        return buildSessionPooledDataSource(metrics, health, Optional.empty());
    }

    /**
     * Creates a pooled DataSource which communicates with pgbouncer set to session mode. In this model a connection
     * held by this pool will hold a connection all the way through pgbouncer to the underlying database. The pgbouncer
     * connection to the database will be reserved to the connection on this pool for its lifetime. <p> The
     * configuration value, `sessionPoolSize` controls how large the created pool will be. Be careful to size it
     * appropriately to allow all the various pools establishing connections to be able to create their pools.
     * <p>
     * If {@code lifecycle} is present, the pool will be shut down when the server shuts down.
     */
    public synchronized DataSource buildSessionPooledDataSource(final Optional<MetricRegistry> metrics,
                                                                final Optional<HealthCheckRegistry> health,
                                                                final Optional<LifecycleEnvironment> lifecycle) {
        if (this.sessionDataSource != null) {
            return this.sessionDataSource;
        }

        Preconditions.checkState(this.app.isPresent(), "App user credentials not available");

        final PGSimpleDataSource unpooled = new PGSimpleDataSource();
        unpooled.setPrepareThreshold(0);
        unpooled.setServerName(this.host);
        unpooled.setDatabaseName(this.database);
        unpooled.setUser(this.app.get().getUser());
        unpooled.setPassword(this.app.get().getPass());
        unpooled.setPortNumber(this.sessionPort);
        unpooled.setAssumeMinServerVersion(MIN_POSTGRES_VERSION);
        configureSslMode(unpooled);
        addConnectionProperties(unpooled);

        final HikariConfig config = new HikariConfig();
        config.setDataSource(unpooled);
        config.setMaximumPoolSize(this.sessionPoolSize);
        config.setPoolName("postgres-session-pool-" + sessionPoolCounter.getAndIncrement());

        metrics.ifPresent(config::setMetricRegistry);
        health.ifPresent(config::setHealthCheckRegistry);

        log.info("built datasource {}", config.getPoolName());
        HikariDataSource hikariDataSource = new HikariDataSource(config);
        lifecycle.ifPresent(lifecycleEnvironment -> this.registerDataSourceInEnvironment(hikariDataSource, lifecycleEnvironment));
        this.sessionDataSource = hikariDataSource;
        return this.sessionDataSource;
    }

    /**
     * Creates a pooled DataSource which communicates with pgbouncer set to transaction mode. In this model there is
     * this connection pool which connects to pgbouncer, which maintains another connection pool. Connections are
     * obtained in pgbouncer for the duration of a transaction, then returned to the pool in pgbouncer.
     * <p>
     * The configuration value, `transactionPoolSize` controls how large the created pool will be.
     */
    public DataSource buildTransactionPooledDataSource(final MetricRegistry metrics, final HealthCheckRegistry health) {
        return buildTransactionPooledDataSource(Optional.of(metrics), Optional.of(health));
    }

    /**
     * Creates a pooled DataSource which communicates with pgbouncer set to transaction mode. In this model there is
     * this connection pool which connects to pgbouncer, which maintains another connection pool. Connections are
     * obtained in pgbouncer for the duration of a transaction, then returned to the pool in pgbouncer.
     * <p>
     * The configuration value, `transactionPoolSize` controls how large the created pool will be.
     */
    public DataSource buildTransactionPooledDataSource() {
        return buildTransactionPooledDataSource(Optional.empty(), Optional.empty());
    }

    /**
     * Creates a pooled DataSource which communicates with pgbouncer set to transaction mode. In this model there is
     * this connection pool which connects to pgbouncer, which maintains another connection pool. Connections are
     * obtained in pgbouncer for the duration of a transaction, then returned to the pool in pgbouncer.
     * <p>
     * The configuration value, `transactionPoolSize` controls how large the created pool will be.
     */
    public synchronized DataSource buildTransactionPooledDataSource(final Optional<MetricRegistry> metrics,
                                                                    final Optional<HealthCheckRegistry> health) {
        return buildTransactionPooledDataSource(metrics, health, Optional.empty());
    }

    /**
     * Creates a pooled DataSource which communicates with pgbouncer set to transaction mode. In this model there is
     * this connection pool which connects to pgbouncer, which maintains another connection pool. Connections are
     * obtained in pgbouncer for the duration of a transaction, then returned to the pool in pgbouncer.
     * <p>
     * The configuration value, `transactionPoolSize` controls how large the created pool will be.
     * <p>
     * If {@code lifecycle} is present, the pool will be shut down when the server shuts down.
     */
    public synchronized DataSource buildTransactionPooledDataSource(final Optional<MetricRegistry> metrics,
                                                                    final Optional<HealthCheckRegistry> health,
                                                                    final Optional<LifecycleEnvironment> lifecycle) {
        if (this.txDataSource != null) {
            return this.txDataSource;
        }
        Preconditions.checkState(this.app.isPresent(), "need app credentials to create transaction pooled datasource");

        final PGSimpleDataSource unpooled = new PGSimpleDataSource();
        unpooled.setPrepareThreshold(0);
        unpooled.setServerName(this.host);
        unpooled.setDatabaseName(this.database);
        unpooled.setUser(this.app.get().getUser());
        unpooled.setPassword(this.app.get().getPass());
        unpooled.setPortNumber(this.transactionPort);
        unpooled.setAssumeMinServerVersion(MIN_POSTGRES_VERSION);
        configureSslMode(unpooled);
        addConnectionProperties(unpooled);

        final HikariConfig config = new HikariConfig();
        config.setDataSource(unpooled);
        config.setMaximumPoolSize(this.transactionPoolSize);
        config.setPoolName("postgres-transaction-pool-" + transactionPoolCounter.getAndIncrement());

        metrics.ifPresent(config::setMetricRegistry);
        health.ifPresent(config::setHealthCheckRegistry);

        log.info("built datasource {}", config.getPoolName());
        HikariDataSource hikariDataSource = new HikariDataSource(config);
        lifecycle.ifPresent(lifecycleEnvironment -> this.registerDataSourceInEnvironment(hikariDataSource, lifecycleEnvironment));
        this.txDataSource = hikariDataSource;
        return this.txDataSource;
    }

    public void setAdminPort(final int adminPort) {
        this.adminPort = adminPort;
    }

    public void setSessionPoolSize(final int sessionPoolSize) {
        this.sessionPoolSize = sessionPoolSize;
    }

    public void setSessionPort(final int sessionPort) {
        this.sessionPort = sessionPort;
    }

    public void setTransactionPoolSize(final int transactionPoolSize) {
        this.transactionPoolSize = transactionPoolSize;
    }

    public void setTransactionPort(final int transactionPort) {
        this.transactionPort = transactionPort;
    }

    public String getSslMode() {
        return this.sslMode;
    }

    private void configureSslMode(final PGSimpleDataSource dataSource){
        if(this.sslMode != null && !this.sslMode.isEmpty()){
            try {
                dataSource.setProperty("sslMode", this.sslMode);
            }
            catch (final SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addConnectionProperties(final PGSimpleDataSource unpooled) {
        for (final Map.Entry<String, String> prop : this.connectionProperties.entrySet()) {
            try {
                unpooled.setProperty(prop.getKey(), prop.getValue());
            } catch (final SQLException e) {
                throw new IllegalStateException(String.format("error setting property %s", prop.getKey()), e);
            }
        }
    }

    public Map<String, String> getConnectionProperties() {
        return this.connectionProperties;
    }

    // TODO: when upgrading to dropwizard 1.2, please replace with io.dropwizard.lifecycle.AutoCloseableManager.
    private void registerDataSourceInEnvironment(HikariDataSource dataSource, LifecycleEnvironment environment) {
        environment.manage(new Managed() {
            @Override
            public void start() throws Exception {}

            @Override
            public void stop() throws Exception {
                dataSource.close();
            }
        });
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        private Optional<PostgresCredential> app;
        private Optional<PostgresCredential> dba;
        private String database;
        private String host;
        private Map<String, String> connectionProperties = Maps.newConcurrentMap();
        private String sslMode;
        volatile int adminPort = 15432;
        volatile int sessionPoolSize = 2;
        volatile int sessionPort = 6432;
        volatile int transactionPoolSize = 50;
        volatile int transactionPort = 5432;

        public Builder() {
        }

        @JsonSetter
        public Builder app(Optional<PostgresCredential> app) {
            this.app = app;
            return this;
        }

        @JsonSetter
        public Builder dba(Optional<PostgresCredential> dba) {
            this.dba = dba;
            return this;
        }

        @JsonSetter
        public Builder database(String database) {
            this.database = database;
            return this;
        }

        @JsonSetter
        public Builder host(String host) {
            this.host = host;
            return this;
        }

        @JsonSetter
        public Builder connectionProperties(Map<String, String> connectionProperties) {
            this.connectionProperties = connectionProperties;
            return this;
        }

        @JsonSetter
        public Builder sslMode(String sslMode) {
            this.sslMode = sslMode;
            return this;
        }

        @JsonSetter
        public Builder adminPort(int adminPort) {
            this.adminPort = adminPort;
            return this;
        }

        @JsonSetter
        public Builder sessionPoolSize(int sessionPoolSize) {
            this.sessionPoolSize = sessionPoolSize;
            return this;
        }

        @JsonSetter
        public Builder sessionPort(int sessionPort) {
            this.sessionPort = sessionPort;
            return this;
        }

        @JsonSetter
        public Builder transactionPoolSize(int transactionPoolSize) {
            this.transactionPoolSize = transactionPoolSize;
            return this;
        }

        @JsonSetter
        public Builder transactionPort(int transactionPort) {
            this.transactionPort = transactionPort;
            return this;
        }

        public PostgresConfig build() {
            return new PostgresConfig(this);
        }

    }
}
