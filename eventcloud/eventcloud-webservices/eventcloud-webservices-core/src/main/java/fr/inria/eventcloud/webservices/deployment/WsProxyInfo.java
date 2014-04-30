/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.webservices.deployment;

import java.io.IOException;

import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxy;

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.deployment.EventCloudComponentsManager;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;

/**
 * Provides information associated to a web service proxy.
 * 
 * @author bsauvan
 */
public abstract class WsProxyInfo<T extends Proxy> extends WsInfo {

    protected final EventCloudComponentsManager componentPoolManager;

    private final String registryUrl;

    protected final T proxy;

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
     * @param componentPoolManager
     *            the component pool manager used for the deployment of the web
     *            service proxy.
     * @param registryUrl
     *            the URL of the EventClouds registry on which the web service
     *            proxy has been registered.
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
            EventCloudComponentsManager componentPoolManager,
            String registryUrl, T proxy, String proxyName, String interfaceName) {
        super(streamUrl, wsEndpointUrl);
        this.componentPoolManager = componentPoolManager;
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
    public T getProxy() {
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
            EventCloudId id = new EventCloudId(this.streamUrl);

            this.releaseProxy(registry, id);
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    /**
     * Releases the proxy;
     * 
     * @param registry
     *            the EventClouds registry on which the web service proxy has
     *            been registered.
     * @param id
     *            the identifier of the EventCloud to which the web service
     *            proxy is associated.
     */
    protected abstract void releaseProxy(EventCloudsRegistry registry,
                                         EventCloudId id);

}
