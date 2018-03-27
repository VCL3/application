package com.intrence.core.persistence.dao;

import com.intrence.core.authentication.User;
import com.intrence.core.persistence.mapper.UserMapper;
import io.dropwizard.jdbi.args.JodaDateTimeMapper;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import java.util.UUID;

public interface UserDao {

    @SqlQuery("SELECT * FROM users WHERE uuid=:uuid")
    @Mapper(UserMapper.class)
    User getUserById(@Bind("uuid") UUID uuid);

    @SqlQuery("SELECT * FROM users WHERE email=:email")
    @Mapper(UserMapper.class)
    User getUserByEmail(@Bind("email") String email);

    @SqlUpdate("INSERT INTO users (uuid, email, password, username, firstname, lastname, created_at, updated_at) VALUES (:uuid, :email, :password, :username, :firstname, :lastname, :created_at, :updated_at)")
    void createUser(@Bind("uuid") UUID uuid, @Bind("email") String email, @Bind("password") String password, @Bind("username") String username, @Bind("firstname") String firstname, @Bind("lastname") String lastname, @Bind("created_at") DateTime createdAt, @Bind("updated_at") DateTime updatedAt);
}
