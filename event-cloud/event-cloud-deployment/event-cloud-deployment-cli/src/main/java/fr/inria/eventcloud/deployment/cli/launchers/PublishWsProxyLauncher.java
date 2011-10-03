/**
 * Copyright (c) 2011 INRIA.
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
package fr.inria.eventcloud.deployment.cli.launchers;

import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.Proxy;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.webservices.deployment.WsProxyDeployer;

/**
 * This launcher is used to deploy a publish proxy component as a web service.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public final class PublishWsProxyLauncher extends WsProxyLauncher<PublishProxy> {

    public PublishWsProxyLauncher(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        new PublishWsProxyLauncher(args).run();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PublishProxy createProxy(ProxyFactory factory) {
        return factory.createPublishProxy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String exposeWsProxy(Proxy proxy) {
        return WsProxyDeployer.exposePublishWebService((PublishProxy) proxy);
    }

}
