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

import com.beust.jcommander.Parameter;

import fr.inria.eventcloud.webservices.deployment.WsDeployer;

public class EventCloudsManagementServiceLauncher extends Launcher {

    @Parameter(names = {"--registry-url", "-r"}, description = "EventClouds registry URL", required = true)
    private String registryUrl;

    @Parameter(names = {"--port", "-p"}, description = "Port on which to deploy the web service", required = true)
    private int port;

    public static void main(String[] args) {
        EventCloudsManagementServiceLauncher launcher =
                new EventCloudsManagementServiceLauncher();
        launcher.parseArguments(launcher, args);
        launcher.launch();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String run() {
        return WsDeployer.deployEventCloudsManagementService(
                this.registryUrl,
                "proactive/services/eventclouds/management-webservices",
                this.port).getEndpoint().getEndpointInfo().getAddress();
    }

}
