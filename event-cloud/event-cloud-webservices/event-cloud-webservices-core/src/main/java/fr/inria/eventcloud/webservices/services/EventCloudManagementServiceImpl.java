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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.EventCloudsRegistryImpl;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.providers.SemanticPersistentOverlayProvider;
import fr.inria.eventcloud.webservices.api.EventCloudManagementWsApi;
import fr.inria.eventcloud.webservices.deployment.ServiceInformation;
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
        EventCloudId eventCloudId = new EventCloudId(streamUrl);

        if (!this.getEventCloudsRegistry().contains(eventCloudId)) {
            EventCloudDescription eventCloudDescription =
                    new EventCloudDescription(eventCloudId);

            EventCloudDeployer deployer =
                    new EventCloudDeployer(
                            eventCloudDescription,
                            new EventCloudDeploymentDescriptor(
                                    new SemanticPersistentOverlayProvider()));

            deployer.deploy(1, 1);

            return this.getEventCloudsRegistry().register(deployer);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean destroyEventCloud(String streamUrl) {
        EventCloudId eventCloudId = new EventCloudId(streamUrl);

        if (this.getEventCloudsRegistry().contains(eventCloudId)) {
            boolean result = true;

            result &=
                    this.destroyProxies(streamUrl, this.publishProxyEndpoints);
            result &= this.destroyProxies(streamUrl, this.putgetProxyEndpoints);
            result &=
                    this.destroyProxies(streamUrl, this.subscribeProxyEndpoints);

            return result
                    && this.getEventCloudsRegistry().undeploy(eventCloudId);
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
        EventCloudId eventCloudId = new EventCloudId(streamUrl);

        if (!this.getEventCloudsRegistry().contains(eventCloudId)) {
            throw new IllegalArgumentException("No Event Cloud running for "
                    + streamUrl);
        }

        int port = this.lockUnassignedPort();

        ServiceInformation publishServiceInformation =
                WebServiceDeployer.deployPublishWebService(
                        this.registryUrl, streamUrl,
                        "proactive/services/EventCloud_publish-webservices",
                        port);

        return this.storeAndReturnProxyAddress(
                publishServiceInformation, this.publishProxyEndpoints);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createSubscribeProxy(String streamUrl) {
        EventCloudId eventCloudId = new EventCloudId(streamUrl);

        if (!this.getEventCloudsRegistry().contains(eventCloudId)) {
            throw new IllegalArgumentException("No Event Cloud running for "
                    + streamUrl);
        }

        int port = this.lockUnassignedPort();

        ServiceInformation subscribeServiceInformation =
                WebServiceDeployer.deploySubscribeWebService(
                        this.registryUrl, streamUrl,
                        "proactive/services/EventCloud_subscribe-webservices",
                        port);

        return this.storeAndReturnProxyAddress(
                subscribeServiceInformation, this.subscribeProxyEndpoints);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createPutGetProxy(String streamUrl) {
        EventCloudId eventCloudId = new EventCloudId(streamUrl);

        if (!this.getEventCloudsRegistry().contains(eventCloudId)) {
            throw new IllegalArgumentException("No Event Cloud running for "
                    + streamUrl);
        }

        int port = this.lockUnassignedPort();

        ServiceInformation putgetServiceInformation =
                WebServiceDeployer.deployPutGetWebService(
                        this.registryUrl, streamUrl,
                        "proactive/services/EventCloud_putget-webservices",
                        port);

        return this.storeAndReturnProxyAddress(
                putgetServiceInformation, this.putgetProxyEndpoints);
    }

    private String storeAndReturnProxyAddress(ServiceInformation serviceInformation,
                                              ListMultimap<String, String> proxyEndpoints) {
        String serviceAddress =
                serviceInformation.getServer()
                        .getEndpoint()
                        .getEndpointInfo()
                        .getAddress();

        proxyEndpoints.put(serviceInformation.getStreamUrl(), serviceAddress);
        this.proxyInstances.put(serviceAddress, serviceInformation);

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
        ServiceInformation serviceInformation =
                this.proxyInstances.remove(proxyEndpoint);

        if (serviceInformation != null) {
            serviceInformation.destroy();
            proxyEndpointsMultimap.remove(
                    serviceInformation.getStreamUrl(), proxyEndpoint);
            this.assignedPorts.remove(serviceInformation.getPort());

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

}
