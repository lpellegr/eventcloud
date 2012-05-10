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
package fr.inria.eventcloud.factories;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.properties.AlterableElaProperty;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.proxies.EventCloudCache;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.PublishProxyAttributeController;
import fr.inria.eventcloud.proxies.PublishProxyImpl;
import fr.inria.eventcloud.proxies.PutGetProxy;
import fr.inria.eventcloud.proxies.PutGetProxyAttributeController;
import fr.inria.eventcloud.proxies.PutGetProxyImpl;
import fr.inria.eventcloud.proxies.SubscribeProxy;
import fr.inria.eventcloud.proxies.SubscribeProxyAttributeController;
import fr.inria.eventcloud.proxies.SubscribeProxyImpl;

/**
 * ProxyFactory is used to create a new instance of a proxy (e.g.
 * {@link SubscribeProxy}, {@link PublishProxy} or {@link PutGetProxy}). This
 * means that there is at least one instance of a proxy by user. To retrieve a
 * ProxyFactory instance you have to use the
 * {@link ProxyFactory#getInstance(String, EventCloudId)} method. It will return
 * an instance of a ProxyFactory that is specialized to create proxies for the
 * given {@link EventCloudId}. Then, internally, when you create a new proxy,
 * the factory will share the same {@link EventCloudCache} for all the proxies
 * that are created from the retrieved ProxyFactory. Indeed, a proxy only needs
 * trackers (which serves as entry points into the network) to work and these
 * trackers are supposed to stay the same over the time.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class ProxyFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger log;

    protected static final ConcurrentMap<EventCloudId, ProxyFactory> proxies;

    protected static String publishProxyAdl;

    protected static String subscribeProxyAdl;

    protected static String putgetProxyAdl;

    static {
        log = LoggerFactory.getLogger(ProxyFactory.class);

        // proxies may be garbage collected in response to memory demand
        proxies = new MapMaker().softValues().makeMap();

        publishProxyAdl = PublishProxyImpl.PUBLISH_PROXY_ADL;
        subscribeProxyAdl = SubscribeProxyImpl.SUBSCRIBE_PROXY_ADL;
        putgetProxyAdl = PutGetProxyImpl.PUTGET_PROXY_ADL;
    }

    protected EventCloudCache eventCloudProxy;

    /**
     * Constructs a new ProxyFactory from the specified registryUrl and the
     * given EventCloudId.
     * 
     * @param registryUrl
     *            the Event-Cloud registry url.
     * @param id
     *            the identifier that identify the Event-Cloud to work on.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the given id.
     */
    protected ProxyFactory(String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        this.eventCloudProxy = new EventCloudCache(registryUrl, id);
    }

    /**
     * Creates a new publish proxy component deployed on the local JVM.
     * 
     * @return the reference on the {@link PublishProxy} interface of the new
     *         publish proxy component created.
     */
    public PublishProxy newPublishProxy() {
        return this.createPublishProxy(new HashMap<String, Object>());
    }

    /**
     * Creates a new publish proxy component deployed on the specified
     * {@code node}.
     * 
     * @param node
     *            the node to be used for deployment.
     * 
     * @return the reference on the {@link PublishProxy} interface of the new
     *         publish proxy component created.
     */
    public PublishProxy newPublishProxy(Node node) {
        return this.createPublishProxy(ComponentUtils.createContext(node));
    }

    /**
     * Creates a new publish proxy component deployed on the specified
     * {@code GCM virtual node}.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * 
     * @return the reference on the {@link PublishProxy} interface of the new
     *         publish proxy component created.
     */
    public PublishProxy newPublishProxy(GCMVirtualNode vn) {
        return this.createPublishProxy(ComponentUtils.createContext(vn));
    }

    /**
     * Creates a new publish proxy component deployed on a {@code node} provided
     * by the specified GCM application.
     * 
     * @param gcma
     *            the GCM application to be used for deployment.
     * 
     * @return the reference on the {@link PublishProxy} interface of the new
     *         publish proxy component created.
     */
    public PublishProxy newPublishProxy(GCMApplication gcma) {
        return this.createPublishProxy(ComponentUtils.createContext(gcma));
    }

    private PublishProxy createPublishProxy(Map<String, Object> context) {
        try {
            PublishProxy pubProxy =
                    ComponentUtils.createComponentAndGetInterface(
                            publishProxyAdl, context,
                            PublishProxyImpl.PUBLISH_SERVICES_ITF,
                            PublishProxy.class, true);

            ((PublishProxyAttributeController) GCM.getAttributeController(((Interface) pubProxy).getFcItfOwner())).setAttributes(this.eventCloudProxy);

            return pubProxy;
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Creates a new subscribe proxy component deployed on the local JVM and by
     * registering the proxy to the registry in order to have the possibility to
     * receive notification.
     * 
     * @param properties
     *            the ELA properties to set.
     * 
     * @return the reference on the {@link SubscribeProxy} interface of the new
     *         subscribe proxy component created.
     */
    public SubscribeProxy newSubscribeProxy(AlterableElaProperty... properties) {
        return this.createSubscribeProxy(
                new HashMap<String, Object>(), properties);
    }

    /**
     * Creates a new subscribe proxy component deployed on the specified
     * {@code node} and by registering the proxy to the registry in order to
     * have the possibility to receive notification.
     * 
     * @param properties
     *            the ELA properties to set.
     * @param node
     *            the node to be used for deployment.
     * 
     * @return the reference on the {@link SubscribeProxy} interface of the new
     *         subscribe proxy component created.
     */
    public SubscribeProxy newSubscribeProxy(Node node,
                                            AlterableElaProperty... properties) {
        return this.createSubscribeProxy(
                ComponentUtils.createContext(node), properties);
    }

    /**
     * Creates a new subscribe proxy component deployed on the specified
     * {@code GCM virtual node} and by registering the proxy to the registry in
     * order to have the possibility to receive notification.
     * 
     * @param properties
     *            the ELA properties to set.
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * 
     * @return the reference on the {@link SubscribeProxy} interface of the new
     *         subscribe proxy component created.
     */
    public SubscribeProxy newSubscribeProxy(GCMVirtualNode vn,
                                            AlterableElaProperty... properties) {
        return this.createSubscribeProxy(
                ComponentUtils.createContext(vn), properties);
    }

    /**
     * Creates a new subscribe proxy component deployed on a {@code node}
     * provided by the specified GCM application and by registering the proxy to
     * the registry in order to have the possibility to receive notification.
     * 
     * @param properties
     *            the ELA properties to set.
     * @param gcma
     *            the GCM application to be used for deployment.
     * 
     * @return the reference on the {@link SubscribeProxy} interface of the new
     *         subscribe proxy component created.
     */
    public SubscribeProxy newSubscribeProxy(GCMApplication gcma,
                                            AlterableElaProperty... properties) {
        return this.createSubscribeProxy(
                ComponentUtils.createContext(gcma), properties);
    }

    private SubscribeProxy createSubscribeProxy(Map<String, Object> context,
                                                AlterableElaProperty... properties) {
        try {
            SubscribeProxy subProxy =
                    ComponentUtils.createComponentAndGetInterface(
                            subscribeProxyAdl, context,
                            SubscribeProxyImpl.SUBSCRIBE_SERVICES_ITF,
                            SubscribeProxy.class, true);

            Component subComponent = ((Interface) subProxy).getFcItfOwner();

            // registers the subscribe proxy to have the possibility
            // to receive notifications
            String componentUri =
                    Fractive.registerByName(subComponent, "subscribe-proxy-"
                            + UUID.randomUUID().toString());
            log.info("SubscribeProxy bound to {}", componentUri);

            ((SubscribeProxyAttributeController) GCM.getAttributeController(subComponent)).setAttributes(
                    this.eventCloudProxy, componentUri, properties);

            return subProxy;
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        } catch (ProActiveException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Creates a new put/get proxy component deployed on the local JVM.
     * 
     * @return the reference on the {@link PutGetProxy} interface of the new
     *         put/get proxy component created.
     */
    public PutGetProxy newPutGetProxy() {
        return this.createPutGetProxy(new HashMap<String, Object>());
    }

    /**
     * Creates a new put/get proxy component deployed on the specified
     * {@code node}.
     * 
     * @param node
     *            the node to be used for deployment.
     * 
     * @return the reference on the {@link PutGetProxy} interface of the new
     *         put/get proxy component created.
     */
    public PutGetProxy newPutGetProxy(Node node) {
        return this.createPutGetProxy(ComponentUtils.createContext(node));
    }

    /**
     * Creates a new put/get proxy component deployed on the specified
     * {@code GCM virtual node}.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * 
     * @return the reference on the {@link PutGetProxy} interface of the new
     *         put/get proxy component created.
     */
    public PutGetProxy newPutGetProxy(GCMVirtualNode vn) {
        return this.createPutGetProxy(ComponentUtils.createContext(vn));
    }

    /**
     * Creates a new put/get proxy component deployed on a {@code node} provided
     * by the specified GCM application.
     * 
     * @param gcma
     *            the GCM application to be used for deployment.
     * 
     * @return the reference on the {@link PutGetProxy} interface of the new
     *         put/get proxy component created.
     */
    public PutGetProxy newPutGetProxy(GCMApplication gcma) {
        return this.createPutGetProxy(ComponentUtils.createContext(gcma));
    }

    private PutGetProxy createPutGetProxy(Map<String, Object> context) {
        try {
            PutGetProxy putgetProxy =
                    ComponentUtils.createComponentAndGetInterface(
                            putgetProxyAdl, context,
                            PutGetProxyImpl.PUTGET_SERVICES_ITF,
                            PutGetProxy.class, true);

            ((PutGetProxyAttributeController) GCM.getAttributeController(((Interface) putgetProxy).getFcItfOwner())).setAttributes(this.eventCloudProxy);

            return putgetProxy;
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        }
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
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the given id.
     */
    public static ProxyFactory getInstance(String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        ProxyFactory newFactory = proxies.get(id);

        if (newFactory == null) {
            newFactory = new ProxyFactory(registryUrl, id);

            ProxyFactory oldFactory = proxies.putIfAbsent(id, newFactory);

            if (oldFactory == null) {
                return newFactory;
            } else {
                return oldFactory;
            }
        }

        return newFactory;
    }

    /**
     * Removes all entries from the cache.
     */
    public static void clear() {
        proxies.clear();
    }

}
