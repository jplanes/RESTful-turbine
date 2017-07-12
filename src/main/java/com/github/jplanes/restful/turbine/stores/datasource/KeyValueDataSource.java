package com.github.jplanes.restful.turbine.stores.datasource;

public interface KeyValueDataSource {
    String get(String key);
    void set(String key, String value);
}
