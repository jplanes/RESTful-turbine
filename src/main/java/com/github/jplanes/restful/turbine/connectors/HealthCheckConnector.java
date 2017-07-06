package com.github.jplanes.restful.turbine.connectors;

import com.netflix.config.DynamicPropertyFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.nio.client.HttpAsyncClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Component
public class HealthCheckConnector {

    private HttpAsyncClient httpClient;

    @Autowired
    public HealthCheckConnector(HttpAsyncClient httpClient) {
        this.httpClient = httpClient;
    }

    public boolean isHostHealty(String host) {
        try {
            final String healthCheckPath = DynamicPropertyFactory.getInstance().getStringProperty("turbine.instanceHealthCheck", null).get();
            final HttpGet request = new HttpGet("http://" + host + healthCheckPath);
            Future<HttpResponse> future = this.httpClient.execute(request, null);

            return future   .get(200, MILLISECONDS)
                            .getStatusLine().getStatusCode() == 200;

        } catch (Exception e) {
            return false;
        }
    }
}
