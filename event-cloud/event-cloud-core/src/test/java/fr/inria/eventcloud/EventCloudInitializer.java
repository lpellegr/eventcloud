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
import fr.inria.eventcloud.api.properties.UnalterableElaProperty;

/**
 * A network initializer is used to setup an Event Cloud.
 * 
 * @author lpellegr
 */
public class EventCloudInitializer {

    private EventCloudsRegistry eventCloudsRegistry;

    private EventCloud eventCloud;

    public EventCloudInitializer() {
    }

    public void setUp(int nbTrackers, int nbPeers) {
        this.eventCloudsRegistry =
                EventCloudsRegistryFactory.newEventCloudsRegistry();

        this.eventCloud =
                EventCloud.create(
                        PAActiveObject.getUrl(this.eventCloudsRegistry),
                        "http://node.provider.not.yet.available",
                        new Collection<UnalterableElaProperty>(), nbTrackers,
                        nbPeers);

        this.eventCloudsRegistry.register(this.eventCloud);
    }

    public void tearDown() {
        // TODO: provide method to close (i.e. to unbind active objects from the
        // rmi registry, etc.)
    }

    public EventCloudsRegistry getEventCloudsRegistry() {
        return this.eventCloudsRegistry;
    }

    public String getEventCloudRegistryUrl() {
        return PAActiveObject.getUrl(this.eventCloudsRegistry);
    }

    public EventCloud getEventCloud() {
        return this.eventCloud;
    }

}
