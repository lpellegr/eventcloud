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
package fr.inria.eventcloud.proxies;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.properties.UnalterableElaProperty;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.factories.ProxyFactory;

/**
 * This class is used to keep in cache the information associated to an
 * EventCloud in order to reduce the number of calls to an
 * {@link EventCloudsRegistry}.
 * 
 * @author lpellegr
 * 
 * @see ProxyFactory
 */
public class EventCloudCache implements Serializable {

    private static final long serialVersionUID = 1L;

    private final EventCloudsRegistry registry;

    private EventCloudDeployer deployer;

    /*
     * TODO: add a thread that updates periodically the trackers.
     */
    public EventCloudCache(String registryUrl, EventCloudId eventcloudId)
            throws EventCloudIdNotManaged {
        try {
            this.registry =
                    EventCloudsRegistryFactory.lookupEventCloudsRegistry(registryUrl);

            this.deployer = this.registry.find(eventcloudId);

            if (this.deployer == null) {
                throw new EventCloudIdNotManaged(
                        eventcloudId.toString(), registryUrl);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public EventCloudsRegistry getRegistry() {
        return this.registry;
    }

    public EventCloudId getId() {
        return this.deployer.getEventCloudDescription().getId();
    }

    public long getCreationTime() {
        return this.deployer.getEventCloudDescription().getCreationTime();
    }

    public List<UnalterableElaProperty> getElaProperties() {
        return this.deployer.getEventCloudDescription().getElaProperties();
    }

    public List<Tracker> getTrackers() {
        return this.deployer.getTrackers();
    }

}
