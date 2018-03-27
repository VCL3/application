package com.intrence.core.persistence.jdbi;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

class JavaOptionalArgumentFactory implements ArgumentFactory<Optional<Object>> {
    private static class DefaultOptionalArgument implements Argument {
        private final Optional<?> value;

        private DefaultOptionalArgument(Optional<?> value) {
            this.value = value;
        }

        @Override
        public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
            if (value.isPresent()) {
                statement.setObject(position, value.get());
            }
            else {
                statement.setNull(position, Types.OTHER);
            }
        }
    }

    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        return value instanceof Optional;
    }

    @Override
    public Argument build(Class<?> expectedType, Optional<Object> value, StatementContext ctx) {
        return new DefaultOptionalArgument(value);
    }
}
