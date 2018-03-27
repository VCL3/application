package com.intrence.core.persistence.jdbi;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

class InstantArgumentFactory implements ArgumentFactory<Instant> {

    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        return value instanceof Instant;
    }

    @Override
    public Argument build(Class<?> expectedType, Instant value, StatementContext ctx) {
        return new InstantArgument(value);
    }

    static class InstantArgument implements Argument {

        private final Instant value;

        public InstantArgument(Instant value) {
            this.value = value;
        }

        @Override
        public void apply(int position, PreparedStatement stmt, StatementContext ctx) throws SQLException {
            Timestamp ts = Timestamp.from(value);
            stmt.setTimestamp(position, ts);
        }
    }
}
