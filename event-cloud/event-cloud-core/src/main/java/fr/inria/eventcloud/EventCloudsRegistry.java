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
 * An eventclouds registry is in charge of maintaining the list of eventclouds
 * which are running for an organization or a group. In addition, for each
 * eventcloud which is managed, the registry also have to store the entry points
 * associated to an eventcloud.
 * <p>
 * <strong>This first prototype is centralized and stores the information in
 * memory.</strong>
 * 
 * @author lpellegr
 */
public class EventCloudsRegistry {

    private Map<EventCloudId, Collection<SemanticTracker>> eventclouds;

    /**
     * Empty constructor commanded by ProActive to expose this object as an
     * active object.
     */
    public EventCloudsRegistry() {
        this.eventclouds =
                new HashMap<EventCloudId, Collection<SemanticTracker>>();
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
            return this.eventclouds.put(
                    eventcloud.getId(), eventcloud.getTrackers()) == null;
        }
    }

    /**
     * Returns a list that contains the identifier of the eventclouds which are
     * managed by the registry.
     * 
     * @return a list that contains the identifier of the eventclouds which are
     *         managed by the registry.
     */
    public Collection<EventCloudId> listEventClouds() {
        return new Collection<EventCloudId>(this.eventclouds.keySet());
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
     * Returns the trackers associated to the specified {@link EventCloudId} if
     * it is registered in the registry or {@code null}.
     * 
     * @param id
     *            the Event Cloud identifier to look for.
     * 
     * @return the trackers associated to the eventcloud identified by the
     *         specified {@link EventCloudId} or {@code null}.
     */
    public Collection<SemanticTracker> findTrackers(EventCloudId id) {
        return this.eventclouds.get(id);
    }

}
