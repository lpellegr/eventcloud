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
package fr.inria.eventcloud.api;

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;

import fr.inria.eventcloud.proxy.EventCloudProxy;
import fr.inria.eventcloud.proxy.PublishSubscribeProxy;
import fr.inria.eventcloud.proxy.PutGetProxy;

/**
 * ProxyFactory is used to create a new instance of a proxy (e.g.
 * {@link PublishSubscribeProxy} or {@link PutGetProxy}). This means that there
 * is at least one instance of a proxy by user. To retrieve a ProxyFactory
 * instance you have to use the {@link #getInstance(URL, EventCloudId)} method.
 * It will return an instance of a ProxyFactory that is specialized to create
 * proxies for the given {@link EventCloudId}. Then, internally, when you create
 * a new proxy, the factory will share the same {@link EventCloudProxy} for all
 * the proxies that are created from the retrieved ProxyFactory. Indeed, a proxy
 * only needs trackers (which serves as entry points into the network) to work
 * and these trackers are supposed to stay the same over the time.
 * 
 * @author lpellegr
 */
public final class ProxyFactory {

    private static final ConcurrentMap<EventCloudId, ProxyFactory> proxies;

    static {
        proxies = new ConcurrentHashMap<EventCloudId, ProxyFactory>();
    }

    private EventCloudProxy eventCloudProxy;

    /**
     * Constructs a new ProxyFactory from the specified registryUrl and the
     * given EventCloudId.
     * 
     * @param registryUrl
     *            the Event-Cloud registry url.
     * @param id
     *            the identifier that identify the Event-Cloud to work on.
     */
    private ProxyFactory(String registryUrl, EventCloudId id) {
        this.eventCloudProxy = new EventCloudProxy(registryUrl, id);
    }

    /**
     * Creates a new {@link PublishSubscribeProxy}.
     * 
     * @return a new {@link PublishSubscribeProxy}.
     */
    public PublishSubscribeProxy createPublishSubscribeProxy() {
        try {
            return PAActiveObject.newActive(
                    PublishSubscribeProxy.class,
                    new Object[] {this.eventCloudProxy});
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Creates a new {@link PutGetProxy}.
     * 
     * @return a new {@link PutGetProxy}.
     */
    public PutGetProxy createPutGetProxy() {
        return new PutGetProxy(this.eventCloudProxy);
    }

    /**
     * Returns an instance of a ProxyFactory that is specialized to create
     * proxies for the given {@link EventCloudId}.
     * 
     * @param registryUrl
     *            the Event-Cloud registry url.
     * @param id
     *            the {@link EventCloudId} to use.
     * 
     * @return an instance of a ProxyFactory that is specialized to create
     *         proxies for the given {@link EventCloudId}.
     */
    public static ProxyFactory getInstance(String registryUrl, EventCloudId id) {
        ProxyFactory newFactory = new ProxyFactory(registryUrl, id);
        
        ProxyFactory oldFactory = proxies.putIfAbsent(id, newFactory);
        
        if (oldFactory == null) {
            return newFactory;
        } else {
            return oldFactory;
        }
    }

}
