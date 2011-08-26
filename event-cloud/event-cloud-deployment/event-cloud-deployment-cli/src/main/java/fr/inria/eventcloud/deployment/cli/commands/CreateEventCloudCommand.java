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
package fr.inria.eventcloud.deployment.cli.commands;

import org.objectweb.proactive.api.PAActiveObject;

import com.beust.jcommander.Parameter;

import fr.inria.eventcloud.EventCloud;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.properties.UnalterableElaProperty;
import fr.inria.eventcloud.deployment.cli.CommandLineReader;

/**
 * This command creates an {@link EventCloud} by using an
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
        EventCloud eventCloud =
                EventCloud.create(
                        PAActiveObject.getUrl(registry),
                        "http://node.provider.not.yet.available",
                        new Collection<UnalterableElaProperty>(),
                        this.nbTrackers, this.nbPeers);

        registry.register(eventCloud);

        System.out.println("Event Cloud with id '" + eventCloud.getId()
                + "' has been created and registered with " + this.nbPeers
                + " peer(s) and " + this.nbTrackers + " tracker(s).");
    }

}
