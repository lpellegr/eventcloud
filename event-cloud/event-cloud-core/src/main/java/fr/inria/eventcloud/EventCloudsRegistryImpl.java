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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.body.ComponentInitActive;

import com.google.common.collect.ImmutableSet;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * EventCloudsRegistryImpl is a concrete implementation of
 * {@link EventCloudsRegistry}. This class has to be instantiated as a
 * ProActive/GCM component.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class EventCloudsRegistryImpl implements EventCloudsRegistry,
        ComponentInitActive {

    private Map<EventCloudId, EventCloud> eventclouds;

    /**
     * No-arg constructor for ProActive.
     */
    public EventCloudsRegistryImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponentActivity(Body body) {
        this.eventclouds = new HashMap<EventCloudId, EventCloud>();
    }

    /**
     * Registers the given {@link EventCloud} into the registry.
     * 
     * @param eventcloud
     *            the eventcloud to register into the registry.
     * 
     * @return {@code true} if the registration has succeed or {@code false} if
     *         the eventcloud is already registered into the registry.
     */
    public boolean register(EventCloud eventcloud) {
        if (this.eventclouds.containsKey(eventcloud.getId())) {
            return false;
        } else {
            return this.eventclouds.put(eventcloud.getId(), eventcloud) == null;
        }
    }

    /**
     * Returns a list that contains the identifier of the eventclouds which are
     * managed by the registry.
     * 
     * @return a list that contains the identifier of the eventclouds which are
     *         managed by the registry.
     */
    public Set<EventCloudId> listEventClouds() {
        // returns an immutable copy because this.eventclouds.keySet() sends
        // back a non-serializable set
        return ImmutableSet.copyOf(this.eventclouds.keySet());

    }

    /**
     * Returns a boolean which indicates if the eventcloud identified by the
     * specified {@code eventcloudId} is already managed by the registry.
     * 
     * @param id
     *            the eventcloud identifier to check for.
     * 
     * @return {@code true} if the eventcloud identifier is already managed,
     *         {@code false} otherwise.
     */
    public boolean contains(EventCloudId id) {
        return this.eventclouds.containsKey(id);
    }

    /**
     * Returns the {@link EventCloud} object associated to the specified
     * {@code id} if it is managed by the registry.
     * 
     * @param id
     *            the eventcloud identifier to look for.
     * 
     * @return the {@link EventCloud} object associated to the specified
     *         {@code id} if it is managed by the registry or {@code null}.
     */
    public EventCloud find(EventCloudId id) {
        return this.eventclouds.get(id);
    }

    /**
     * Returns the trackers associated to the specified {@link EventCloudId} if
     * it is registered in the registry or {@code null}.
     * 
     * @param id
     *            the Event Cloud identifier to look for.
     * 
     * @return the trackers associated to the eventcloud identified by the
     *         specified {@link EventCloudId} or {@code null}.
     */
    public List<SemanticTracker> findTrackers(EventCloudId id) {
        return this.eventclouds.get(id).getTrackers();
    }

}
