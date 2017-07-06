package com.github.jplanes.restful.turbine.config;

import com.github.jplanes.restful.turbine.turbine.CustomAggregatorFactory;
import com.netflix.turbine.discovery.InstanceDiscovery;
import com.netflix.turbine.init.TurbineInit;
import com.netflix.turbine.plugins.PluginsFactory;
import com.netflix.turbine.streaming.servlet.TurbineStreamServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Configuration
public class TurbineConfig {

    @Resource
    private InstanceDiscovery instanceDiscovery;
    @Resource
    private CustomAggregatorFactory aggregatorFactory;

    @Bean
    public ServletRegistrationBean turbineStreamServlet() {
        return new ServletRegistrationBean(new TurbineStreamServlet(), "/turbine.stream");
    }

    @PostConstruct
    public void initTurbine() {
        PluginsFactory.setInstanceDiscovery(instanceDiscovery);
        PluginsFactory.setClusterMonitorFactory(aggregatorFactory);
        TurbineInit.init();
    }

}
