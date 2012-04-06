/**
 * Copyright (c) 2011-2012 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.webservices.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.cxf.endpoint.Server;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import fr.inria.eventcloud.EventCloud;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.EventCloudsRegistryImpl;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.properties.UnalterableElaProperty;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.webservices.api.EventCloudManagementWsApi;
import fr.inria.eventcloud.webservices.deployment.WebServiceDeployer;

/**
 * Web service implementation for {@link EventCloudManagementWsApi}.
 * 
 * @author lpellegr
 */
public class EventCloudManagementServiceImpl implements
        EventCloudManagementWsApi {

    private final String registryUrl;

    private final int portLowerBound;

    // streamUrl -> one or several subscribe proxy
    private final ListMultimap<String, String> subscribeProxyEndpoints;

    // streamUrl -> one or several publish proxy
    private final ListMultimap<String, String> publishProxyEndpoints;

    // streamUrl -> one or several putget proxy
    private final ListMultimap<String, String> putgetProxyEndpoints;

    // proxyEndpoint -> one proxy server instance
    private final Map<String, ServiceInformation> proxyInstances;

    // port numbers which are already assigned
    private final Set<Integer> assignedPorts;

    private EventCloudsRegistry registry;

    public EventCloudManagementServiceImpl(String registryUrl,
            int portLowerBound) {
        this.registryUrl = registryUrl;
        this.portLowerBound = portLowerBound;

        this.subscribeProxyEndpoints = ArrayListMultimap.create();
        this.publishProxyEndpoints = ArrayListMultimap.create();
        this.putgetProxyEndpoints = ArrayListMultimap.create();

        this.proxyInstances = Maps.newHashMap();
        this.assignedPorts = Sets.newHashSet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean createEventCloud(String streamUrl) {
        return EventCloud.create(
                this.registryUrl, new EventCloudId(streamUrl),
                new EventCloudDeployer(),
                new ArrayList<UnalterableElaProperty>(), 1, 1).register();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean destroyEventCloud(String streamUrl) {
        if (this.getEventCloudsRegistry().contains(new EventCloudId(streamUrl))) {
            boolean result = true;

            result &= destroyProxies(streamUrl, this.publishProxyEndpoints);
            result &= destroyProxies(streamUrl, this.putgetProxyEndpoints);
            result &= destroyProxies(streamUrl, this.subscribeProxyEndpoints);

            return result
                    && this.getEventCloudsRegistry().undeploy(
                            new EventCloudId(streamUrl));
        }

        return false;
    }

    private boolean destroyProxies(String streamUrl,
                                   ListMultimap<String, String> proxyEndpoints) {
        boolean result = true;

        for (String proxyEndpoint : ImmutableList.copyOf(proxyEndpoints.get(streamUrl))) {
            result &= this.destroyProxy(proxyEndpoint, proxyEndpoints);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRegistryEndpointUrl() {
        return this.registryUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getEventCloudIds() {
        Set<EventCloudId> ecIds =
                this.getEventCloudsRegistry().listEventClouds();
        List<String> result = new ArrayList<String>(ecIds.size());

        for (EventCloudId ecId : ecIds) {
            result.add(ecId.getStreamUrl());
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createPublishProxy(String streamUrl) {
        // TODO: check that streamUrl exists
        int port = this.lockUnassignedPort();

        Server service =
                WebServiceDeployer.deployPublishWebService(
                        this.registryUrl, streamUrl,
                        "proactive/services/EventCloud_publish-webservices",
                        port);

        return this.storeAndReturnProxyAddress(
                service, port, streamUrl, this.publishProxyEndpoints);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createSubscribeProxy(String streamUrl) {
        // TODO: check that streamUrl exists
        int port = this.lockUnassignedPort();

        Server service =
                WebServiceDeployer.deploySubscribeWebService(
                        this.registryUrl, streamUrl,
                        "proactive/services/EventCloud_subscribe-webservices",
                        port);

        return this.storeAndReturnProxyAddress(
                service, port, streamUrl, this.subscribeProxyEndpoints);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createPutGetProxy(String streamUrl) {
        // TODO: check that streamUrl exists
        int port = this.lockUnassignedPort();

        Server service =
                WebServiceDeployer.deployPutGetWebService(
                        this.registryUrl, streamUrl,
                        "proactive/services/EventCloud_putget-webservices",
                        port);

        return this.storeAndReturnProxyAddress(
                service, port, streamUrl, this.putgetProxyEndpoints);
    }

    private String storeAndReturnProxyAddress(Server service,
                                              int port,
                                              String streamUrl,
                                              ListMultimap<String, String> proxyEndpoints) {
        String serviceAddress =
                service.getEndpoint().getEndpointInfo().getAddress();

        proxyEndpoints.put(streamUrl, serviceAddress);
        this.proxyInstances.put(serviceAddress, new ServiceInformation(
                service, streamUrl, port));

        return serviceAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSubscribeProxyEndpointUrls(String streamUrl) {
        return this.subscribeProxyEndpoints.get(streamUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getPublishProxyEndpointUrls(String streamUrl) {
        return this.publishProxyEndpoints.get(streamUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getPutgetProxyEndpointUrls(String streamUrl) {
        return this.putgetProxyEndpoints.get(streamUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean destroyPublishProxy(String publishProxyEndpoint) {
        return this.destroyProxy(
                publishProxyEndpoint, this.publishProxyEndpoints);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean destroySubscribeProxy(String subscribeProxyEndpoint) {
        return this.destroyProxy(
                subscribeProxyEndpoint, this.subscribeProxyEndpoints);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean destroyPutGetProxy(String putgetProxyEndpoint) {
        return this.destroyProxy(putgetProxyEndpoint, this.putgetProxyEndpoints);
    }

    private boolean destroyProxy(String proxyEndpoint,
                                 ListMultimap<String, String> proxyEndpointsMultimap) {
        ServiceInformation info = this.proxyInstances.remove(proxyEndpoint);

        if (info != null) {
            proxyEndpointsMultimap.remove(info.streamUrl, proxyEndpoint);
            info.service.destroy();
            this.assignedPorts.remove(info.port);

            return true;
        }

        return false;
    }

    private int lockUnassignedPort() {
        int port = this.portLowerBound;

        synchronized (this.assignedPorts) {
            while (true) {
                if (!this.assignedPorts.contains(port)) {
                    this.assignedPorts.add(port);
                    return port;
                }
                port++;
            }
        }
    }

    private synchronized EventCloudsRegistry getEventCloudsRegistry() {
        if (this.registry == null) {
            try {
                this.registry =
                        EventCloudsRegistryImpl.lookup(this.registryUrl);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        return this.registry;
    }

    private static class ServiceInformation {

        public final Server service;

        public final String streamUrl;

        public final int port;

        public ServiceInformation(Server service, String streamUrl, int port) {
            this.service = service;
            this.streamUrl = streamUrl;
            this.port = port;
        }

    }

}
