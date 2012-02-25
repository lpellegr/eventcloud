/**
 * Copyright (c) 2011 INRIA.
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
import java.util.Set;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;

import fr.inria.eventcloud.EventCloud;
import fr.inria.eventcloud.EventCloudsRegistry;
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

    private int portLowerBound;

    public EventCloudManagementServiceImpl(String registryUrl,
            int portLowerBound) {
        this.registryUrl = registryUrl;
        this.portLowerBound = portLowerBound;
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
    public String getRegistryEndpointUrl() {
        return this.registryUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getEventCloudIds() {
        try {
            EventCloudsRegistry registry =
                    PAActiveObject.lookupActive(
                            EventCloudsRegistry.class, this.registryUrl);

            Set<EventCloudId> ecIds = registry.listEventClouds();
            List<String> result = new ArrayList<String>(ecIds.size());

            for (EventCloudId ecId : ecIds) {
                result.add(ecId.getStreamUrl());
            }

            return result;
        } catch (ActiveObjectCreationException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createPublishProxy(String streamUrl) {
        return WebServiceDeployer.deployPublishWebService(
                this.registryUrl, streamUrl,
                "proactive/services/EventCloud_publish-webservices",
                this.portLowerBound++);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createSubscribeProxy(String streamUrl) {
        return WebServiceDeployer.deploySubscribeWebService(
                this.registryUrl, streamUrl,
                "proactive/services/EventCloud_subscribe-webservices",
                this.portLowerBound++);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createPutGetProxy(String streamUrl) {
        return WebServiceDeployer.deployPutGetWebService(
                this.registryUrl, streamUrl,
                "proactive/services/EventCloud_putget-webservices",
                this.portLowerBound++);
    }

}
