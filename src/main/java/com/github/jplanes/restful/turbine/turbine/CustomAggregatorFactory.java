/**
 * Copyright 2012 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jplanes.restful.turbine.turbine;

import java.util.Collection;

import com.github.jplanes.restful.turbine.stores.ClustersStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.turbine.data.AggDataFromCluster;
import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.handler.PerformanceCriteria;
import com.netflix.turbine.handler.TurbineDataHandler;
import com.netflix.turbine.monitor.TurbineDataMonitor;
import com.netflix.turbine.monitor.cluster.AggregateClusterMonitor;
import com.netflix.turbine.monitor.cluster.ClusterMonitor;
import com.netflix.turbine.monitor.cluster.ClusterMonitorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
     copy of DefaultAggregatorFactory
 */
@Component
public class CustomAggregatorFactory implements ClusterMonitorFactory<AggDataFromCluster> {

    private static final Logger logger = LoggerFactory.getLogger(CustomAggregatorFactory.class);

    private ClustersStore clustersStore;

    @Autowired
    public CustomAggregatorFactory(ClustersStore clustersStore) {
        this.clustersStore = clustersStore;
    }

    /**
     * @return {@link ClusterMonitor}<{@link AggDataFromCluster}>
     */
    @Override
    public ClusterMonitor<AggDataFromCluster> getClusterMonitor(String name) {
        TurbineDataMonitor<AggDataFromCluster> clusterMonitor = AggregateClusterMonitor.AggregatorClusterMonitorConsole.findMonitor(name + "_agg");
        return (ClusterMonitor<AggDataFromCluster>) clusterMonitor;
    }

    /**
     * Inits all configured cluster monitors
     */
    @Override
    public void initClusterMonitors() {

        for(String clusterName : this.clustersStore.findAll()) {
            ClusterMonitor<AggDataFromCluster> clusterMonitor = (ClusterMonitor<AggDataFromCluster>) AggregateClusterMonitor.findOrRegisterAggregateMonitor(clusterName);
            clusterMonitor.registerListenertoClusterMonitor(StaticListener);
            try {
                clusterMonitor.startMonitor();
            } catch (Exception e) {
                logger.warn("Could not init cluster monitor for: " + clusterName);
                clusterMonitor.stopMonitor();
                clusterMonitor.getDispatcher().stopDispatcher();
            }
        }
    }

    /**
     * shutdown all configured cluster monitors
     */
    @Override
    public void shutdownClusterMonitors() {

        for(String clusterName : this.clustersStore.findAll()) {
            ClusterMonitor<AggDataFromCluster> clusterMonitor = (ClusterMonitor<AggDataFromCluster>) AggregateClusterMonitor.findOrRegisterAggregateMonitor(clusterName);
            clusterMonitor.stopMonitor();
            clusterMonitor.getDispatcher().stopDispatcher();
        }
    }

    private TurbineDataHandler<AggDataFromCluster> StaticListener = new TurbineDataHandler<AggDataFromCluster>() {

        @Override
        public String getName() {
            return "StaticListener_For_Aggregator";
        }

        @Override
        public void handleData(Collection<AggDataFromCluster> stats) {
        }

        @Override
        public void handleHostLost(Instance host) {
        }

        @Override
        public PerformanceCriteria getCriteria() {
            return NonCriticalCriteria;
        }

    };

    private PerformanceCriteria NonCriticalCriteria = new PerformanceCriteria() {

        @Override
        public boolean isCritical() {
            return false;
        }

        @Override
        public int getMaxQueueSize() {
            return 0;
        }

        @Override
        public int numThreads() {
            return 0;
        }
    };
}
