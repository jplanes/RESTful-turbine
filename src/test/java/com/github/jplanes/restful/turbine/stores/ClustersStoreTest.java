package com.github.jplanes.restful.turbine.stores;

import com.github.jplanes.restful.turbine.dummy.InMemoryMapDataSource;
import com.github.jplanes.restful.turbine.stores.datasource.KeyValueDataSource;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.crypto.dsig.keyinfo.KeyValue;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


public class ClustersStoreTest {

    private ClustersStore store;

    @Test
    public void FindAll_ShouldReturnAllClustersInTheStore() {
        KeyValueDataSource ds = new InMemoryMapDataSource();
        ds.set("turbine.aggregator.clusterConfig", "cluster_1,cluster_2");
        this.store = new ClustersStore(ds);

        assertThat(this.store.findAll(), containsInAnyOrder("cluster_1", "cluster_2"));
    }

    @Test
    public void SaveCluster_ShouldAddToTheDataSource() {
        this.store = new ClustersStore(new InMemoryMapDataSource());
        assertThat(this.store.findAll(), hasSize(0));

        this.store.save("new_cluster");
        assertThat(this.store.findAll(), hasSize(1));

        this.store.save("another_new_cluster");
        assertThat(this.store.findAll(), hasSize(2));

        assertThat(this.store.findAll(), containsInAnyOrder("new_cluster", "another_new_cluster"));
    }

    @Test
    public void SaveSameClusterTwice_ShouldOnlyAddItOnce() {
        KeyValueDataSource ds = Mockito.spy(new InMemoryMapDataSource());
        this.store = new ClustersStore(ds);
        assertThat(this.store.findAll(), hasSize(0));

        this.store.save("new_cluster");
        this.store.save("another_new_cluster");
        this.store.save("new_cluster");

        assertThat(this.store.findAll(), hasSize(2));
        verify(ds, times(2)).set(any(), any());
    }

}
