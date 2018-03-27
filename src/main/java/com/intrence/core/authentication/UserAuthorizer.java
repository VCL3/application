package com.intrence.core.authentication;

import io.dropwizard.auth.Authorizer;

public class UserAuthorizer implements Authorizer<User> {

    @Override
    public boolean authorize(User user, String role) {
        return user.getName().equals("good-guy") && role.equals("ADMIN");
    }

}
