package com.github.jplanes.restful.turbine.services;

import com.github.jplanes.restful.turbine.stores.ClustersStore;
import com.github.jplanes.restful.turbine.stores.InstancesStore;
import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.monitor.cluster.ClusterMonitorFactory;
import com.netflix.turbine.plugins.PluginsFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class InstanceRegistrationServiceTest {

    private InstanceRegistrationService service;
    private ClustersStore clusterStore = mock(ClustersStore.class);
    private InstancesStore instanceStore = mock(InstancesStore.class);
    private ClusterMonitorFactory<?> clusterMonitorFactory = mock(ClusterMonitorFactory.class);

    @Before
    public void init() {
        Collection<Instance> cluster1Instances = asList(
            new Instance("host_1", "cluster_1", true),
            new Instance("host_2", "cluster_1", true)
        );
        Collection<Instance> cluster2Instances = asList(
                new Instance("host_3", "cluster_2", true)
        );
        Collection<Instance> allInstances = newArrayList();
        allInstances.addAll(cluster1Instances);
        allInstances.addAll(cluster2Instances);

        when(instanceStore.findAll()).thenReturn(allInstances);
        when(instanceStore.findAll(eq("cluster_1"))).thenReturn(cluster1Instances);
        when(instanceStore.findAll(eq("cluster_2"))).thenReturn(cluster2Instances);

        PluginsFactory.setClusterMonitorFactory(clusterMonitorFactory);

        this.service = new InstanceRegistrationService(clusterStore, instanceStore);
    }

    @Test
    public void FindAllInstances_ShouldReturnAllOfThem() {
        assertThat(
                this.service.findAllRegisteredInstances(null),
                hasSize(3)
        );

        verify(instanceStore, times(1)).findAll();
        verifyNoMoreInteractions(instanceStore);
    }

    @Test
    public void FindAllInstances_WithCluster_ShouldReturnOnlySome() {
        assertThat(
                this.service.findAllRegisteredInstances("cluster_1"),
                hasSize(2)
        );

        assertThat(
                this.service.findAllRegisteredInstances("cluster_2"),
                hasSize(1)
        );

        verify(instanceStore, times(2)).findAll(any());
        verifyNoMoreInteractions(instanceStore);
    }

    @Test
    public void FindAllInstances_WithNotValidCluster_ShouldReturnEmtpy() {
        assertThat(
                this.service.findAllRegisteredInstances("invalid_cluster"),
                empty()
        );

        verify(instanceStore, times(1)).findAll(any());
        verifyNoMoreInteractions(instanceStore);
    }

    @Test
    public void RegisterNewInstance_ShouldSaveTheInstanceAndTheCluster() {
        Instance newInstance = new Instance("host_x", "cluster_x", true);
        this.service.registerInstance(newInstance);

        verify(instanceStore, times(1)).save(eq(newInstance));
        verify(clusterStore, times(1)).save(eq("cluster_x"));
        verify(clusterMonitorFactory, times(1)).initClusterMonitors();

        verifyNoMoreInteractions(instanceStore);
        verifyNoMoreInteractions(clusterStore);
        verifyNoMoreInteractions(clusterMonitorFactory);
    }

    @Test
    public void UnregisterInstance_ShouldDeleteTheInstanceFromStore() {
        Instance instanceToDelete = new Instance("host_x", "cluster_x", true);
        this.service.unregisterInstance(instanceToDelete);

        verify(instanceStore, times(1)).delete(instanceToDelete);
        verifyNoMoreInteractions(instanceStore);
        verifyNoMoreInteractions(clusterStore);
    }

}
