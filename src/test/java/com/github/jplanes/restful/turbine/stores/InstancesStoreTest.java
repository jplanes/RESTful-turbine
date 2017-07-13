package com.github.jplanes.restful.turbine.stores;

import com.github.jplanes.restful.turbine.dummy.InMemoryMapDataSource;
import com.netflix.turbine.discovery.Instance;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InstancesStoreTest {

    private ClustersStore clusterStore;
    private InstancesStore store;

    @Before
    public void init() {
        clusterStore = mock(ClustersStore.class);
        when(clusterStore.findAll()).thenReturn(asList("cluster_1", "cluster_2"));

        store = new InstancesStore(clusterStore, new InMemoryMapDataSource());
        store.save(new Instance("host_1", "cluster_1", true));
        store.save(new Instance("host_2", "cluster_1", true));
        store.save(new Instance("host_3", "cluster_2", true));
    }

    @Test
    public void FindAll_ShouldReturnAllInstances() {
        Collection<Instance> instances = store.findAll();

        assertThat(instances, hasSize(3));
        assertThat(
                instances.stream().filter(i -> i.getCluster().equals("cluster_1")).collect(toList()),
                hasSize(2)
        );
        assertThat(
                instances.stream().filter(i -> i.getCluster().equals("cluster_2")).collect(toList()),
                hasSize(1)
        );
        assertThat(
                instances.stream().map(Instance::getHostname).collect(toList()),
                containsInAnyOrder("host_1", "host_2", "host_3")
        );
    }

    @Test
    public void FindAll_WithClusterParameter_ShouldReturnOnlyTheInstancesInThatCluster() {
        Collection<Instance> instances = store.findAll("cluster_1");
        assertThat(instances, hasSize(2));
        assertThat(
                instances.stream().map(Instance::getHostname).collect(toList()),
                containsInAnyOrder("host_1", "host_2")
        );
        // only 1 cluster in the response
        assertThat(
                instances.stream().map(Instance::getCluster).collect(toSet()),
                hasSize(1)
        );

        instances = store.findAll("cluster_2");
        assertThat(instances, hasSize(1));
    }

    @Test
    public void SaveNew_ShouldAppendToTheCluster() {
        Collection<Instance> instances = store.findAll("cluster_1");
        assertThat(instances, hasSize(2));

        Instance newInstance = new Instance("host_4","cluster_1", true);
        store.save(newInstance);

        instances = store.findAll("cluster_1");
        assertThat(instances, hasSize(3));
        assertThat(instances, hasItem(newInstance));
    }

    @Test
    public void SaveExisting_ShoulNotAddItAgain() {
        Collection<Instance> instances = store.findAll("cluster_1");
        assertThat(instances, hasSize(2));

        Instance newInstance = new Instance("host_1","cluster_1", true);
        store.save(newInstance);

        instances = store.findAll("cluster_1");
        assertThat(instances, hasSize(2));
    }

    @Test
    public void DeleteInstance_ShouldRemoveIt() {
        Collection<Instance> instances = store.findAll("cluster_1");
        assertThat(instances, hasSize(2));

        Instance instanceToDelete = new Instance("host_1","cluster_1", true);
        store.delete(instanceToDelete);

        instances = store.findAll("cluster_1");
        assertThat(instances, hasSize(1));
        assertFalse(instances.contains(instanceToDelete));
    }

    @Test
    public void DeleteInexistentInstance_ShouldNotRemoveAnything() {
        Collection<Instance> instances = store.findAll("cluster_1");
        assertThat(instances, hasSize(2));

        Instance instanceToDelete = new Instance("inexistent_host","cluster_1", true);
        store.delete(instanceToDelete);

        instances = store.findAll("cluster_1");
        assertThat(instances, hasSize(2));
    }

}
