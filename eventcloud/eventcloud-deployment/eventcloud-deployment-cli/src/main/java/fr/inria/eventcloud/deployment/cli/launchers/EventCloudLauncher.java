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
package fr.inria.eventcloud.deployment.cli.launchers;

import java.io.IOException;

import com.beust.jcommander.Parameter;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.providers.SemanticPersistentOverlayProvider;

/**
 * This launcher is used to create a new EventCloud and to register it to the
 * specified {@link EventCloudsRegistry} URL. If the deployment succeed, an
 * instance file is created. This instance file contains a three columns value
 * such as {@code 1 1 -932949592}. The first number indicates the number of
 * trackers deployed, the second the number of peers deployed and the last
 * information is an URL which uniquely identifies the EventCloud which is
 * running.
 * 
 * @author lpellegr
 */
public final class EventCloudLauncher extends Launcher {

    @Parameter(names = {"--registry-url", "-r"}, description = "EventClouds registry URL", required = true)
    private String registryUrl;

    @Parameter(names = {"--nb-peers"}, description = "Number of peers")
    private int nbPeers = 1;

    @Parameter(names = {"--nb-trackers"}, description = "Number of trackers")
    private int nbTrackers = 1;

    public static void main(String[] args) {
        EventCloudLauncher launcher = new EventCloudLauncher();
        launcher.parseArguments(launcher, args);
        launcher.launch();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String run() {
        EventCloudDeployer deployer =
                new EventCloudDeployer(
                        new EventCloudDescription(new EventCloudId()),
                        new EventCloudDeploymentDescriptor(
                                new SemanticPersistentOverlayProvider()));

        deployer.deploy(this.nbTrackers, this.nbPeers);

        boolean registered = false;
        try {
            registered =
                    EventCloudsRegistryFactory.lookupEventCloudsRegistry(
                            this.registryUrl).register(deployer);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        if (!registered) {
            throw new IllegalStateException("EventCloud with id "
                    + deployer.getEventCloudDescription().getId()
                    + " is already registered into the EventClouds registry "
                    + this.registryUrl);
        }

        StringBuilder result = new StringBuilder();
        result.append(this.nbTrackers);
        result.append(' ');
        result.append(this.nbPeers);
        result.append(' ');
        result.append(deployer.getEventCloudDescription()
                .getId()
                .getStreamUrl());

        return result.toString();
    }
}
