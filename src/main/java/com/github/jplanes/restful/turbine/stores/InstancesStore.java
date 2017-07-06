package com.github.jplanes.restful.turbine.stores;

import com.netflix.turbine.discovery.Instance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Component
public class InstancesStore {
    private ClustersStore clustersStore;
    private PropertiesDataSource properties;

    @Autowired
    public InstancesStore(ClustersStore clustersStore, PropertiesDataSource properties) {
        this.clustersStore = clustersStore;
        this.properties = properties;
    }

    public Collection<Instance> findAll() {
        return clustersStore.findAll().stream()
                            .flatMap(cluster -> this.findAll(cluster).stream())
                            .collect(toList());
    }

    public Collection<Instance> findAll(String cluster) {
        return
                java.util.Optional.ofNullable(this.properties.getInstancesInCluster(cluster))
                .map(propertyValue ->
                        newArrayList(propertyValue.trim().split(",")).stream()
                        .map(instanceName -> new Instance(instanceName, cluster, true))
                        .collect(toList())
                )
                .orElse(newArrayList());
    }

    public void save(Instance instance) {
        Collection<Instance> instances = this.findAll(instance.getCluster());
        if(instances.contains(instance)) return;

        instances.add(instance);
        String instancesAsString = instances.stream().map(Instance::getHostname).collect(joining(","));
        this.properties.setInstancesInCluster(instance.getCluster(), instancesAsString);
    }

    public void delete(Instance instance) {
        String remainingInstancesInCluster =
                                    this   .findAll(instance.getCluster()).stream()
                                           .filter(i -> {
                                               return !i.getHostname().equals(instance.getHostname());
                                           })
                                           .map(Instance::getHostname).collect(joining(","));

        this.properties.setInstancesInCluster(instance.getCluster(), remainingInstancesInCluster);
    }

}
