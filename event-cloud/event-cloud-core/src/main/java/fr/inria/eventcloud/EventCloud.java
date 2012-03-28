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
package fr.inria.eventcloud;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.objectweb.proactive.extensions.p2p.structured.utils.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.properties.UnalterableElaProperty;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * EventCloud is an object that is used to model an EventCloud and to register
 * the properties associated to that Event-Cloud into the
 * {@link EventCloudsRegistry}.
 * 
 * @author lpellegr
 */
public class EventCloud implements EventCloudApi, Serializable {

    private static final Logger log = LoggerFactory.getLogger(EventCloud.class);

    private static final long serialVersionUID = 1L;

    private final EventCloudId id;

    private final long creationTime;

    private final List<UnalterableElaProperty> elaProperties;

    private final EventCloudDeployer deployer;

    private final String registryUrl;

    private List<String> trackerUrls;

    private List<SemanticTracker> trackers;

    private EventCloud(String registryUrl, EventCloudId eventcloudId,
            EventCloudDeployer deployer,
            List<UnalterableElaProperty> elaProperties, int nbTrackers,
            int nbPeers) {
        this.id = eventcloudId;
        this.creationTime = System.currentTimeMillis();
        this.elaProperties = elaProperties;
        this.deployer = deployer;
        this.registryUrl = registryUrl;
        this.trackerUrls = new ArrayList<String>();
        this.trackers = new ArrayList<SemanticTracker>();

        this.initializeNetwork(nbTrackers, nbPeers);
    }

    private void initializeNetwork(int nbTrackers, int nbPeers) {
        this.deployer.deploy(nbTrackers, nbPeers);

        for (Tracker tracker : this.deployer.getTrackers()) {
            this.trackers.add((SemanticTracker) tracker);
            this.trackerUrls.add(PAActiveObject.getUrl(tracker));
        }
    }

    public static EventCloud create(String registryUrl,
                                    EventCloudDeployer deployer,
                                    List<UnalterableElaProperty> elaProperties,
                                    int nbTrackers, int nbPeers) {
        return create(
                registryUrl, new EventCloudId(), deployer, elaProperties,
                nbTrackers, nbPeers);
    }

    public static EventCloud create(String registryUrl,
                                    EventCloudId eventcloudId,
                                    EventCloudDeployer deployer,
                                    List<UnalterableElaProperty> elaProperties,
                                    int nbTrackers, int nbPeers) {
        return new EventCloud(
                registryUrl, eventcloudId, deployer, elaProperties, nbTrackers,
                nbPeers);
    }

    /**
     * Registers the eventcloud to the {@link EventCloudsRegistry} associated to
     * the eventcloud.
     * 
     * @return {@code true} if the operation succeed, {@code false} if the
     *         operation fails or if the eventcloud is already registered.
     */
    public boolean register() {
        EventCloudsRegistry registry = null;
        try {
            registry = EventCloudsRegistryImpl.lookup(this.registryUrl);

            if (registry.register(this)) {
                log.info(
                        "Eventcloud '{}' registered into registry '{}'",
                        this.id.getStreamUrl(), this.registryUrl);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
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
    public List<UnalterableElaProperty> getElaProperties() {
        return this.elaProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventCloudDeployer getEventCloudDeployer() {
        return this.deployer;
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
    public List<String> getTrackerUrls() {
        return this.trackerUrls;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SemanticTracker> getTrackers() {
        return this.trackers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SemanticTracker selectTracker() {
        // TODO use a metric (e.g. according to the load) to select a tracker
        return this.trackers.get(RandomUtils.nextInt(this.trackers.size()));
    }

}
