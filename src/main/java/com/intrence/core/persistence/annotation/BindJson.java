package com.intrence.core.persistence.annotation;

import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;

import java.lang.annotation.*;
import java.sql.SQLException;

@BindingAnnotation(BindJson.JsonBinderFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface BindJson {
    String value();

    class JsonBinderFactory implements BinderFactory {
        @Override
        public Binder build(Annotation annotation) {
            return (Binder<BindJson, String>) (sqlStatement, bind, stringJson) -> {
                try {
                    PGobject data = new PGobject();
                    data.setType("jsonb");
                    data.setValue(stringJson);
                    sqlStatement.bind(bind.value(), data);
                } catch (SQLException e) {
                    throw new IllegalStateException("Could not execute query", e);
                }
            };
        }
    }
}