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

import java.io.IOException;
import java.util.Arrays;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;

import fr.inria.eventcloud.EventCloud;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.EventCloudsRegistryFactory;
import fr.inria.eventcloud.deployment.cli.CommandLineReader;
import fr.inria.eventcloud.deployment.cli.commands.CreateEventCloudCommand;
import fr.inria.eventcloud.deployment.cli.commands.ListEventCloudsCommand;

/**
 * This launcher is used to create a new {@link EventCloudsRegistry}. Then, from
 * this instance it is possible, thanks to an interactive command-line reader,
 * to execute some operation (e.g. to create an {@link EventCloud}, to list the
 * {@link EventCloud}s managed by the registry, etc).
 * 
 * @author lpellegr
 */
public class EventCloudsRegistryLauncher {

    private EventCloudsRegistryLauncher() {
        
    }
    
    public static void main(String[] args) {

        EventCloudsRegistry registry = null;

        if (args.length == 1) {
            // retrieves the event clouds registry from the URL specified
            try {
                registry =
                        PAActiveObject.lookupActive(
                                EventCloudsRegistry.class, args[0]);
            } catch (ActiveObjectCreationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            registry = EventCloudsRegistryFactory.newEventCloudsRegistry();
        }

        System.out.println("EventCloudsRegistry running at:");
        System.out.println(PAActiveObject.getUrl(registry));
        System.out.println();

        System.out.println("Type 'help' to know what are the possible actions");

        @SuppressWarnings("unchecked")
        CommandLineReader<EventCloudsRegistry> reader =
                new CommandLineReader<EventCloudsRegistry>(Arrays.asList(
                        new CreateEventCloudCommand(),
                        new ListEventCloudsCommand()), registry);
        reader.run();
    }

}
