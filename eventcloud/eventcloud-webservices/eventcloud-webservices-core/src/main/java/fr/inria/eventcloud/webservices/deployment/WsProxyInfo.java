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
package fr.inria.eventcloud.webservices.deployment;

import java.io.IOException;

import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.proxies.Proxy;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.PutGetProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;

/**
 * Provides information associated to a web service proxy.
 * 
 * @author bsauvan
 */
public class WsProxyInfo extends WsInfo {

    private final String registryUrl;

    private final Proxy proxy;

    private final String proxyName;

    private final String interfaceName;

    /**
     * Creates a {@link WsProxyInfo}.
     * 
     * @param streamUrl
     *            the URL which identifies the EventCloud which has been used to
     *            create the web service.
     * @param wsEndpointUrl
     *            the endpoint URL of the web service.
     * @param registryUrl
     *            the URL of the EventClouds registry used to create the web
     *            service proxy.
     * @param proxy
     *            the Proxy interface of the component owning the interface
     *            exposed as a web service.
     * @param proxyName
     *            the name of the web service proxy which is used as part of the
     *            URL associated to the web service deployed.
     * @param interfaceName
     *            the name of the interface exposed as a web service.
     */
    public WsProxyInfo(String streamUrl, String wsEndpointUrl,
            String registryUrl, Proxy proxy, String proxyName,
            String interfaceName) {
        super(streamUrl, wsEndpointUrl);
        this.registryUrl = registryUrl;
        this.proxy = proxy;
        this.proxyName = proxyName;
        this.interfaceName = interfaceName;
    }

    /**
     * Returns the URL of the EventClouds registry used to create the web
     * service proxy.
     * 
     * @return the URL of the EventClouds registry used to create the web
     *         service proxy.
     */
    public String getRegistryUrl() {
        return this.registryUrl;
    }

    /**
     * Returns the Proxy interface of the component owning the interface exposed
     * as a web service.
     * 
     * @return the Proxy interface of the component owning the interface exposed
     *         as a web service.
     */
    public Proxy getProxy() {
        return this.proxy;
    }

    /**
     * Returns the name of the web service proxy which is used as part of the
     * URL associated to the web service deployed.
     * 
     * @return the name of the web service proxy which is used as part of the
     *         URL associated to the web service deployed.
     */
    public String getProxyName() {
        return this.proxyName;
    }

    /**
     * Returns the name of the interface exposed as a web service.
     * 
     * @return the name of the interface exposed as a web service.
     */
    public String getInterfaceName() {
        return this.interfaceName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        WsDeployer.unexposeWebService(
                this.proxy, this.proxyName, this.interfaceName);
        try {
            EventCloudsRegistry registry =
                    EventCloudsRegistryFactory.lookupEventCloudsRegistry(this.registryUrl);

            EventCloudId eventCloudId = new EventCloudId(this.streamUrl);

            if (this.proxy instanceof PublishProxy) {
                registry.unregisterProxy(
                        eventCloudId, (PublishProxy) this.proxy);
            } else if (this.proxy instanceof SubscribeProxy) {
                registry.unregisterProxy(
                        eventCloudId, (SubscribeProxy) this.proxy);
            } else if (this.proxy instanceof PutGetProxy) {
                registry.unregisterProxy(eventCloudId, (PutGetProxy) this.proxy);
            } else {
                throw new IllegalArgumentException("Unknow proxy type: "
                        + this.proxy.getClass());
            }

            ComponentUtils.terminateComponent(this.proxy);
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }
}
