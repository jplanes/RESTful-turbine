package com.github.jplanes.restful.turbine.stores;

import com.netflix.config.DynamicPropertyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Properties;

import static java.lang.String.format;

@Component
public class PropertiesDataSource {
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

    public String getClusters() {
        return this.get("turbine.aggregator.clusterConfig");
    }

    public String getInstancesInCluster(String cluster) {
        return this.get(format("turbine.ConfigPropertyBasedDiscovery.%s.instances", cluster));
    }

    public void set(String property, String value) {
        try {
            File file = new File(fileName);
            OutputStream output = new FileOutputStream(file);

            this.properties.setProperty(property, value);
            properties.store(output, null);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setClusters(String clusters) {
        this.set("turbine.aggregator.clusterConfig", clusters);
    }

    public void setInstancesInCluster(String cluster, String instances) {
        String key = format("turbine.ConfigPropertyBasedDiscovery.%s.instances", cluster);

        if(instances == null || instances.trim().equals("")) {
            this.properties.remove(key);
        } else {
            this.set(key, instances);
        }
    }

}
