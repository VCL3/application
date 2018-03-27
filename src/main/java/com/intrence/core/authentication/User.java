package com.intrence.core.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.joda.time.DateTime;

import java.security.Principal;
import java.util.UUID;

@JsonDeserialize(builder = User.Builder.class)
public class User implements Principal {

    private UUID uuid;
    private String email;
    private String password;
    private String username;
    private String firstName;
    private String lastName;
    private DateTime createdAt;
    private DateTime updatedAt;

    private User(Builder builder) {
        this.uuid = builder.uuid;
        this.email = builder.email;
        this.password = builder.password;
        this.username = builder.username;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    @JsonProperty
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getName() {
        return this.email;
    }

    @JsonProperty
    public String getEmail() {
        return email;
    }

    @JsonProperty
    public String getPassword() {
        return this.password;
    }

    @JsonProperty
    public String getUsername() {
        return this.username;
    }

    @JsonProperty
    public String getFirstName() {
        return this.firstName;
    }

    @JsonProperty
    public String getLastName() {
        return this.lastName;
    }

    @JsonProperty
    public DateTime getCreatedAt() {
        return this.createdAt;
    }

    @JsonProperty
    public DateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public static class Builder {

        private UUID uuid;
        private String email;
        private String password;
        private String username;
        private String firstName;
        private String lastName;
        private DateTime createdAt;
        private DateTime updatedAt;

        public Builder() {
        }

        public Builder(User user) {
            this.uuid = user.uuid;
            this.email = user.email;
            this.password = user.password;
            this.username = user.username;
            this.firstName = user.firstName;
            this.lastName = user.lastName;
            this.createdAt = user.createdAt;
            this.updatedAt = user.updatedAt;
        }

        @JsonSetter
        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        @JsonSetter
        public Builder email(String email) {
            this.email = email;
            return this;
        }

        @JsonSetter
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        @JsonSetter
        public Builder username(String username) {
            this.username = username;
            return this;
        }

        @JsonSetter
        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        @JsonSetter
        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        @JsonSetter
        public Builder createdAt(DateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @JsonSetter
        public Builder updatedAt(DateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }

}
