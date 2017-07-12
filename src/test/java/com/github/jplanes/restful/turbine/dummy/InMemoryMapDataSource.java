package com.github.jplanes.restful.turbine.dummy;

import com.github.jplanes.restful.turbine.stores.datasource.KeyValueDataSource;

import java.util.HashMap;
import java.util.Map;

public class InMemoryMapDataSource implements KeyValueDataSource {

    private Map<String, String> map = new HashMap<>();

    @Override
    public String get(String key) {
        return this.map.get(key);
    }

    @Override
    public void set(String key, String value) {
        if(value == null || value.trim().equals("")) {
            this.map.remove(key);
        } else {
            this.map.put(key, value);
        }
    }
}
