package com.github.jplanes.restful.turbine.stores.datasource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Properties;

import static java.lang.String.format;

@Component
public class PropertiesDataSource implements KeyValueDataSource {
    private String fileName;
    private Properties properties = new Properties();

    @Autowired
    public PropertiesDataSource(@Value("${propertiesDataSourceFileName}") String fileName) throws IOException {
        this.fileName = fileName;

        File file = new File(fileName);
        if(file.exists()){
            this.properties.load(new FileInputStream(file));
        }
    }

    public String get(String property) {
        return this.properties.getProperty(property);
    }

    public void set(String key, String value) {
        try {
            File file = new File(fileName);
            OutputStream output = new FileOutputStream(file);

            if(value == null || value.trim().equals("")) {
                this.properties.remove(key);
            } else {
                this.set(key, value);
            }
            properties.store(output, null);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
