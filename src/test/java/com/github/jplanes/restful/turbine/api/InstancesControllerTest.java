package com.github.jplanes.restful.turbine.api;

import com.github.jplanes.restful.turbine.services.InstanceRegistrationService;
import com.netflix.turbine.discovery.Instance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(InstancesController.class)
public class InstancesControllerTest {

    @MockBean
    private InstanceRegistrationService service;

    @Autowired
    private MockMvc mvc;

    @Before
    public void initMocks() {
        Collection<Instance> cluster1Instances = asList(
                new Instance("host_1", "cluster_1", true),
                new Instance("host_2", "cluster_1", true)
        );
        when(service.findAllRegisteredInstances(eq("cluster_1"))).thenReturn(cluster1Instances);

        Collection<Instance> cluster2Instances = asList(
                new Instance("host_1", "cluster_2", true)
        );
        when(service.findAllRegisteredInstances(eq("cluster_2"))).thenReturn(cluster2Instances);
    }

    @Test
    public void GETInstances_WithClusterParameter_ShouldReturnAllInstancesInCluster() throws Exception {
        this.mvc.perform(get("/instances?cluster=cluster_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].host", is("host_1")))
                .andExpect(jsonPath("$[1].host", is("host_2")))
                .andExpect(jsonPath("$..cluster", everyItem(equalTo("cluster_1"))))
        ;

        verify(service, times(1)).findAllRegisteredInstances(eq("cluster_1"));
        verifyNoMoreInteractions(service);
    }

    @Test
    public void GETInstances_WithoutCluster_ShouldReturnBadRequest() throws Exception {
        this.mvc.perform(get("/instances"))
                .andExpect(status().isBadRequest())
        ;

        verify(service, never()).findAllRegisteredInstances(anyString());
    }

    @Test
    public void POSTInstance_ValidBody_ShouldReturnOkMessage() throws Exception {
        this.mvc.perform(
                        post("/instances")
                            .contentType(APPLICATION_JSON)
                            .content("{ \"host\": \"host_1\", \"cluster\": \"cluster_1\" }"))
                .andExpect(status().isOk())
        ;

        verify(service, times(1)).registerInstance(refEq(new Instance("host_1", "cluster_1", true)));
        verifyNoMoreInteractions(service);
    }

    @Test
    public void POSTInstance_InvalidBody_ShouldReturnBadRequest() throws Exception {
        this.mvc.perform(
                post("/instances")
                        .contentType(APPLICATION_JSON)
                        .content("{ \"cluster\": \"cluster_1\" }"))
                .andExpect(status().isBadRequest())
        ;

        verify(service, never()).registerInstance(any());
    }

    @Test
    public void POSTInstance_WithoutBody_ShouldReturnBadRequest() throws Exception {
        this.mvc.perform(
                post("/instances")
                        .contentType(APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
        ;

        verify(service, never()).registerInstance(any());
    }

    @Test
    public void DELETEInstance_ValidParameters_ShouldCallTheService() throws Exception {
        this.mvc.perform(delete("/instances/cluster_1/host_1"))
                .andExpect(status().isOk())
        ;

        verify(service, times(1)).unregisterInstance(refEq(new Instance("host_1", "cluster_1", true)));
    }

    @Test
    public void DELETEInstance_InvalidParameters_ShouldReturnResourceNotFound() throws Exception {
        this.mvc.perform(delete("/instances/host_1"))
                .andExpect(status().isNotFound())
        ;

        verify(service, never()).unregisterInstance(any());
    }

}
