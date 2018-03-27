package com.intrence.core.persistence.postgres;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents credentials for a user in postgres.
 */
public class PostgresCredential {
    private final String pass;
    private final String user;

    @JsonCreator
    public PostgresCredential(@JsonProperty("user") String user,
                              @JsonProperty("pass") String pass) {
        this.user = user;
        this.pass = pass;
    }

    public String getPass() {
        return pass;
    }

    public String getUser() {
        return user;
    }
}
