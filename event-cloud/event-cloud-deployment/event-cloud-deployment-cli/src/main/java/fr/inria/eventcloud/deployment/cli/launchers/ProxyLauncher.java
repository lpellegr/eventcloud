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

import java.util.Arrays;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.deployment.cli.CommandLineReader;
import fr.inria.eventcloud.deployment.cli.commands.Command;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.Proxy;

/**
 * 
 * @author lpellegr
 */
public abstract class ProxyLauncher<T extends Proxy> {

    @Parameter(names = {"-registry"}, description = "The EventCloudsRegistry URL to use", required = true)
    private String registryUrl;

    @Parameter(names = {"-id"}, description = "The Event Cloud Id that identify the event cloud to work on", required = true)
    private long eventCloudId;

    private Command<T>[] commands;

    public ProxyLauncher(String[] args, Command<T>... commands) {
        this.commands = commands;

        JCommander jCommander = new JCommander(this);
        jCommander.setProgramName(this.getClass().getCanonicalName());

        try {
            jCommander.parse(args);
        } catch (ParameterException e) {
            jCommander.usage();
            System.exit(1);
        }
    }

    public abstract T createProxy(ProxyFactory factory);

    public void run() {
        ProxyFactory factory =
                ProxyFactory.getInstance(this.registryUrl, new EventCloudId(
                        this.eventCloudId));

        CommandLineReader<T> reader =
                new CommandLineReader<T>(
                        Arrays.asList(this.commands), createProxy(factory));
        reader.run();
    }

}
