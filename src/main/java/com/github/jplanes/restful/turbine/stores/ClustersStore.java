package com.github.jplanes.restful.turbine.stores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;

@Component
public class ClustersStore {

    private PropertiesDataSource properties;

    @Autowired
    public ClustersStore(PropertiesDataSource properties) {
        this.properties = properties;
    }

    public Collection<String> findAll() {
        return
                Optional.ofNullable(this.properties.getClusters())
                .map(propertyValue -> newArrayList(propertyValue.trim().split(",")))
                .orElse(newArrayList());
    }

    public void save(String cluster) {
        Collection<String> clusters = this.findAll();
        if(!clusters.contains(cluster)) {
            clusters.add(cluster);

            String clustersAsString = clusters.stream().collect(joining(","));
            this.properties.setClusters(clustersAsString);
        }
    }

}
