package com.github.jplanes.restful.turbine.stores;

import com.github.jplanes.restful.turbine.dummy.InMemoryMapDataSource;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


public class ClustersStoreTest {

    private ClustersStore store;

    @Before
    public void init() {
        this.store = new ClustersStore(new InMemoryMapDataSource());
        this.store.save("cluster_1");
        this.store.save("cluster_2");
    }

    @Test
    public void FindAll_ShouldReturnAllClustersInTheStore() {
        Collection<String> clusters = store.findAll();
        assertThat(clusters, hasSize(2));
        assertThat(clusters, containsInAnyOrder("cluster_1", "cluster_2"));
    }

    @Test
    public void SaveCluster_ShouldAddToTheDataSource() {
        assertThat(this.store.findAll(), hasSize(2));

        this.store.save("new_cluster");
        assertThat(this.store.findAll(), hasSize(3));

        this.store.save("another_new_cluster");
        assertThat(this.store.findAll(), hasSize(4));

        assertThat(this.store.findAll(), hasItems("new_cluster", "another_new_cluster"));
    }

    @Test
    public void SaveSameClusterTwice_ShouldOnlyAddItOnce() {
        assertThat(this.store.findAll(), hasSize(2));

        this.store.save("cluster_1");

        assertThat(this.store.findAll(), hasSize(2));
    }

}
