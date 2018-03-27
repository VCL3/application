/**
 * Created by wliu on 12/18/17.
 */
package com.intrence.core.elasticsearch;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class ElasticSearchConfiguration {

    @NotNull
    @JsonProperty
    String clusterName;

    @NotNull
    @JsonProperty
    Integer port;

    @JsonProperty
    String nodesToConnect;

    public String getClusterName() {
        return this.clusterName;
    }

    public Integer getPort() {
        return this.port;
    }

    public String getNodesToConnect() {
        return this.nodesToConnect;
    }

}
