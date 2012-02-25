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

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.webservices.configuration.EventCloudWsProperties;

/**
 * WsProxyFactory is used to create a new instance of a proxy component (e.g.
 * subscribe proxy component, publish proxy component or put/get proxy
 * component) that can be exposed as a web service.
 * 
 * @author bsauvan
 */
public final class WsProxyFactory extends ProxyFactory {

    private static final long serialVersionUID = 1L;

    static {
        publishProxyAdl = EventCloudWsProperties.PUBLISH_PROXY_ADL.getValue();
        subscribeProxyAdl =
                EventCloudWsProperties.SUBSCRIBE_PROXY_ADL.getValue();
        putgetProxyAdl = EventCloudWsProperties.PUTGET_PROXY_ADL.getValue();
    }

    /**
     * Constructs a new WsProxyFactory from the specified registryUrl and the
     * given Event Cloud id.
     * 
     * @param registryUrl
     *            the Event-Cloud registry url.
     * @param id
     *            the identifier that identify the Event-Cloud to work on.
     */
    private WsProxyFactory(String registryUrl, EventCloudId id) {
        super(registryUrl, id);
    }

    public static ProxyFactory getInstance(String registryUrl, EventCloudId id) {
        ProxyFactory newFactory = new WsProxyFactory(registryUrl, id);

        ProxyFactory oldFactory = proxies.putIfAbsent(id, newFactory);

        if (oldFactory == null) {
            return newFactory;
        } else {
            return oldFactory;
        }
    }

}
