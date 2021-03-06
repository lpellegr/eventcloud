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
package fr.inria.eventcloud.deployment.cli.launchers;

import fr.inria.eventcloud.webservices.api.PutGetWsApi;
import fr.inria.eventcloud.webservices.deployment.WsDeployer;

/**
 * This launcher is used to deploy a {@link PutGetWsApi put/get web service
 * proxy}.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public final class PutGetWsProxyLauncher extends WsLauncher {

    public static void main(String[] args) {
        PutGetWsProxyLauncher launcher = new PutGetWsProxyLauncher();
        launcher.parseArguments(launcher, args);
        launcher.launch();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String run() {
        return WsDeployer.deployPutGetWsProxy(
                WsLauncher.COMPONENT_POOL_MANAGER,
                super.registryUrl,
                super.eventCloudUrl,
                "eventclouds/" + this.getTopicName(super.eventCloudUrl)
                        + "/ws-proxy-" + this.numberId).getWsEndpointUrl();
    }
}
