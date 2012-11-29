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
package fr.inria.eventcloud.webservices.factories;

import java.util.HashMap;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.properties.AlterableElaProperty;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.webservices.proxies.PublishWsProxyImpl;
import fr.inria.eventcloud.webservices.proxies.PutGetWsProxyImpl;
import fr.inria.eventcloud.webservices.proxies.SubscribeWsProxyImpl;

/**
 * WsProxyFactory is used to create a new instance of a proxy component (e.g.
 * subscribe proxy component, publish proxy component or put/get proxy
 * component) that can be exposed as a web service.
 * 
 * @author bsauvan
 */
public final class WsProxyFactory extends ProxyFactory {

    private static final long serialVersionUID = 130L;

    static {
        publishProxyAdl = PublishWsProxyImpl.PUBLISH_WEBSERVICE_PROXY_ADL;
        subscribeProxyAdl = SubscribeWsProxyImpl.SUBSCRIBE_WEBSERVICE_PROXY_ADL;
        putgetProxyAdl = PutGetWsProxyImpl.PUTGET_WEBSERVICE_PROXY_ADL;
    }

    /**
     * Creates a new publish web service proxy component deployed on the local
     * JVM.
     * 
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PublishApi} interface of the new
     *         publish web service proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the given id.
     */
    public static PublishApi newPublishProxy(String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return createPublishProxy(
                publishProxyAdl, new HashMap<String, Object>(), registryUrl, id);
    }

    /**
     * Creates a new publish web service proxy component deployed on the
     * specified {@code node}.
     * 
     * @param node
     *            the node to be used for deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PublishApi} interface of the new
     *         publish web service proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the given id.
     */
    public static PublishApi newPublishProxy(Node node, String registryUrl,
                                             EventCloudId id)
            throws EventCloudIdNotManaged {
        return createPublishProxy(
                publishProxyAdl, ComponentUtils.createContext(node),
                registryUrl, id);
    }

    /**
     * Creates a new publish web service proxy component deployed on the
     * specified {@code GCM virtual node}.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PublishApi} interface of the new
     *         publish web service proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the given id.
     */
    public static PublishApi newPublishProxy(GCMVirtualNode vn,
                                             String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return createPublishProxy(
                publishProxyAdl, ComponentUtils.createContext(vn), registryUrl,
                id);
    }

    /**
     * Creates a new subscribe web service proxy component deployed on the local
     * JVM and by registering the proxy to the registry in order to have the
     * possibility to receive notification.
     * 
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * @param properties
     *            the ELA properties to set.
     * 
     * @return the reference on the {@link SubscribeApi} interface of the new
     *         subscribe web service proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the given id.
     */
    public static SubscribeApi newSubscribeProxy(String registryUrl,
                                                 EventCloudId id,
                                                 AlterableElaProperty... properties)
            throws EventCloudIdNotManaged {
        return createSubscribeProxy(
                subscribeProxyAdl, new HashMap<String, Object>(), registryUrl,
                id, properties);
    }

    /**
     * Creates a new subscribe web service proxy component deployed on the
     * specified {@code node} and by registering the proxy to the registry in
     * order to have the possibility to receive notification.
     * 
     * @param node
     *            the node to be used for deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * @param properties
     *            the ELA properties to set.
     * 
     * @return the reference on the {@link SubscribeApi} interface of the new
     *         subscribe web service proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the given id.
     */
    public static SubscribeApi newSubscribeProxy(Node node,
                                                 String registryUrl,
                                                 EventCloudId id,
                                                 AlterableElaProperty... properties)
            throws EventCloudIdNotManaged {
        return createSubscribeProxy(
                subscribeProxyAdl, ComponentUtils.createContext(node),
                registryUrl, id, properties);
    }

    /**
     * Creates a new subscribe web service proxy component deployed on the
     * specified {@code GCM virtual node} and by registering the proxy to the
     * registry in order to have the possibility to receive notification.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * @param properties
     *            the ELA properties to set.
     * 
     * @return the reference on the {@link SubscribeApi} interface of the new
     *         subscribe web service proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the given id.
     */
    public static SubscribeApi newSubscribeProxy(GCMVirtualNode vn,
                                                 String registryUrl,
                                                 EventCloudId id,
                                                 AlterableElaProperty... properties)
            throws EventCloudIdNotManaged {
        return createSubscribeProxy(
                subscribeProxyAdl, ComponentUtils.createContext(vn),
                registryUrl, id, properties);
    }

    /**
     * Creates a new put/get web service proxy component deployed on the local
     * JVM.
     * 
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PutGetApi} interface of the new
     *         put/get web service proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the given id.
     */
    public static PutGetApi newPutGetProxy(String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return createPutGetProxy(
                putgetProxyAdl, new HashMap<String, Object>(), registryUrl, id);
    }

    /**
     * Creates a new put/get web service proxy component deployed on the
     * specified {@code node}.
     * 
     * @param node
     *            the node to be used for deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PutGetApi} interface of the new
     *         put/get web service proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the given id.
     */
    public static PutGetApi newPutGetProxy(Node node, String registryUrl,
                                           EventCloudId id)
            throws EventCloudIdNotManaged {
        return createPutGetProxy(
                putgetProxyAdl, ComponentUtils.createContext(node),
                registryUrl, id);
    }

    /**
     * Creates a new put/get web service proxy component deployed on the
     * specified {@code GCM virtual node}.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PutGetApi} interface of the new
     *         put/get web service proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the given id.
     */
    public static PutGetApi newPutGetProxy(GCMVirtualNode vn,
                                           String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return createPutGetProxy(
                putgetProxyAdl, ComponentUtils.createContext(vn), registryUrl,
                id);
    }

}
