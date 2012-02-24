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

import com.beust.jcommander.Parameter;

import fr.inria.eventcloud.EventCloud;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.properties.UnalterableElaProperty;
import fr.inria.eventcloud.deployment.EventCloudDeployer;

/**
 * This launcher is used to create a new {@link EventCloud} and to register it
 * to the specified {@link EventCloudsRegistry} URL. If the deployment succeed,
 * an instance file is created. This instance file contains a three columns
 * value such as {@code 1 1 -932949592}. The first number indicates the number
 * of trackers deployed, the second the number of peers deployed and the last
 * information is an URL which uniquely identifies the eventcloud which is
 * running.
 * 
 * @author lpellegr
 */
public final class EventCloudLauncher extends Launcher {

    @Parameter(names = {"--registry-url", "-r"}, description = "Eventclouds registry URL", required = true)
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
        EventCloud eventCloud =
                EventCloud.create(
                        this.registryUrl, new EventCloudDeployer(),
                        new Collection<UnalterableElaProperty>(),
                        this.nbTrackers, this.nbPeers);

        StringBuilder result = new StringBuilder();
        result.append(this.nbTrackers);
        result.append(" ");
        result.append(this.nbPeers);
        result.append(" ");
        result.append(eventCloud.getId().getStreamUrl());

        return result.toString();
    }

}
