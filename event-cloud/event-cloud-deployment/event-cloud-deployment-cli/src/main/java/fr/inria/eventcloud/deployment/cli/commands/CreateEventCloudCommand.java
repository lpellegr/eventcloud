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
package fr.inria.eventcloud.deployment.cli.commands;

import com.beust.jcommander.Parameter;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.deployment.cli.CommandLineReader;
import fr.inria.eventcloud.providers.SemanticPersistentOverlayProvider;

/**
 * This command creates an {@link EventCloudDescription} by using an
 * {@link EventCloudsRegistry}.
 * 
 * @author lpellegr
 */
public class CreateEventCloudCommand extends Command<EventCloudsRegistry> {

    @Parameter(names = {"-nb-peers"}, description = "Number of Peers", required = true)
    private int nbPeers = 1;

    @Parameter(names = {"-nb-trackers"}, description = "Number of Trackers")
    private int nbTrackers = 1;

    public CreateEventCloudCommand() {
        super("create-event-cloud",
                "Creates a new event cloud and returns the binding URL",
                new String[] {"create"});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(CommandLineReader<EventCloudsRegistry> reader,
                        EventCloudsRegistry registry) {

        EventCloudDeployer deployer =
                new EventCloudDeployer(
                        new EventCloudDescription(new EventCloudId()),
                        new EventCloudDeploymentDescriptor(
                                new SemanticPersistentOverlayProvider()));

        deployer.deploy(this.nbTrackers, this.nbPeers);

        System.out.println("Event Cloud with id '"
                + deployer.getEventCloudDescription().getId()
                + "' has been created and registered with " + this.nbPeers
                + " peer(s) and " + this.nbTrackers + " tracker(s).");
    }

}
