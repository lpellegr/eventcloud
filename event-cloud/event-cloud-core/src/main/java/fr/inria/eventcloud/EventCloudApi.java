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

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.properties.UnalterableElaProperty;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * Defines the properties that are accessible for an Event-Cloud.
 * 
 * @author lpellegr
 */
public interface EventCloudApi {

    /**
     * Returns the {@link EventCloudId} that uniquely identifies the
     * Event-Cloud.
     * 
     * @return the {@link EventCloudId} that uniquely identifies the
     *         Event-Cloud.
     */
    public EventCloudId getId();

    /**
     * Returns the Event-Cloud creation date.
     * 
     * @return the Event-Cloud creation date.
     */
    public long getCreationTime();

    /**
     * Returns the list of ELA properties that have been set when the
     * Event-Cloud has been created. These ELA properties are not modifiable.
     * 
     * @return the list of ELA properties that have been set when the
     *         Event-Cloud has been created.
     */
    public Collection<UnalterableElaProperty> getElaProperties();

    /**
     * Returns the URL to the node provider that is used to retrieve the nodes
     * for the deployment of the trackers and the peers.
     * 
     * @return the URL to the node provider that is used to retrieve the nodes
     *         for the deployment of the trackers and the peers.
     */
    public String getNodeProviderUrl();

    /**
     * Returns the URL to the EventCloudsRegistry.
     * 
     * @return the URL to the EventCloudsRegistry.
     */
    public String getRegistryUrl();

    /**
     * Returns the URL to the trackers that are binded to the Event-Cloud.
     * 
     * @return the URL to the trackers that are binded to the Event-Cloud.
     */
    public Collection<String> getTrackerUrls();

    /**
     * Returns a collection of tracker active objects that are binded to the
     * Event-Cloud.
     * 
     * @return a collection of tracker active objects that are binded to the
     *         Event-Cloud.
     */
    public Collection<SemanticTracker> getTrackers();

    /**
     * Returns a tracker which is selected among the collection of trackers
     * according to a criterion. This criterion may be a metric or not.
     * 
     * @return a tracker which is selected among the collection of trackers
     *         according to a criterion.
     */
    public SemanticTracker selectTracker();

}
