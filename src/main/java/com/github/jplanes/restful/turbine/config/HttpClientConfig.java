package com.github.jplanes.restful.turbine.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    @Bean
    public HttpAsyncClient httpClient() {
        final RequestConfig requestConfig = RequestConfig   .custom()
                                                            .setSocketTimeout(300)
                                                            .setConnectTimeout(100)
                                                            .build();

        CloseableHttpAsyncClient client = HttpAsyncClients   .custom()
                                                            .setDefaultRequestConfig(requestConfig)
                                                            .setMaxConnTotal(20)
                                                            .build();
        client.start();
        return client;
    }
}
