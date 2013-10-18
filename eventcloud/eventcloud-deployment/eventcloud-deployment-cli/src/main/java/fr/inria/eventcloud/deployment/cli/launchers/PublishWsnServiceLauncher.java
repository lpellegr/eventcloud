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
package fr.inria.eventcloud.deployment.cli.launchers;

import org.objectweb.proactive.extensions.webservices.WSConstants;

import fr.inria.eventcloud.webservices.api.PublishWsnApi;
import fr.inria.eventcloud.webservices.deployment.WsDeployer;

/**
 * This launcher is used to deploy a {@link PublishWsnApi publish
 * WS-Notification service}.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public final class PublishWsnServiceLauncher extends WsnServiceLauncher {

    public static void main(String[] args) {
        PublishWsnServiceLauncher launcher = new PublishWsnServiceLauncher();
        launcher.parseArguments(launcher, args);
        launcher.launch();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String run() {
        return WsDeployer.deployPublishWsnService(
                WsLauncher.COMPONENT_POOL_MANAGER,
                super.registryUrl,
                super.eventCloudUrl,
                WSConstants.SERVICES_PATH + "eventclouds/"
                        + this.getTopicName(super.eventCloudUrl)
                        + "/wsn-service-" + this.numberId
                        + "_publish-webservices", this.port).getWsEndpointUrl();
    }

}
