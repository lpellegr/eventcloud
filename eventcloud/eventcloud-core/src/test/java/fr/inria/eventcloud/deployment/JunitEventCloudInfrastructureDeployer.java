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
package fr.inria.eventcloud.deployment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitHelper;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * This class is used to instantiate an EventCloud infrastructure (i.e. an
 * {@link EventCloudsRegistry} and one or several EventClouds).
 * 
 * @author lpellegr
 */
public class JunitEventCloudInfrastructureDeployer {

    private final Map<EventCloudId, EventCloudDeployer> eventClouds;

    private EventCloudsRegistry eventCloudsRegistry;

    private String eventCloudsRegistryUrl;

    public JunitEventCloudInfrastructureDeployer() {
        this.eventClouds = new HashMap<EventCloudId, EventCloudDeployer>();
    }

    public EventCloudId newEventCloud(int nbTrackers, int nbPeers) {
        return this.newEventCloud(
                new EventCloudDescription(),
                new EventCloudDeploymentDescriptor(), nbTrackers, nbPeers);
    }

    public EventCloudId newEventCloud(EventCloudDescription eventCloudDescription,
                                      int nbTrackers, int nbPeers) {
        return this.newEventCloud(
                eventCloudDescription, new EventCloudDeploymentDescriptor(),
                nbTrackers, nbPeers);
    }

    public EventCloudId newEventCloud(EventCloudDeploymentDescriptor deploymentDescriptor,
                                      int nbTrackers, int nbPeers) {
        return this.newEventCloud(
                new EventCloudDescription(), deploymentDescriptor, nbTrackers,
                nbPeers);
    }

    public EventCloudId newEventCloud(EventCloudDescription eventCloudDescription,
                                      EventCloudDeploymentDescriptor deploymentDescriptor,
                                      int nbTrackers, int nbPeers) {
        this.initializeEventCloudsRegistry();

        JunitHelper.setTestingDeploymentConfiguration(deploymentDescriptor);

        EventCloudDeployer deployer =
                new EventCloudDeployer(
                        eventCloudDescription, deploymentDescriptor);
        deployer.deploy(nbTrackers, nbPeers);

        this.eventClouds.put(eventCloudDescription.getId(), deployer);

        if (!this.eventCloudsRegistry.register(deployer)) {
            throw new IllegalStateException(
                    "Eventcloud registration failed: it is already registered");
        }

        return eventCloudDescription.getId();
    }

    private synchronized void initializeEventCloudsRegistry() {
        if (this.eventCloudsRegistry == null) {
            this.eventCloudsRegistry =
                    EventCloudsRegistryFactory.newEventCloudsRegistry();
            this.eventCloudsRegistryUrl =
                    PAActiveObject.getUrl(this.eventCloudsRegistry);
        }
    }

    public EventCloudDeployer find(EventCloudId id) {
        return this.eventClouds.get(id);
    }

    public SemanticTracker getRandomSemanticTracker(EventCloudId id) {
        EventCloudDeployer deployer = this.eventClouds.get(id);
        if (deployer == null) {
            return null;
        }

        return deployer.getRandomSemanticTracker();
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
            EventCloudId id = it.next();
            this.eventClouds.get(id).undeploy();
        }

        ComponentUtils.terminateComponent(this.eventCloudsRegistry);
    }

    public void undeploy(EventCloudId eventCloudId) {
        if (!this.eventClouds.containsKey(eventCloudId)) {
            throw new IllegalArgumentException("EventCloud id not managed: "
                    + eventCloudId);
        }

        this.eventClouds.get(eventCloudId).undeploy();
        this.eventClouds.remove(eventCloudId);
    }

}
