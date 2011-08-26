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

import java.util.HashMap;
import java.util.Map;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * The EventCloudRegistry is in charge of storing all the information related to
 * the Event-Clouds which are runnings for an organization or a group.
 * <p>
 * <strong>As a first prototype the registry is centralized and stores the
 * information in memory.</strong>
 * <p>
 * TODO: the registry has to be distributed and the data that are saved have to
 * be persisted.
 * 
 * @author lpellegr
 */
public class EventCloudsRegistry {

    private Map<EventCloudId, Collection<SemanticTracker>> eventClouds;

    /**
     * Empty constructor commanded by ProActive to expose this object as an
     * active object.
     */
    public EventCloudsRegistry() {
        this.eventClouds =
                new HashMap<EventCloudId, Collection<SemanticTracker>>();
    }

    /**
     * Registers the given {@link EventCloud} into the registry.
     * 
     * @param eventCloud
     *            the EventCloud to register into the registry.
     */
    public void register(EventCloud eventCloud) {
        if (this.eventClouds.containsKey(eventCloud.getId())) {
            throw new IllegalArgumentException("Event with id '"
                    + eventCloud.getId() + "' already registered");
        } else {
            this.eventClouds.put(eventCloud.getId(), eventCloud.getTrackers());
        }
    }

    /**
     * Returns the list of the Event Clouds that are managed by the registry.
     * 
     * @return the list of the Event Clouds that are managed by the registry.
     */
    public Collection<EventCloudId> listEventClouds() {
        return new Collection<EventCloudId>(this.eventClouds.keySet());
    }

    /**
     * Returns the trackers associated to the specified {@link EventCloudId} if
     * it is registered in the registry or {@code null}.
     * 
     * @param id
     *            the Event Cloud identifier to look for.
     * 
     * @return the trackers associated to the specified {@link EventCloudId} if
     *         it is registered in the registry or {@code null}.
     */
    public Collection<SemanticTracker> findTrackers(EventCloudId id) {
        return this.eventClouds.get(id);
    }

}
