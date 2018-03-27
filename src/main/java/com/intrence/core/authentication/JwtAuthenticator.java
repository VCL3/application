package com.intrence.core.authentication;

import com.intrence.core.persistence.dao.UserDao;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.JwtContext;

import java.util.Optional;
import java.util.UUID;

public class JwtAuthenticator implements Authenticator<JwtContext, User> {

    private final UserDao userDao;

    public JwtAuthenticator(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Optional<User> authenticate(JwtContext jwtContext) throws AuthenticationException {
        try {
            String userId = jwtContext.getJwtClaims().getSubject();
            NumericDate expiration = jwtContext.getJwtClaims().getExpirationTime();
            // Check expiration time
            User user = this.userDao.getUserById(UUID.fromString(userId));
            if (user != null) {
                return Optional.of(user);
            } else {
                return Optional.empty();
            }
        } catch (MalformedClaimException mce) {
            return Optional.empty();
        }
    }

}
