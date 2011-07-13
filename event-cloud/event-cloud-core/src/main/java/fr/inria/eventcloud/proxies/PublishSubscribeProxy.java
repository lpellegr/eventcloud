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

import java.io.Serializable;

import fr.inria.eventcloud.api.PublishSubscribeApi;

/**
 * A PublishSubscribeProxy is a proxy that implements the
 * {@link PublishSubscribeApi}. It has to be used by a user who wants to execute
 * publish/subscribe asynchronous operations on an Event-Cloud.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public interface PublishSubscribeProxy extends PublishSubscribeApi,
        Serializable {

    /**
     * The init method is a convenient method for components which is used to
     * initialize the {@link EventCloudProxy}. Once this method is called and
     * the value is set, the next calls perform no action.
     * 
     * @param proxy
     *            the event cloud proxy instance to set to the publish subscribe
     *            proxy.
     */
    public void init(EventCloudProxy proxy);

}
