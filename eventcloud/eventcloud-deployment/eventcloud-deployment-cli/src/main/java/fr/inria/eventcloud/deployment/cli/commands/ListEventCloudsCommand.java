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
package fr.inria.eventcloud.deployment.cli.commands;

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.deployment.cli.CommandLineReader;

/**
 * This command lists the EventClouds that are registered by an
 * {@link EventCloudsRegistry}.
 * 
 * @author lpellegr
 */
public class ListEventCloudsCommand extends Command<EventCloudsRegistry> {

    public ListEventCloudsCommand() {
        super("list-eventclouds",
                "Lists the EventClouds maintained by the EventClouds registry",
                new String[] {"list"});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(CommandLineReader<EventCloudsRegistry> reader,
                        EventCloudsRegistry registry) {
        for (EventCloudId id : registry.listEventClouds()) {
            System.out.println("  - " + id);
        }
    }

}
