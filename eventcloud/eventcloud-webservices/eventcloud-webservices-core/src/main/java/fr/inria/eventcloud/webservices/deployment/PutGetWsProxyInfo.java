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
package fr.inria.eventcloud.webservices.deployment;

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.deployment.ComponentPoolManager;
import fr.inria.eventcloud.proxies.PutGetProxy;

/**
 * Provides information associated to a put/get web service proxy.
 * 
 * @author bsauvan
 */
public class PutGetWsProxyInfo extends WsProxyInfo<PutGetProxy> {

    /**
     * Creates a {@link PutGetWsProxyInfo}.
     * 
     * @param streamUrl
     *            the URL which identifies the EventCloud which has been used to
     *            create the web service.
     * @param wsEndpointUrl
     *            the endpoint URL of the web service.
     * @param componentPoolManager
     *            the component pool manager used for the deployment of the
     *            put/get web service proxy.
     * @param registryUrl
     *            the URL of the EventClouds registry on which the put/get web
     *            service proxy has been registered.
     * @param putgetProxy
     *            the PublishProxy interface of the component owning the
     *            interface exposed as a web service.
     * @param proxyName
     *            the name of the put/get web service proxy which is used as
     *            part of the URL associated to the web service deployed.
     * @param interfaceName
     *            the name of the interface exposed as a web service.
     */
    public PutGetWsProxyInfo(String streamUrl, String wsEndpointUrl,
            ComponentPoolManager componentPoolManager, String registryUrl,
            PutGetProxy putgetProxy, String proxyName, String interfaceName) {
        super(streamUrl, wsEndpointUrl, componentPoolManager, registryUrl,
                putgetProxy, proxyName, interfaceName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void releaseProxy(EventCloudsRegistry registry, EventCloudId id) {
        registry.unregisterProxy(id, super.proxy);
        super.componentPoolManager.releasePutGetProxies(super.proxy);
    }

}
