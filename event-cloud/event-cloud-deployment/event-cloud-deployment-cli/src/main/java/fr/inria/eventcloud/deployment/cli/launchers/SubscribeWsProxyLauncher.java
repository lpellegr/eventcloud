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

import fr.inria.eventcloud.webservices.deployment.WsProxyDeployer;

/**
 * This launcher is used to deploy a subscribe proxy component as a web service.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public final class SubscribeWsProxyLauncher extends WsProxyLauncher2 {

    public SubscribeWsProxyLauncher(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        new SubscribeWsProxyLauncher(args).run();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String deployWsProxy() {
        return WsProxyDeployer.deploySubscribeWebService(
                super.registryUrl, super.eventCloudIdUrl,
                "proactive/services/EventCloud_subscribe-webservices",
                super.port);
    }

}
