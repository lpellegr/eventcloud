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
package fr.inria.eventcloud.deployment.cli.readers;

import java.io.IOException;
import java.util.Arrays;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;

import fr.inria.eventcloud.EventCloud;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.deployment.cli.CommandLineReader;
import fr.inria.eventcloud.deployment.cli.commands.CreateEventCloudCommand;
import fr.inria.eventcloud.deployment.cli.commands.ListEventCloudsCommand;

/**
 * This class is used to execute some operation (e.g. to create an
 * {@link EventCloud}, to list the {@link EventCloud}s managed by the registry,
 * etc) from the command-line.
 * 
 * @author lpellegr
 */
public class EventCloudsRegistryReader {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Event clouds registry URL expected");
            System.exit(1);
        }

        EventCloudsRegistry registry = null;
        try {
            registry =
                    PAActiveObject.lookupActive(
                            EventCloudsRegistry.class, args[0]);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Type 'help' to know what are the possible actions");

        @SuppressWarnings("unchecked")
        CommandLineReader<EventCloudsRegistry> reader =
                new CommandLineReader<EventCloudsRegistry>(Arrays.asList(
                        new CreateEventCloudCommand(),
                        new ListEventCloudsCommand()), registry);
        reader.run();
    }

}
