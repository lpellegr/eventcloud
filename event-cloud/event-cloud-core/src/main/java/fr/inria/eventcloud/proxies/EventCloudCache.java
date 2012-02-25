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

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;

import fr.inria.eventcloud.EventCloud;
import fr.inria.eventcloud.EventCloudApi;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.properties.UnalterableElaProperty;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * This class is used to keep in cache the information associated to an
 * {@link EventCloud} in order to reduce the number of calls to an
 * {@link EventCloudsRegistry}.
 * 
 * @author lpellegr
 * 
 * @see ProxyFactory
 */
public class EventCloudCache implements EventCloudApi, Serializable {

    private static final long serialVersionUID = 1L;

    private final EventCloudsRegistry registry;

    private EventCloud delegate;

    /*
     * TODO: add a thread that updates periodically the trackers.
     */

    public EventCloudCache(String registryUrl, EventCloudId eventcloudId) {
        try {
            this.registry =
                    PAActiveObject.lookupActive(
                            EventCloudsRegistry.class, registryUrl);

            this.delegate = this.registry.find(eventcloudId);
        } catch (ActiveObjectCreationException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public EventCloudId getId() {
        return this.delegate.getId();
    }

    public long getCreationTime() {
        return this.delegate.getCreationTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventCloudDeployer getEventCloudDeployer() {
        return this.delegate.getEventCloudDeployer();
    }

    public List<UnalterableElaProperty> getElaProperties() {
        return this.delegate.getElaProperties();
    }

    public String getRegistryUrl() {
        return this.delegate.getRegistryUrl();
    }

    public List<String> getTrackerUrls() {
        return this.delegate.getTrackerUrls();
    }

    public List<SemanticTracker> getTrackers() {
        return this.delegate.getTrackers();
    }

    public SemanticTracker selectTracker() {
        return this.delegate.selectTracker();
    }

}
