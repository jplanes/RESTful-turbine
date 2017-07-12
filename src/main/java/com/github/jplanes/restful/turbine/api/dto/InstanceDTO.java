package com.github.jplanes.restful.turbine.api.dto;

public class InstanceDTO {
        private String host;
        private String cluster;

        public InstanceDTO() { }

        public InstanceDTO(String host, String cluster) {
            this.host = host;
            this.cluster = cluster;
        }

        public String getHost() {
            return host;
        }

        public String getCluster() {
            return cluster;
        }
    }
