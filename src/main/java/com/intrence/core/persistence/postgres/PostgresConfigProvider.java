/**
 * Created by wliu on 12/1/17.
 */
package com.intrence.core.persistence.postgres;

import com.google.inject.Provider;
import com.intrence.config.ConfigProvider;
import com.intrence.config.collection.ConfigMap;

import java.util.Optional;

public class PostgresConfigProvider implements Provider<PostgresConfig> {

    @Override
    public PostgresConfig get() {
        PostgresConfig.Builder builder = new PostgresConfig.Builder();

        ConfigMap configMap = ConfigProvider.getConfig();
        if (configMap.containsKey(PostgresConfig.POSTGRES_STRING)) {
            ConfigMap postgresConfigMap = (ConfigMap) configMap.get(PostgresConfig.POSTGRES_STRING);
            if (postgresConfigMap.containsKey(PostgresConfig.HOST_STRING)) {
                builder.host(postgresConfigMap.getString(PostgresConfig.HOST_STRING));
            } else {
                throw new IllegalArgumentException("no host specified");
            }
            if (postgresConfigMap.containsKey(PostgresConfig.DATATBASE_STRING)) {
                builder.database(postgresConfigMap.getString(PostgresConfig.DATATBASE_STRING));
            } else {
                throw new IllegalArgumentException("no host specified");
            }
            if (postgresConfigMap.containsKey(PostgresConfig.APP_STRING)) {
                ConfigMap appConfigMap = (ConfigMap) postgresConfigMap.get(PostgresConfig.APP_STRING);
                PostgresCredential appCredential = new PostgresCredential(appConfigMap.getString("user"), appConfigMap.getString("pass"));
                builder.app(Optional.of(appCredential));
            }
        }
        return builder.build();
    }

}
