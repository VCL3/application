package com.intrence.core.authentication;

import com.intrence.core.persistence.dao.UserDao;

public class AuthenticationUtil {

    public static User retrieveUserWithCredentials(UserDao userDao, String username, String password) throws Exception {
        User user = userDao.getUserByEmail(username);
        if (user.getPassword().equals(password)) {
            return user;
        } else {
            return null;
        }
    }

}
