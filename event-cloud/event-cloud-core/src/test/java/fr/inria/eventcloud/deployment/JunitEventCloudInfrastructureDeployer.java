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

import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extensions.p2p.structured.deployment.TestingDeploymentConfiguration;

import fr.inria.eventcloud.EventCloud;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.EventCloudsRegistryFactory;
import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.properties.UnalterableElaProperty;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * 
 * @author lpellegr
 */
public class JunitEventCloudInfrastructureDeployer {

    private final EventCloudsRegistry eventCloudsRegistry;

    private final String eventCloudsRegistryUrl;

    public final Map<EventCloudId, EventCloud> eventClouds;

    public JunitEventCloudInfrastructureDeployer() {
        this.eventCloudsRegistry =
                EventCloudsRegistryFactory.newEventCloudsRegistry();
        this.eventCloudsRegistryUrl =
                PAActiveObject.getUrl(this.eventCloudsRegistry);
        this.eventClouds = new HashMap<EventCloudId, EventCloud>();
    }

    public EventCloudId createEventCloud(int nbPeers) {
        return this.createEventCloud(1, nbPeers);
    }

    public EventCloudId createEventCloud(int nbTrackers, int nbPeers) {
        EventCloud ec =
                EventCloud.create(
                        PAActiveObject.getUrl(this.eventCloudsRegistry), null,
                        new TestingDeploymentConfiguration(),
                        new Collection<UnalterableElaProperty>(), nbTrackers,
                        nbPeers);
        this.eventCloudsRegistry.register(ec);
        this.eventClouds.put(ec.getId(), ec);
        return ec.getId();
    }

    public void destroyEventCloud(EventCloudId id) {
        this.eventClouds.remove(id);
        // TODO: undeploy network
    }

    public EventCloud find(EventCloudId id) {
        return this.eventClouds.get(id);
    }

    public SemanticTracker getRandomSemanticTracker(EventCloudId id) {
        EventCloud ec = this.eventClouds.get(id);
        if (ec == null) {
            return null;
        }

        return ec.getTrackers().toArray(
                new SemanticTracker[ec.getTrackers().size()])[ProActiveRandom.nextInt(0)];
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

}
