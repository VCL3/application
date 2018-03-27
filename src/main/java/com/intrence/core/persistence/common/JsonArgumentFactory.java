package com.intrence.core.persistence.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.sql.SQLException;

/**
 * Takes care of serializing any JDBI json args
 * Postgres specific functionality is abstracted here
 */
public class JsonArgumentFactory<T> implements ArgumentFactory<JsonObject<T>> {
    private static final String JSON_SQL_TYPE = "jsonb";
    private final ObjectMapper objectMapper;

    @Inject
    public JsonArgumentFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        return value instanceof JsonObject;
    }

    /**
     * Json must be wrapped in PGobject for PG to recognize it as json (and not string)
     */
    @Override
    public Argument build(Class<?> expectedType, JsonObject<T> value, StatementContext ctx) {
        return (position, statement, ctx1) -> {
            try {
                PGobject pgObject = new PGobject();
                pgObject.setType(JSON_SQL_TYPE);
                pgObject.setValue(objectMapper.writeValueAsString(value.get()));
                statement.setObject(position, pgObject);
            } catch (JsonProcessingException e) {
                throw new SQLException("Error serializing JSON value to String", e);
            }
        };
    }
}
