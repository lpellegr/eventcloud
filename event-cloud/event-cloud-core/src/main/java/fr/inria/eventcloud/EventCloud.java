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
package fr.inria.eventcloud;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.properties.UnalterableElaProperty;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.tracker.SemanticTracker;
import fr.inria.eventcloud.utils.MurmurHash;

/**
 * EventCloud is an object that is used to model an EventCloud and to register
 * the properties associated to that Event-Cloud into the
 * {@link EventCloudsRegistry}.
 * 
 * @author lpellegr
 */
public final class EventCloud implements EventCloudApi, Serializable {

    private static final Logger log = LoggerFactory.getLogger(EventCloud.class);

    private static final long serialVersionUID = 1L;

    private final EventCloudId id;

    private final long creationTime;

    private final Collection<UnalterableElaProperty> elaProperties;

    private final String nodeProviderUrl;

    private final String registryUrl;

    private final List<String> trackerUrls;

    private final List<SemanticTracker> trackers;

    private EventCloud(String registryUrl, String nodeProviderUrl,
            Collection<UnalterableElaProperty> elaProperties, int nbTrackers,
            int nbPeers) {
        this.id =
                new EventCloudId(MurmurHash.hash64(
                        registryUrl.toString(), nodeProviderUrl.toString(),
                        elaProperties.toString(), Integer.toString(nbTrackers),
                        Integer.toString(nbPeers)));

        this.creationTime = System.currentTimeMillis();
        this.elaProperties = elaProperties;
        this.nodeProviderUrl = nodeProviderUrl;
        this.registryUrl = registryUrl;
        this.trackerUrls = new ArrayList<String>();
        this.trackers = new ArrayList<SemanticTracker>();

        this.initializeNetwork(nbTrackers, nbPeers);
    }

    private void initializeNetwork(int nbTrackers, int nbPeers) {
        // TODO retrieve the nodes, create and deploy the peers and the trackers
        // and populate the fields

        // temporary implementation that deploy the trackers and the peers on
        // the local machine
        EventCloudDeployer deployer =
                new EventCloudDeployer();
        deployer.deploy(nbTrackers, nbPeers);
        
        for (Tracker tracker : deployer.getTrackers()) {
            this.trackers.add((SemanticTracker) tracker);
            this.trackerUrls.add(PAActiveObject.getUrl(tracker));
        }
    }

    /**
     * Creates a new Event-Cloud with the specified arguments.
     * 
     * @param registryUrl
     *            the URL to the EventCloudsRegistry that stores the properties.
     * @param nodeProviderUrl
     *            the URL to the node provider.
     * @param elaProperties
     *            a collection of non-alterable ELA properties.
     * @param nbTrackers
     *            the number of trackers to create for the Event-Cloud.
     * @param nbPeers
     *            the initial number of peers that is contained by the
     *            Event-Cloud.
     * 
     * @return the Event-Cloud that has been created. When the object is
     *         returned, the Event-Cloud has been created (i.e. the peers and
     *         the trackers are running and have joined the network) and the
     *         properties associated to the Event-Cloud have been registered
     *         into the EventCloudsRegistry.
     */
    public static EventCloud create(String registryUrl,
                                    String nodeProviderUrl,
                                    Collection<UnalterableElaProperty> elaProperties,
                                    int nbTrackers, int nbPeers) {
        EventCloud eventCloud =
                new EventCloud(
                        registryUrl, nodeProviderUrl, elaProperties,
                        nbTrackers, nbPeers);

        eventCloud.register();

        return eventCloud;
    }

    /**
     * Registers the properties to the {@link EventCloudsRegistry}.
     */
    private void register() {
        log.info("Registering Event-Cloud {}", this.id);

        // TODO implement the registration into the EventCloudRegistry
        // and check if the registration has already be done
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventCloudId getId() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCreationTime() {
        return this.creationTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<UnalterableElaProperty> getElaProperties() {
        return this.elaProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNodeProviderUrl() {
        return this.nodeProviderUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRegistryUrl() {
        return this.registryUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getTrackerUrls() {
        return new Collection<String>(this.trackerUrls);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<SemanticTracker> getTrackers() {
        return new Collection<SemanticTracker>(this.trackers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SemanticTracker selectTracker() {
        // TODO use a metric (e.g. according to the load) to select a tracker
        return this.trackers.get(ProActiveRandom.nextInt(this.trackers.size()));
    }

}
