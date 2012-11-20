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

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.deployment.cli.CommandLineReader;
import fr.inria.eventcloud.deployment.cli.converters.EventCloudIdConverter;

/**
 * This command destroys an EventCloud identified by a specified stream URL.
 * 
 * @author lpellegr
 */
public class DestroyEventCloudCommand extends Command<EventCloudsRegistry> {

    @Parameter(names = {"--stream-url"}, description = "Stream URL", converter = EventCloudIdConverter.class, required = true)
    private EventCloudId id;

    public DestroyEventCloudCommand() {
        super(
                "destroy-eventcloud",
                "Destroy the EventCloud identified by the specified stream URL",
                new String[] {"destroy"});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(CommandLineReader<EventCloudsRegistry> reader,
                        EventCloudsRegistry registry) {
        if (registry.contains(this.id)) {
            registry.undeploy(this.id);
            System.out.println("EventCloud associated to stream URL '"
                    + this.id.getStreamUrl() + "' destroyed with success");
        } else {
            System.out.println("EventCloud identified by stream URL '"
                    + this.id.getStreamUrl() + "' does not exist");
        }
    }

}
