package com.intrence.core.persistence.common;

// Wrapper class to denote object should be stored as JSON
public class JsonObject<T> {
    private final T object;

    public JsonObject(T object) {
        this.object = object;
    }

    public T get() {
        return object;
    }
}
