/**
 * Copyright (c) 2011-2013 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.factories;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.factories.AbstractFactory;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.SubscribeApi;
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
 * {@link SubscribeProxy}, {@link PublishProxy} or {@link PutGetProxy}).
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class ProxyFactory extends AbstractFactory {

    private static final Logger log;

    protected static String publishProxyAdl;

    protected static String subscribeProxyAdl;

    protected static String putgetProxyAdl;

    static {
        log = LoggerFactory.getLogger(ProxyFactory.class);

        publishProxyAdl = PublishProxyImpl.PUBLISH_PROXY_ADL;
        subscribeProxyAdl = SubscribeProxyImpl.SUBSCRIBE_PROXY_ADL;
        putgetProxyAdl = PutGetProxyImpl.PUTGET_PROXY_ADL;
    }

    /**
     * Creates a new publish proxy component deployed on the local JVM.
     * 
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PublishApi} interface of the new
     *         publish proxy component created.
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
     * Creates a new publish proxy component deployed on the specified
     * {@code node}.
     * 
     * @param node
     *            the node to be used for deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PublishApi} interface of the new
     *         publish proxy component created.
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
     * Creates a new publish proxy component deployed on the specified
     * {@code GCM virtual node}.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PublishApi} interface of the new
     *         publish proxy component created.
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
     * Creates a new publish proxy component deployed on a node provided by the
     * specified {@code node provider}.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PublishApi} interface of the new
     *         publish proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the given id.
     */
    public static PublishApi newPublishProxy(NodeProvider nodeProvider,
                                             String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return createPublishProxy(publishProxyAdl, getContextFromNodeProvider(
                nodeProvider, PublishProxyImpl.PROXY_VN), registryUrl, id);
    }

    protected static <T extends PublishProxy> PublishApi createPublishProxy(String publishProxyAdl,
                                                                            Map<String, Object> context,
                                                                            String registryUrl,
                                                                            EventCloudId id)
            throws EventCloudIdNotManaged {
        return createPublishProxy(
                publishProxyAdl, PublishProxy.class, context, registryUrl, id);
    }

    protected static <T extends PublishProxy> PublishApi createPublishProxy(String publishProxyAdl,
                                                                            Class<T> interfaceClass,
                                                                            Map<String, Object> context,
                                                                            String registryUrl,
                                                                            EventCloudId id)
            throws EventCloudIdNotManaged {
        checkNotNull(registryUrl, id);

        try {
            T pubProxy =
                    ComponentUtils.createComponentAndGetInterface(
                            publishProxyAdl, context,
                            PublishProxyImpl.PUBLISH_SERVICES_ITF,
                            interfaceClass, true);

            EventCloudCache eventCloudProxy =
                    new EventCloudCache(registryUrl, id);
            ((PublishProxyAttributeController) GCM.getAttributeController(((Interface) pubProxy).getFcItfOwner())).setAttributes(eventCloudProxy);
            eventCloudProxy.getRegistry().registerProxy(
                    eventCloudProxy.getId(), pubProxy);

            return pubProxy;
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Lookups a publish proxy component on the specified {@code componentUri}.
     * 
     * @param componentUri
     *            the URL of the publish proxy component.
     * 
     * @return the reference on the {@link PublishApi} interface of the publish
     *         proxy component.
     * 
     * @throws IOException
     *             if an error occurs during the construction of the stub.
     */
    public static PublishApi lookupPublishProxy(String componentUri)
            throws IOException {
        return ComponentUtils.lookupFcInterface(
                componentUri, PublishProxyImpl.PUBLISH_SERVICES_ITF,
                PublishApi.class);
    }

    /**
     * Creates a new subscribe proxy component deployed on the local JVM and by
     * registering the proxy to the registry in order to have the possibility to
     * receive notification.
     * 
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * @param properties
     *            the ELA properties to set.
     * 
     * @return the reference on the {@link SubscribeApi} interface of the new
     *         subscribe proxy component created.
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
     * Creates a new subscribe proxy component deployed on the specified
     * {@code node} and by registering the proxy to the registry in order to
     * have the possibility to receive notification.
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
     *         subscribe proxy component created.
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
     * Creates a new subscribe proxy component deployed on the specified
     * {@code GCM virtual node} and by registering the proxy to the registry in
     * order to have the possibility to receive notification.
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
     *         subscribe proxy component created.
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
     * Creates a new subscribe proxy component deployed on a node provided by
     * the specified {@code node provider} and by registering the proxy to the
     * registry in order to have the possibility to receive notification.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * @param properties
     *            the ELA properties to set.
     * 
     * @return the reference on the {@link SubscribeApi} interface of the new
     *         subscribe proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the given id.
     */
    public static SubscribeApi newSubscribeProxy(NodeProvider nodeProvider,
                                                 String registryUrl,
                                                 EventCloudId id,
                                                 AlterableElaProperty... properties)
            throws EventCloudIdNotManaged {
        return createSubscribeProxy(
                subscribeProxyAdl, getContextFromNodeProvider(
                        nodeProvider, SubscribeProxyImpl.PROXY_VN),
                registryUrl, id, properties);
    }

    protected static SubscribeApi createSubscribeProxy(String subscribeProxyAdl,
                                                       Map<String, Object> context,
                                                       String registryUrl,
                                                       EventCloudId id,
                                                       AlterableElaProperty... properties)
            throws EventCloudIdNotManaged {
        checkNotNull(registryUrl, id);

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

            EventCloudCache eventCloudProxy =
                    new EventCloudCache(registryUrl, id);
            ((SubscribeProxyAttributeController) GCM.getAttributeController(subComponent)).setAttributes(
                    eventCloudProxy, componentUri, properties);
            eventCloudProxy.getRegistry().registerProxy(id, subProxy);

            return subProxy;
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        } catch (ProActiveException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Lookups a subscribe proxy component on the specified {@code componentUri}
     * .
     * 
     * @param componentUri
     *            the URL of the subscribe proxy component.
     * 
     * @return the reference on the {@link SubscribeApi} interface of the
     *         subscribe proxy component.
     * 
     * @throws IOException
     *             if an error occurs during the construction of the stub.
     */
    public static SubscribeApi lookupSubscribeProxy(String componentUri)
            throws IOException {
        return ComponentUtils.lookupFcInterface(
                componentUri, SubscribeProxyImpl.SUBSCRIBE_SERVICES_ITF,
                SubscribeApi.class);
    }

    /**
     * Creates a new put/get proxy component deployed on the local JVM.
     * 
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PutGetApi} interface of the new
     *         put/get proxy component created.
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
     * Creates a new put/get proxy component deployed on the specified
     * {@code node}.
     * 
     * @param node
     *            the node to be used for deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PutGetApi} interface of the new
     *         put/get proxy component created.
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
     * Creates a new put/get proxy component deployed on the specified
     * {@code GCM virtual node}.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PutGetApi} interface of the new
     *         put/get proxy component created.
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

    /**
     * Creates a new put/get proxy component deployed on a node provided by the
     * specified {@code node provider}.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PutGetApi} interface of the new
     *         put/get proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the given id.
     */
    public static PutGetApi newPutGetProxy(NodeProvider nodeProvider,
                                           String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return createPutGetProxy(putgetProxyAdl, getContextFromNodeProvider(
                nodeProvider, PutGetProxyImpl.PROXY_VN), registryUrl, id);
    }

    protected static PutGetApi createPutGetProxy(String putgetProxyAdl,
                                                 Map<String, Object> context,
                                                 String registryUrl,
                                                 EventCloudId id)
            throws EventCloudIdNotManaged {
        checkNotNull(registryUrl, id);

        try {
            PutGetProxy putgetProxy =
                    ComponentUtils.createComponentAndGetInterface(
                            putgetProxyAdl, context,
                            PutGetProxyImpl.PUTGET_SERVICES_ITF,
                            PutGetProxy.class, true);

            EventCloudCache eventCloudProxy =
                    new EventCloudCache(registryUrl, id);
            ((PutGetProxyAttributeController) GCM.getAttributeController(((Interface) putgetProxy).getFcItfOwner())).setAttributes(eventCloudProxy);
            eventCloudProxy.getRegistry().registerProxy(id, putgetProxy);

            return putgetProxy;
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Lookups a put/get proxy component on the specified {@code componentUri}.
     * 
     * @param componentUri
     *            the URL of the put/get proxy component.
     * 
     * @return the reference on the {@link PutGetApi} interface of the put/get
     *         proxy component.
     * 
     * @throws IOException
     *             if an error occurs during the construction of the stub.
     */
    public static PutGetApi lookupPutGetProxy(String componentUri)
            throws IOException {
        return ComponentUtils.lookupFcInterface(
                componentUri, PutGetProxyImpl.PUTGET_SERVICES_ITF,
                PutGetApi.class);
    }

    private static void checkNotNull(String registryUrl, EventCloudId id) {
        Preconditions.checkNotNull(registryUrl, "Invalid registry URL: "
                + registryUrl);
        Preconditions.checkNotNull(id, "Invalid EventCloud id: " + id);
    }

}
