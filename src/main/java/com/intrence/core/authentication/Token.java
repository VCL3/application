package com.intrence.core.authentication;

import org.joda.time.DateTime;

import java.util.UUID;

public class Token {

    private UUID userId;
    private String authMethod;
    private DateTime authenticatedAt;

}
