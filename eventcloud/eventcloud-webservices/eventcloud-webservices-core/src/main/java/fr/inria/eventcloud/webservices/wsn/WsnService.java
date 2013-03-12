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
package fr.inria.eventcloud.webservices.wsn;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.proxies.Proxy;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;
import fr.inria.eventcloud.translators.wsn.WsnTranslator;
import fr.inria.eventcloud.webservices.api.PublishWsnApi;
import fr.inria.eventcloud.webservices.api.SubscribeWsnApi;

/**
 * Abstract class which is common to all WS-Notification services (e.g.
 * {@link PublishWsnApi} or {@link SubscribeWsnApi}). All the calls to the
 * WS-Notification services will be translated and redirected to an underlying
 * {@link Proxy} in order to be treated into an EventCloud.
 * 
 * @author lpellegr
 * @author bsauvan
 * 
 * @param <T>
 *            proxy type.
 */
public abstract class WsnService<T> {

    protected static Logger log = LoggerFactory.getLogger(WsnService.class);

    protected final NodeProvider nodeProvider;

    protected final String registryUrl;

    protected final String streamUrl;

    protected T proxy;

    protected final WsnTranslator translator;

    /**
     * Creates a {@link WsnService}.
     * 
     * @param nodeProvider
     *            the node provider to be used for the deployment of the
     *            underlying proxy.
     * @param registryUrl
     *            the URL of the EventClouds registry to connect to in order to
     *            create the underlying proxy.
     * @param streamUrl
     *            the URL which identifies the EventCloud on which the
     *            underlying proxy must be connected.
     */
    public WsnService(NodeProvider nodeProvider, String registryUrl,
            String streamUrl) {
        this.nodeProvider = nodeProvider;
        this.registryUrl = registryUrl;
        this.streamUrl = streamUrl;
        this.translator = new WsnTranslator();
    }

    /**
     * Initializes the WS-Notification service.
     */
    @PostConstruct
    public void init() {
        try {
            this.proxy = this.getProxy();
            log.info("{} proxy deployed", this.proxy.getClass().getName());
        } catch (EventCloudIdNotManaged e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns the underlying proxy.
     * 
     * @return the undelying proxy.
     * @throws EventCloudIdNotManaged
     *             if there was no existing EventCloud for the {@code streamUrl}
     *             .
     */
    public abstract T getProxy() throws EventCloudIdNotManaged;

    /**
     * Terminates the underlying proxy.
     */
    public void terminateProxy() {
        try {
            EventCloudsRegistry registry =
                    EventCloudsRegistryFactory.lookupEventCloudsRegistry(this.registryUrl);
            EventCloudId id = new EventCloudId(this.streamUrl);

            if (this.proxy instanceof PublishProxy) {
                registry.unregisterProxy(id, (PublishProxy) this.proxy);
            } else if (this.proxy instanceof SubscribeProxy) {
                registry.unregisterProxy(id, (SubscribeProxy) this.proxy);
            } else {
                logAndThrowIllegalArgumentException("Unknow proxy type: "
                        + this.proxy.getClass());
            }

            ComponentUtils.terminateComponent(this.proxy);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected static final void logAndThrowIllegalArgumentException(String msg) {
        log.error(msg);
        throw new IllegalArgumentException(msg);
    }

}
