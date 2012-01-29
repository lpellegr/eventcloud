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

import fr.inria.eventcloud.EventCloud;
import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.properties.UnalterableElaProperty;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.webservices.api.EventCloudManagementWsApi;
import fr.inria.eventcloud.webservices.deployment.WebServiceDeployer;

/**
 * 
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
    public String createEventCloud() {
        return EventCloud.create(
                this.registryUrl, new EventCloudDeployer(),
                new Collection<UnalterableElaProperty>(), 1, 1).getId().toUrl();
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
    public String createPublishProxy(String eventcloudIdUrl) {
        checkEventcloudIdUrl(eventcloudIdUrl);

        return WebServiceDeployer.deployPublishWebService(
                this.registryUrl, eventcloudIdUrl,
                "proactive/services/EventCloud_publish-webservices",
                this.portLowerBound++);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createSubscribeProxy(String eventcloudIdUrl) {
        checkEventcloudIdUrl(eventcloudIdUrl);

        return WebServiceDeployer.deploySubscribeWebService(
                this.registryUrl, eventcloudIdUrl,
                "proactive/services/EventCloud_subscribe-webservices",
                this.portLowerBound++);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createPutGetProxy(String eventcloudIdUrl) {
        checkEventcloudIdUrl(eventcloudIdUrl);

        return WebServiceDeployer.deployPutGetWebService(
                this.registryUrl, eventcloudIdUrl,
                "proactive/services/EventCloud_putget-webservices",
                this.portLowerBound++);
    }

    private static void checkEventcloudIdUrl(String eventcloudIdUrl) {
        if (!EventCloudId.isEventCloudIdUrl(eventcloudIdUrl)) {
            throw new IllegalArgumentException(
                    "The specified eventcloudIdUrl is not valid");
        }
    }

}
