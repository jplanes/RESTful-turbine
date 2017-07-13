RESTful-Turbine
======

Intro
-------

The aim of this project is to let developers configure and run a turbine server in a straightforward way by adding or removing hystrix instances on the fly.

The [official distribution of turbine](https://github.com/Netflix/Turbine) provides 3 different ways for instance discovering:

#### 1. FileBasedInstanceDiscovery

By using [this strategy](https://github.com/Netflix/Turbine/wiki/Configuration-(1.x)#filebasedinstancediscovery-impl) you can set up a **config.properties** file in your classpath with all your application clusters and instances. Typically, this configuration file will look like this:

```

##### Turbine configuration
turbine.instanceUrlSuffix=/hystrix.stream
turbine.aggregator.clusterConfig=app_1,app_2
turbine.ConfigPropertyBasedDiscovery.app_1.instances=instance_1_app_1,instance_2_app_1
turbine.ConfigPropertyBasedDiscovery.app_2.instances=instance_1_app_2,instance_2_app_2

```
As you may notice, this is very easy to configure but if you are working inside an elastic infrastructure you will be losing several hours a week maintaining this file up to date. Because you have to update this file content each time you shrink or enlarge any cluster. 

#### 2. EurekaInstanceDiscovery

You may want to take a look at [this instance discovery strategy](https://github.com/Netflix/Turbine/wiki/Configuration-(1.x)#eurekainstancediscovery-impl) if you are using [Eureka](https://github.com/Netflix/eureka). This strategy will listen to every change inside your infrastructure and register new monitors for each new instance:

```
InstanceDiscovery.impl=com.netflix.turbine.discovery.EurekaInstanceDiscovery.class
```

But what happens if you aren't using Eureka?

#### 3. AwsInstanceDiscovery

If your application is mounted over AWS you can use the [AwsInstanceDiscovery](https://javalibs.com/artifact/com.netflix.turbine/turbine-contrib?className=com.netflix.turbine.discovery.AwsInstanceDiscovery&source) provided by [turbine-contrib](https://mvnrepository.com/artifact/com.netflix.turbine/turbine-contrib/1.0.0).
This InstanceDiscovery will listen for changes on every AutoScallingGroup and automatically add/remove instances.

-------

Though all these options are great, what happens if you are using neither AWS nor Eureka? Are you condemned to waste such a long hours keeping every config file up to date?

This is when RESTful-turbine becomes handy, it gives you the possibility to add or remove instances or clusters by sending some requests over HTTP. For example, if you want to register a new instance you can simply do:

```
curl 
    -H "Content-Type: application/json" 
    -X POST 
    -d '{"cluster":"app_name","host":"instance_ip"}'
    http://localhost:8080/instances
```

Or, if you want to see all instances in a specific cluster:

```
curl http://localhost:8080/instances\?cluster\=app_name
[{"host":"instance_ip","cluster":"app_name"}]
```

So, every time your cluster scales or shrinks you can trigger a POST | DELETE request to this API and turbine will start or stop monitoring the instance.
 
How to run RESTful-turbine
-------

First, you will need to download the latest stable version:

```
curl 
    -L https://github.com/jplanes/restful-turbine/releases/download/{latest_version}/restful-turbine-{latest_version}.jar 
    -o RESTful-turbine.jar
```

Once you have downloaded it you may want to set up a configuration file. RESTful-turbine is 100% compatible with Turbine & [Archaius](https://github.com/Netflix/archaius/wiki/Getting-Started), the only configuration we don't back up is the one related to clusters and instance declaration (because we use our own InstanceDiscovery strategy).
 
 A typical configuration file will look like this:

```
##### Turbine configuration
# Suffix where the hystrix stream is located in each instance.
turbine.instanceUrlSuffix=:5000/hystrix.stream
# This health check is used to tell turbine when an instance is healthy or nor.
turbine.instanceHealthCheck=:5000/health
```

The last (but not least) step is to actually run the turbine server. You can run it by simply executing this command:

```
java 
    -Darchaius.configurationSource.additionalUrls=file:///path/to/the/config.properties 
    -jar RESTful-turbine.jar
```
Once everything is up and running you can point your Hystrix dashboard to 

> http://{turbine_server}:8080/turbine.stream?cluster=cluster_name

How to register a new instance
-------

Typically, every time you create a new instance (or every time your application starts) you may want to register it into the turbine stream aggregator.

This procedure is very simple and you can do it by executing this POST: 

```
curl 
    -H "Content-Type: application/json" 
    -X POST 
    -d '{"cluster":"app_name","host":"instance_ip"}'
    http://localhost:8080/instances
```

How to unregister an instance
-------

If you are having some issues and you want to shrink your cluster, or if you simply realized your application doesn't need so many servers, you can shut down some instances and delete them from the monitor by just sending a DELETE request to:

```
curl -X "DELETE" HTTP://localhost:8080/instances/{cluster_name}/{instance_name}
```

How to change the RESTful-turbine port
-------

As RESTful-turbine is using Spring-Boot you can simply use the **-Dserver.port** parameter:

```
java 
    -Darchaius.configurationSource.additionalUrls=file:///path/to/the/config.properties 
    -Dserver.port=80
    -jar RESTful-turbine.jar
```

LICENSE
-----

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


