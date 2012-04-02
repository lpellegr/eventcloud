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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.p2p.structured.AbstractComponent;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.properties.AlterableElaProperty;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.proxies.EventCloudCache;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.PublishProxyAttributeController;
import fr.inria.eventcloud.proxies.PutGetProxy;
import fr.inria.eventcloud.proxies.PutGetProxyAttributeController;
import fr.inria.eventcloud.proxies.SubscribeProxy;
import fr.inria.eventcloud.proxies.SubscribeProxyAttributeController;

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

    protected static Factory factory;

    protected static String publishProxyAdl;

    protected static String subscribeProxyAdl;

    protected static String putgetProxyAdl;

    static {
        log = LoggerFactory.getLogger(ProxyFactory.class);

        // proxies may be garbage collected in response to memory demand
        proxies = new MapMaker().softValues().makeMap();

        CentralPAPropertyRepository.GCM_PROVIDER.setValue(P2PStructuredProperties.GCM_PROVIDER.getValue());
        try {
            factory = FactoryFactory.getFactory();
        } catch (ADLException e) {
            e.printStackTrace();
        }

        publishProxyAdl = EventCloudProperties.PUBLISH_PROXY_ADL.getValue();
        subscribeProxyAdl = EventCloudProperties.SUBSCRIBE_PROXY_ADL.getValue();
        putgetProxyAdl = EventCloudProperties.PUTGET_PROXY_ADL.getValue();
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
     * Creates a new {@link PublishProxy}.
     * 
     * @return a new {@link PublishProxy}.
     */
    public PublishProxy createPublishProxy() {
        try {
            Component pubProxy =
                    (Component) factory.newComponent(
                            publishProxyAdl, new HashMap<String, Object>());
            PublishProxy stub =
                    (PublishProxy) pubProxy.getFcInterface(EventCloudProperties.PUBLISH_PROXY_SERVICES_ITF.getValue());

            ((PublishProxyAttributeController) GCM.getAttributeController(pubProxy)).setAttributes(this.eventCloudProxy);

            GCM.getGCMLifeCycleController(pubProxy).startFc();

            return stub;
        } catch (ADLException e) {
            e.printStackTrace();
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        } catch (IllegalLifeCycleException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Creates a new {@link SubscribeProxy} by registering the proxy to the
     * registry in order to have the possibility to receive notification.
     * 
     * @param properties
     *            the ELA properties to set.
     * 
     * @return a new {@link SubscribeProxy}.
     */
    public SubscribeProxy createSubscribeProxy(AlterableElaProperty... properties) {
        try {
            Component subProxy =
                    (Component) factory.newComponent(
                            subscribeProxyAdl, new HashMap<String, Object>());
            SubscribeProxy stub =
                    (SubscribeProxy) subProxy.getFcInterface(EventCloudProperties.SUBSCRIBE_PROXY_SERVICES_ITF.getValue());

            // registers the subscribe proxy to have the possibility
            // to receive notifications
            String componentUri =
                    Fractive.registerByName(subProxy, "subscribe-proxy-"
                            + UUID.randomUUID().toString());
            log.info("SubscribeProxy bound to {}", componentUri);

            ((SubscribeProxyAttributeController) GCM.getAttributeController(subProxy)).setAttributes(
                    this.eventCloudProxy, componentUri, properties);

            GCM.getGCMLifeCycleController(subProxy).startFc();

            return stub;
        } catch (ADLException e) {
            e.printStackTrace();
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        } catch (IllegalLifeCycleException e) {
            e.printStackTrace();
        } catch (ProActiveException e) {
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
        try {
            Component putgetProxy =
                    (Component) factory.newComponent(
                            putgetProxyAdl, new HashMap<String, Object>());
            PutGetProxy stub =
                    (PutGetProxy) putgetProxy.getFcInterface(EventCloudProperties.PUTGET_PROXY_SERVICES_ITF.getValue());

            ((PutGetProxyAttributeController) GCM.getAttributeController(putgetProxy)).setAttributes(this.eventCloudProxy);

            GCM.getGCMLifeCycleController(putgetProxy).startFc();

            return stub;
        } catch (ADLException e) {
            e.printStackTrace();
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        } catch (IllegalLifeCycleException e) {
            e.printStackTrace();
        }

        return null;
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
        ProxyFactory newFactory = new ProxyFactory(registryUrl, id);

        ProxyFactory oldFactory = proxies.putIfAbsent(id, newFactory);

        if (oldFactory == null) {
            return newFactory;
        } else {
            return oldFactory;
        }
    }

    public static SubscribeProxy lookupSubscribeProxy(String componentUri)
            throws IOException {
        return (SubscribeProxy) AbstractComponent.lookupFcInterface(
                componentUri,
                EventCloudProperties.SUBSCRIBE_PROXY_SERVICES_ITF.getValue());
    }

    public static PublishProxy lookupPublishProxy(String componentUri)
            throws IOException {
        return (PublishProxy) AbstractComponent.lookupFcInterface(
                componentUri,
                EventCloudProperties.PUBLISH_PROXY_SERVICES_ITF.getValue());
    }

    public static PutGetProxy lookupPutGetProxy(String componentUri)
            throws IOException {
        return (PutGetProxy) AbstractComponent.lookupFcInterface(
                componentUri,
                EventCloudProperties.PUTGET_PROXY_SERVICES_ITF.getValue());
    }

}
