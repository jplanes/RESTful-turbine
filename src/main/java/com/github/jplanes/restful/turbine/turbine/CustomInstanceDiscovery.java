package com.github.jplanes.restful.turbine.turbine;

import com.github.jplanes.restful.turbine.connectors.HealthCheckConnector;
import com.github.jplanes.restful.turbine.stores.InstancesStore;
import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceDiscovery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;

@Component
public class CustomInstanceDiscovery implements InstanceDiscovery {

    private ExecutorService threadExecutor = Executors.newCachedThreadPool();
    private InstancesStore store;
    private HealthCheckConnector healthCheckConnector;

    @Autowired
    public CustomInstanceDiscovery(InstancesStore store, HealthCheckConnector healthCheckConnector) {
        this.store = store;
        this.healthCheckConnector = healthCheckConnector;
    }

    @Override
    public Collection<Instance> getInstanceList() throws Exception {
        return
            this.threadExecutor.submit(() ->
                        this.store  .findAll().parallelStream()
                                    .map(this::instanceWithHealthInformation)
                                    .collect(toList())
            ).get(10, SECONDS);
    }

    private Instance instanceWithHealthInformation(Instance instance) {
        boolean isHealthy = this.healthCheckConnector.isHostHealty(instance.getHostname());

        return new Instance(instance.getHostname(), instance.getCluster(), isHealthy);
    }
}
