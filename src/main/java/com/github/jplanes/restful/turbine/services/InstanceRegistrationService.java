package com.github.jplanes.restful.turbine.services;

import com.github.jplanes.restful.turbine.stores.ClustersStore;
import com.github.jplanes.restful.turbine.stores.InstancesStore;
import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.plugins.PluginsFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class InstanceRegistrationService {

    private ClustersStore clustersStore;
    private InstancesStore instancesStore;

    @Autowired
    public InstanceRegistrationService(ClustersStore clustersStore, InstancesStore instancesStore) {
        this.clustersStore = clustersStore;
        this.instancesStore = instancesStore;
    }

    public Collection<Instance> findAllRegisteredInstances(String clusterName) {
        return  clusterName == null ?
                    this.instancesStore.findAll() :
                    this.instancesStore.findAll(clusterName);
    }

    public void registerInstance(Instance instance) {
        // creates the cluster if it doesn't exists.
        this.clustersStore.save(instance.getCluster());
        // creates or update the instance
        this.instancesStore.save(instance);
        // init the cluster monitoring for each new clusters only
        PluginsFactory.getClusterMonitorFactory().initClusterMonitors();
    }

    public void unregisterInstance(Instance instance) {
        this.instancesStore.delete(instance);
    }
}
