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

    private static final long serialVersionUID = 1L;

    static {
        publishProxyAdl = PublishWsProxyImpl.PUBLISH_WEBSERVICE_PROXY_ADL;
        subscribeProxyAdl = SubscribeWsProxyImpl.SUBSCRIBE_WEBSERVICE_PROXY_ADL;
        putgetProxyAdl = PutGetWsProxyImpl.PUTGET_WEBSERVICE_PROXY_ADL;
    }

}
