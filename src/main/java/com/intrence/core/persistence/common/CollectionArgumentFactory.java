package com.intrence.core.persistence.common;

import org.apache.commons.lang3.StringUtils;
import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.util.Collection;

/**
 * Takes care of serializing JDBI collection/array args
 * Postgres specific functionality is abstracted here
 */
public class CollectionArgumentFactory implements ArgumentFactory<Collection<String>> {
    private static final String STRING_ARRAY_FORMAT = "varchar[]";

    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        return value instanceof Collection;
    }

    @Override
    public Argument build(Class<?> expectedType, Collection<String> value, StatementContext ctx) {
        return (position, statement, ctx1) -> {
            PGobject pgObject = new PGobject();
            pgObject.setType(STRING_ARRAY_FORMAT);
            // ugh postgres..coerce collection in string format PG requires
            pgObject.setValue(String.format("{%s}", StringUtils.join(value, ",")));
            statement.setObject(position, pgObject);
        };
    }
}
