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
package fr.inria.eventcloud.deployment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.objectweb.proactive.api.PAActiveObject;

import fr.inria.eventcloud.EventCloud;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.EventCloudsRegistryFactory;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.properties.UnalterableElaProperty;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * This class is used to instantiate an Event Cloud infrastructure (i.e. an
 * {@link EventCloudsRegistry} and one or several {@link EventCloud}s).
 * 
 * @author lpellegr
 */
public class JunitEventCloudInfrastructureDeployer {

    private final Map<EventCloudId, EventCloud> eventClouds;

    private final DatastoreType datastoreType;

    private EventCloudsRegistry eventCloudsRegistry;

    private String eventCloudsRegistryUrl;

    /**
     * Creates an infrastructure deployer for testing purposes. By default this
     * deployer will create peers inside an event cloud by using an in-memory
     * datastore.
     */
    public JunitEventCloudInfrastructureDeployer() {
        this(DatastoreType.IN_MEMORY);
    }

    public JunitEventCloudInfrastructureDeployer(DatastoreType type) {
        this.eventClouds = new HashMap<EventCloudId, EventCloud>();
        this.datastoreType = type;
    }

    public EventCloudId createEventCloud(int nbPeers) {
        return this.createEventCloud(1, nbPeers);
    }

    public EventCloudId createEventCloud(int nbTrackers, int nbPeers) {
        this.initializeEventCloudsRegistry();

        EventCloud eventcloud =
                EventCloud.create(
                        this.eventCloudsRegistryUrl,
                        new JunitEventCloudDeployer(this.datastoreType),
                        new ArrayList<UnalterableElaProperty>(), nbTrackers,
                        nbPeers);

        this.eventClouds.put(eventcloud.getId(), eventcloud);

        if (!this.eventCloudsRegistry.register(eventcloud)) {
            throw new IllegalStateException(
                    "Eventcloud registration failed: it is already registered");
        }

        return eventcloud.getId();
    }

    private synchronized void initializeEventCloudsRegistry() {
        if (this.eventCloudsRegistry == null) {
            this.eventCloudsRegistry =
                    EventCloudsRegistryFactory.newEventCloudsRegistry();
            this.eventCloudsRegistryUrl =
                    PAActiveObject.getUrl(this.eventCloudsRegistry);
        }
    }

    public EventCloud find(EventCloudId id) {
        return this.eventClouds.get(id);
    }

    public SemanticTracker getRandomSemanticTracker(EventCloudId id) {
        EventCloud ec = this.eventClouds.get(id);
        if (ec == null) {
            return null;
        }

        return ec.selectTracker();
    }

    public SemanticPeer getRandomSemanticPeer(EventCloudId id) {
        SemanticTracker tracker = this.getRandomSemanticTracker(id);
        if (tracker == null) {
            return null;
        }

        return tracker.getRandomSemanticPeer();
    }

    public String getEventCloudsRegistryUrl() {
        return this.eventCloudsRegistryUrl;
    }

    public void undeploy() {
        Iterator<EventCloudId> it = this.eventClouds.keySet().iterator();

        while (it.hasNext()) {
            EventCloudId ecId = it.next();
            this.eventClouds.get(ecId).getEventCloudDeployer().undeploy();
        }

        PAActiveObject.terminateActiveObject(this.eventCloudsRegistry, false);
    }

    public void undeploy(EventCloudId eventCloudId) {
        if (!this.eventClouds.containsKey(eventCloudId)) {
            throw new IllegalArgumentException("Event cloud id not managed: "
                    + eventCloudId);
        }

        this.eventClouds.get(eventCloudId).getEventCloudDeployer().undeploy();
        this.eventClouds.remove(eventCloudId);
    }

}
