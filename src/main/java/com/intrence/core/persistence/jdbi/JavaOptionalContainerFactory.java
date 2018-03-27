package com.intrence.core.persistence.jdbi;

import org.skife.jdbi.v2.ContainerBuilder;
import org.skife.jdbi.v2.tweak.ContainerFactory;

import java.util.Optional;

class JavaOptionalContainerFactory implements ContainerFactory<Optional<?>> {

    @Override
    public boolean accepts(Class<?> type) {
        return Optional.class.isAssignableFrom(type);
    }

    @Override
    public ContainerBuilder<Optional<?>> newContainerBuilderFor(Class<?> type) {
        return new OptionalContainerBuilder();
    }

    private static class OptionalContainerBuilder implements ContainerBuilder<Optional<?>> {

        private Optional<?> optional = Optional.empty();

        @Override
        public ContainerBuilder<Optional<?>> add(Object it) {
            optional = Optional.ofNullable(it);
            return this;
        }

        @Override
        public Optional<?> build() {
            return optional;
        }
    }
}
