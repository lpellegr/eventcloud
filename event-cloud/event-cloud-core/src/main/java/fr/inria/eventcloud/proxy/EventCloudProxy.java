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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.proxy;

import java.net.URL;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.EventCloudApi;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.EventCloudsRegistry;
import fr.inria.eventcloud.api.ProxyFactory;
import fr.inria.eventcloud.api.properties.UnalterableElaProperty;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * An EventCloudProxy is used to keep in cache the information that are exposed
 * by the {@link EventCloudApi}. This is done to reduce the number of calls to
 * the {@link EventCloudsRegistry}.
 * 
 * @author lpellegr
 * 
 * @see ProxyFactory
 */
public class EventCloudProxy implements EventCloudApi {

    public EventCloudProxy(URL registryUrl, EventCloudId id) {
        // TODO throw an exception if the registry identified by registryUrl
        // does not contain the specified EventCloudId
    }

    @Override
    public EventCloudId getId() {
        // TODO Auto-generated method stub
        return null;
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
    public URL getNodeProviderUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public URL getRegistryUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<URL> getTrackerUrls() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<SemanticTracker> getTrackers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SemanticTracker selectTracker() {
        // TODO Auto-generated method stub
        return null;
    }

}
