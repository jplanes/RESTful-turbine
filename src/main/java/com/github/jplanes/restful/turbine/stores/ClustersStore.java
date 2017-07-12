package com.github.jplanes.restful.turbine.stores;

import com.github.jplanes.restful.turbine.stores.datasource.KeyValueDataSource;
import com.github.jplanes.restful.turbine.stores.datasource.PropertiesDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;

@Component
public class ClustersStore {

    private KeyValueDataSource ds;

    @Autowired
    public ClustersStore(KeyValueDataSource ds) {
        this.ds = ds;
    }

    public Collection<String> findAll() {
        return
                Optional.ofNullable(this.ds.get("turbine.aggregator.clusterConfig"))
                .map(propertyValue -> newArrayList(propertyValue.trim().split(",")))
                .orElse(newArrayList());
    }

    public void save(String cluster) {
        Collection<String> clusters = this.findAll();
        if(!clusters.contains(cluster)) {
            clusters.add(cluster);

            String clustersAsString = clusters.stream().collect(joining(","));
            this.ds.set("turbine.aggregator.clusterConfig", clustersAsString);
        }
    }

}
