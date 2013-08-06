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
import org.objectweb.proactive.extensions.p2p.structured.deployment.DeploymentConfiguration;
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
     *             if the specified registry does not managed the specified id.
     */
    public static PublishApi newPublishProxy(String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return ProxyFactory.createPublishProxy(
                publishProxyAdl, new HashMap<String, Object>(), null,
                registryUrl, id);
    }

    /**
     * Creates a new publish proxy component deployed on the local JVM and by
     * using the specified deployment configuration.
     * 
     * @param deploymentConfiguration
     *            the deployment configuration to use during the deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PublishApi} interface of the new
     *         publish proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the specified id.
     */
    public static PublishApi newPublishProxy(DeploymentConfiguration deploymentConfiguration,
                                             String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return ProxyFactory.createPublishProxy(
                publishProxyAdl, new HashMap<String, Object>(),
                deploymentConfiguration, registryUrl, id);
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
     *             if the specified registry does not managed the specified id.
     */
    public static PublishApi newPublishProxy(Node node, String registryUrl,
                                             EventCloudId id)
            throws EventCloudIdNotManaged {
        return ProxyFactory.newPublishProxy(node, null, registryUrl, id);
    }

    /**
     * Creates a new publish proxy component deployed on the specified
     * {@code node} and by using the specified deployment configuration.
     * 
     * @param node
     *            the node to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use during the deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PublishApi} interface of the new
     *         publish proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the specified id.
     */
    public static PublishApi newPublishProxy(Node node,
                                             DeploymentConfiguration deploymentConfiguration,
                                             String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return ProxyFactory.createPublishProxy(
                publishProxyAdl, ComponentUtils.createContext(node),
                deploymentConfiguration, registryUrl, id);
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
     *             if the specified registry does not managed the specified id.
     */
    public static PublishApi newPublishProxy(GCMVirtualNode vn,
                                             String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return ProxyFactory.newPublishProxy(vn, null, registryUrl, id);
    }

    /**
     * Creates a new publish proxy component deployed on the specified
     * {@code GCM virtual node} and by using the specified deployment
     * configuration.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use during the deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PublishApi} interface of the new
     *         publish proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the specified id.
     */
    public static PublishApi newPublishProxy(GCMVirtualNode vn,
                                             DeploymentConfiguration deploymentConfiguration,
                                             String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return ProxyFactory.createPublishProxy(
                publishProxyAdl, ComponentUtils.createContext(vn),
                deploymentConfiguration, registryUrl, id);
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
     *             if the specified registry does not managed the specified id.
     */
    public static PublishApi newPublishProxy(NodeProvider nodeProvider,
                                             String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return ProxyFactory.newPublishProxy(nodeProvider, null, registryUrl, id);
    }

    /**
     * Creates a new publish proxy component deployed on a node provided by the
     * specified {@code node provider} and by using the specified deployment
     * configuration.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use during the deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PublishApi} interface of the new
     *         publish proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the specified id.
     */
    public static PublishApi newPublishProxy(NodeProvider nodeProvider,
                                             DeploymentConfiguration deploymentConfiguration,
                                             String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return ProxyFactory.createPublishProxy(
                publishProxyAdl, AbstractFactory.getContextFromNodeProvider(
                        nodeProvider, PublishProxyImpl.PUBLISH_PROXY_VN),
                deploymentConfiguration, registryUrl, id);
    }

    protected static <T extends PublishProxy> PublishApi createPublishProxy(String publishProxyAdl,
                                                                            Map<String, Object> context,
                                                                            DeploymentConfiguration deploymentConfiguration,
                                                                            String registryUrl,
                                                                            EventCloudId id)
            throws EventCloudIdNotManaged {
        return ProxyFactory.createPublishProxy(
                publishProxyAdl, PublishProxy.class, context,
                deploymentConfiguration, registryUrl, id);
    }

    protected static <T extends PublishProxy> PublishApi createPublishProxy(String publishProxyAdl,
                                                                            Class<T> interfaceClass,
                                                                            Map<String, Object> context,
                                                                            DeploymentConfiguration deploymentConfiguration,
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

            PublishProxyAttributeController publishProxyAttributeController =
                    (PublishProxyAttributeController) GCM.getAttributeController(((Interface) pubProxy).getFcItfOwner());
            if (deploymentConfiguration != null) {
                publishProxyAttributeController.setDeploymentConfiguration(deploymentConfiguration);
            }
            EventCloudCache eventCloudProxy =
                    new EventCloudCache(registryUrl, id);
            publishProxyAttributeController.setAttributes(eventCloudProxy);

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
     *             if the specified registry does not managed the specified id.
     */
    public static SubscribeApi newSubscribeProxy(String registryUrl,
                                                 EventCloudId id,
                                                 AlterableElaProperty... properties)
            throws EventCloudIdNotManaged {
        return ProxyFactory.createSubscribeProxy(
                subscribeProxyAdl, new HashMap<String, Object>(), null,
                registryUrl, id, properties);
    }

    /**
     * Creates a new subscribe proxy component deployed on the local JVM, by
     * using the specified deployment configuration and by registering the proxy
     * to the registry in order to have the possibility to receive notification.
     * 
     * @param deploymentConfiguration
     *            the deployment configuration to use during the deployment.
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
     *             if the specified registry does not managed the specified id.
     */
    public static SubscribeApi newSubscribeProxy(DeploymentConfiguration deploymentConfiguration,
                                                 String registryUrl,
                                                 EventCloudId id,
                                                 AlterableElaProperty... properties)
            throws EventCloudIdNotManaged {
        return ProxyFactory.createSubscribeProxy(
                subscribeProxyAdl, new HashMap<String, Object>(),
                deploymentConfiguration, registryUrl, id, properties);
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
     *             if the specified registry does not managed the specified id.
     */
    public static SubscribeApi newSubscribeProxy(Node node,
                                                 String registryUrl,
                                                 EventCloudId id,
                                                 AlterableElaProperty... properties)
            throws EventCloudIdNotManaged {
        return ProxyFactory.newSubscribeProxy(
                node, null, registryUrl, id, properties);
    }

    /**
     * Creates a new subscribe proxy component deployed on the specified
     * {@code node}, by using the specified deployment configuration and by
     * registering the proxy to the registry in order to have the possibility to
     * receive notification.
     * 
     * @param node
     *            the node to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use during the deployment.
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
     *             if the specified registry does not managed the specified id.
     */
    public static SubscribeApi newSubscribeProxy(Node node,
                                                 DeploymentConfiguration deploymentConfiguration,
                                                 String registryUrl,
                                                 EventCloudId id,
                                                 AlterableElaProperty... properties)
            throws EventCloudIdNotManaged {
        return ProxyFactory.createSubscribeProxy(
                subscribeProxyAdl, ComponentUtils.createContext(node),
                deploymentConfiguration, registryUrl, id, properties);
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
     *             if the specified registry does not managed the specified id.
     */
    public static SubscribeApi newSubscribeProxy(GCMVirtualNode vn,
                                                 String registryUrl,
                                                 EventCloudId id,
                                                 AlterableElaProperty... properties)
            throws EventCloudIdNotManaged {
        return ProxyFactory.newSubscribeProxy(
                vn, null, registryUrl, id, properties);
    }

    /**
     * Creates a new subscribe proxy component deployed on the specified
     * {@code GCM virtual node}, by using the specified deployment configuration
     * and by registering the proxy to the registry in order to have the
     * possibility to receive notification.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use during the deployment.
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
     *             if the specified registry does not managed the specified id.
     */
    public static SubscribeApi newSubscribeProxy(GCMVirtualNode vn,
                                                 DeploymentConfiguration deploymentConfiguration,
                                                 String registryUrl,
                                                 EventCloudId id,
                                                 AlterableElaProperty... properties)
            throws EventCloudIdNotManaged {
        return ProxyFactory.createSubscribeProxy(
                subscribeProxyAdl, ComponentUtils.createContext(vn),
                deploymentConfiguration, registryUrl, id, properties);
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
     *             if the specified registry does not managed the specified id.
     */
    public static SubscribeApi newSubscribeProxy(NodeProvider nodeProvider,
                                                 String registryUrl,
                                                 EventCloudId id,
                                                 AlterableElaProperty... properties)
            throws EventCloudIdNotManaged {
        return ProxyFactory.newSubscribeProxy(
                nodeProvider, null, registryUrl, id, properties);
    }

    /**
     * Creates a new subscribe proxy component deployed on a node provided by
     * the specified {@code node provider}, by using the specified deployment
     * configuration and by registering the proxy to the registry in order to
     * have the possibility to receive notification.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use during the deployment.
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
     *             if the specified registry does not managed the specified id.
     */
    public static SubscribeApi newSubscribeProxy(NodeProvider nodeProvider,
                                                 DeploymentConfiguration deploymentConfiguration,
                                                 String registryUrl,
                                                 EventCloudId id,
                                                 AlterableElaProperty... properties)
            throws EventCloudIdNotManaged {
        return ProxyFactory.createSubscribeProxy(
                subscribeProxyAdl, AbstractFactory.getContextFromNodeProvider(
                        nodeProvider, SubscribeProxyImpl.SUBSCRIBE_PROXY_VN),
                deploymentConfiguration, registryUrl, id, properties);
    }

    protected static SubscribeApi createSubscribeProxy(String subscribeProxyAdl,
                                                       Map<String, Object> context,
                                                       DeploymentConfiguration deploymentConfiguration,
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

            SubscribeProxyAttributeController subscribeProxyAttributeController =
                    (SubscribeProxyAttributeController) GCM.getAttributeController(subComponent);
            if (deploymentConfiguration != null) {
                subscribeProxyAttributeController.setDeploymentConfiguration(deploymentConfiguration);
            }
            EventCloudCache eventCloudProxy =
                    new EventCloudCache(registryUrl, id);
            subscribeProxyAttributeController.setAttributes(
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
     *             if the specified registry does not managed the specified id.
     */
    public static PutGetApi newPutGetProxy(String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return ProxyFactory.createPutGetProxy(
                putgetProxyAdl, new HashMap<String, Object>(), null,
                registryUrl, id);
    }

    /**
     * Creates a new put/get proxy component deployed on the local JVM and by
     * using the specified deployment configuration.
     * 
     * @param deploymentConfiguration
     *            the deployment configuration to use during the deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PutGetApi} interface of the new
     *         put/get proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the specified id.
     */
    public static PutGetApi newPutGetProxy(DeploymentConfiguration deploymentConfiguration,
                                           String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return ProxyFactory.createPutGetProxy(
                putgetProxyAdl, new HashMap<String, Object>(),
                deploymentConfiguration, registryUrl, id);
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
     *             if the specified registry does not managed the specified id.
     */
    public static PutGetApi newPutGetProxy(Node node, String registryUrl,
                                           EventCloudId id)
            throws EventCloudIdNotManaged {
        return ProxyFactory.newPutGetProxy(node, null, registryUrl, id);
    }

    /**
     * Creates a new put/get proxy component deployed on the specified
     * {@code node} and by using the specified deployment configuration.
     * 
     * @param node
     *            the node to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use during the deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PutGetApi} interface of the new
     *         put/get proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the specified id.
     */
    public static PutGetApi newPutGetProxy(Node node,
                                           DeploymentConfiguration deploymentConfiguration,
                                           String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return ProxyFactory.createPutGetProxy(
                putgetProxyAdl, ComponentUtils.createContext(node),
                deploymentConfiguration, registryUrl, id);
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
     *             if the specified registry does not managed the specified id.
     */
    public static PutGetApi newPutGetProxy(GCMVirtualNode vn,
                                           String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return ProxyFactory.newPutGetProxy(vn, null, registryUrl, id);
    }

    /**
     * Creates a new put/get proxy component deployed on the specified
     * {@code GCM virtual node} and by using the specified deployment
     * configuration.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use during the deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PutGetApi} interface of the new
     *         put/get proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the specified id.
     */
    public static PutGetApi newPutGetProxy(GCMVirtualNode vn,
                                           DeploymentConfiguration deploymentConfiguration,
                                           String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return ProxyFactory.createPutGetProxy(
                putgetProxyAdl, ComponentUtils.createContext(vn),
                deploymentConfiguration, registryUrl, id);
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
     *             if the specified registry does not managed the specified id.
     */
    public static PutGetApi newPutGetProxy(NodeProvider nodeProvider,
                                           String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return ProxyFactory.newPutGetProxy(nodeProvider, null, registryUrl, id);
    }

    /**
     * Creates a new put/get proxy component deployed on a node provided by the
     * specified {@code node provider} and by using the specified deployment
     * configuration.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use during the deployment.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * 
     * @return the reference on the {@link PutGetApi} interface of the new
     *         put/get proxy component created.
     * 
     * @throws EventCloudIdNotManaged
     *             if the specified registry does not managed the specified id.
     */
    public static PutGetApi newPutGetProxy(NodeProvider nodeProvider,
                                           DeploymentConfiguration deploymentConfiguration,
                                           String registryUrl, EventCloudId id)
            throws EventCloudIdNotManaged {
        return ProxyFactory.createPutGetProxy(
                putgetProxyAdl, AbstractFactory.getContextFromNodeProvider(
                        nodeProvider, PutGetProxyImpl.PUTGET_PROXY_VN),
                deploymentConfiguration, registryUrl, id);
    }

    protected static PutGetApi createPutGetProxy(String putgetProxyAdl,
                                                 Map<String, Object> context,
                                                 DeploymentConfiguration deploymentConfiguration,
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

            PutGetProxyAttributeController putGetProxyAttributeController =
                    (PutGetProxyAttributeController) GCM.getAttributeController(((Interface) putgetProxy).getFcItfOwner());
            if (deploymentConfiguration != null) {
                putGetProxyAttributeController.setDeploymentConfiguration(deploymentConfiguration);
            }
            EventCloudCache eventCloudProxy =
                    new EventCloudCache(registryUrl, id);
            putGetProxyAttributeController.setAttributes(eventCloudProxy);

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
