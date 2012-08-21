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
package fr.inria.eventcloud.deployment.cli.readers;

import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.deployment.cli.CommandLineReader;
import fr.inria.eventcloud.deployment.cli.commands.CreateEventCloudCommand;
import fr.inria.eventcloud.deployment.cli.commands.DestroyEventCloudCommand;
import fr.inria.eventcloud.deployment.cli.commands.ListEventCloudsCommand;
import fr.inria.eventcloud.deployment.cli.commands.SubscribeEventCloudCommand;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;

/**
 * This class is used to execute some operation (e.g. to create an
 * {@link EventCloudDescription}, to list the {@link EventCloudDescription}s
 * managed by the registry, etc) from the command-line.
 * 
 * @author lpellegr
 */
public class EventCloudsRegistryReader {

    @Parameter(names = {"-r", "--registry-url"}, description = "Eventclouds registry URL to use", required = true)
    private String registryUrl;

    public static void main(String[] args) {
        disableLogging();

        new EventCloudsRegistryReader().run(args);
    }

    private static void disableLogging() {
        System.setProperty("log4j.configuration", "");
        Logger.getRootLogger().setLevel(Level.OFF);

        LoggerContext context =
                (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
    }

    public void run(String[] args) {
        JCommander jCommander = new JCommander(this);
        jCommander.setProgramName(EventCloudsRegistryReader.class.getCanonicalName());

        try {
            jCommander.parse(args);
        } catch (ParameterException e) {
            jCommander.usage();
            System.exit(1);
        }

        System.out.println("Connecting to " + this.registryUrl + "...");

        EventCloudsRegistry registry = null;
        try {
            registry = EventCloudsRegistryFactory.lookupEventCloudsRegistry(this.registryUrl);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Type 'help' to know what are the possible actions");

        @SuppressWarnings("unchecked")
        CommandLineReader<EventCloudsRegistry> reader =
                new CommandLineReader<EventCloudsRegistry>(Arrays.asList(
                        new CreateEventCloudCommand(),
                        new DestroyEventCloudCommand(),
                        new ListEventCloudsCommand(),
                        new SubscribeEventCloudCommand()), registry);
        reader.run();
    }

    public String getRegistryUrl() {
        return this.registryUrl;
    }

}
