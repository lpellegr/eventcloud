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

import org.objectweb.proactive.api.PAActiveObject;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * The EventCloudRegistry is in charge of storing all the information related to
 * the Event-Clouds which are runnings for an organization or a group.
 * <p>
 * As a first prototype the registry is centralized and stores the information
 * as {@link Quadruple}s.
 * 
 * @author lpellegr
 */
public class EventCloudsRegistry {

    // TODO: temporary fields that have to be replaced by an RDF repository.
    private EventCloudId id;

    private Collection<SemanticTracker> trackers;

    public EventCloudId getId() {
        return this.id;
    }

    public Collection<SemanticTracker> getTrackers() {
        return this.trackers;
    }

    /**
     * Empty constructor commanded by ProActive to expose this object as an
     * active object.
     */
    public EventCloudsRegistry() {
    }

    /**
     * Registers the given {@link EventCloud} into the registry.
     * 
     * @param eventCloud
     *            the EventCloud to register into the registry.
     */
    public void register(EventCloud eventCloud) {
        this.id = eventCloud.getId();
        this.trackers = eventCloud.getTrackers();
    }

    /**
     * Returns the URL to which the active object is bind.
     * 
     * @return the URL to which the active object is bind.
     */
    public String getUrl() {
        return PAActiveObject.getUrl(PAActiveObject.getStubOnThis());
    }

    /**
     * Returns the list of the Event Clouds that are managed by the registry.
     * 
     * @return the list of the Event Clouds that are managed by the registry.
     */
    public Collection<EventCloudId> listEventClouds() {
        // TODO: implement
        return null;
    }

}
