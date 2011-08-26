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

    public EventCloudCache(String registryUrl, EventCloudId id) {
        // TODO throw an exception if the registry identified by registryUrl
        // does not contain the specified EventCloudId
        this.id = id;

        try {
            this.trackers = new ArrayList<SemanticTracker>();
            this.trackers.addAll(PAActiveObject.lookupActive(
                    EventCloudsRegistry.class, registryUrl).findTrackers(id));
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventCloudId getId() {
        return this.id;
    }

    @Override
    public long getCreationTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Collection<UnalterableElaProperty> getElaProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getNodeProviderUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRegistryUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> getTrackerUrls() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<SemanticTracker> getTrackers() {
        return new Collection<SemanticTracker>(this.trackers);
    }

    @Override
    public SemanticTracker selectTracker() {
        return this.trackers.get(ProActiveRandom.nextInt(this.trackers.size()));
    }

}