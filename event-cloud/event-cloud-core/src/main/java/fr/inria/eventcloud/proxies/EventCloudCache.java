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
package fr.inria.eventcloud.proxies;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.ProActiveRandom;

import fr.inria.eventcloud.EventCloudApi;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.properties.UnalterableElaProperty;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * An EventCloudCache is used to keep in cache the information that are exposed
 * by the {@link EventCloudApi}. This is done to reduce the number of calls to
 * the {@link EventCloudsRegistry}.
 * 
 * @author lpellegr
 * 
 * @see ProxyFactory
 */
public class EventCloudCache implements EventCloudApi, Serializable {

    private static final long serialVersionUID = 1L;

    private EventCloudId id;

    private List<SemanticTracker> trackers;

    public EventCloudCache(String registryUrl, EventCloudId eventcloudId) {
        this.id = eventcloudId;

        try {
            Collection<SemanticTracker> trackersReceived =
                    PAActiveObject.lookupActive(
                            EventCloudsRegistry.class, registryUrl)
                            .findTrackers(eventcloudId);
            if (trackersReceived == null) {
                throw new IllegalArgumentException(
                        "Registry does not manage an eventcloud identified by: "
                                + eventcloudId);
            } else {
                this.trackers = new ArrayList<SemanticTracker>();
                this.trackers.addAll(trackersReceived);
            }
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
    public EventCloudId getId() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCreationTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<UnalterableElaProperty> getElaProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventCloudDeployer getEventCloudDeployer() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRegistryUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getTrackerUrls() {
        // TODO Auto-generated method stub
        return null;
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
        return this.trackers.get(ProActiveRandom.nextInt(this.trackers.size()));
    }

}
