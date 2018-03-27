package com.intrence.core.persistence.mapper;

import com.intrence.core.authentication.User;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserMapper implements ResultSetMapper<User> {

    public User map(int index, ResultSet results, StatementContext ctx) throws SQLException {
        return new User.Builder()
                .uuid(UUID.fromString(results.getString("uuid")))
                .email(results.getString("email"))
                .password(results.getString("password"))
                .username(results.getString("username"))
                .firstName(results.getString("firstname"))
                .lastName(results.getString("lastname"))
                .createdAt(mapDateTime("created_at", results, ctx))
                .updatedAt(mapDateTime("updated_at", results, ctx))
                .build();
    }

    private DateTime mapDateTime(String column, ResultSet resultSet, StatementContext ctx) throws SQLException {
        return (DateTime) ctx.columnMapperFor(DateTime.class).mapColumn(resultSet, column, ctx);
    }

}
