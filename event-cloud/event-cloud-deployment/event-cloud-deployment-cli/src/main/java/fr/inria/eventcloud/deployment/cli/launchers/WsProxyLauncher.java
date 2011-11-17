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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * This class is used to provide the operations which are common to all the
 * proxies that have to be deployed as a web service.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public abstract class WsProxyLauncher extends Launcher {

    @Parameter(names = {"-registry"}, description = "An eventclouds registry URL to use", required = true)
    protected String registryUrl;

    @Parameter(names = {"-id"}, description = "An eventcloud identifier to link the proxy with", required = true)
    protected String eventCloudIdUrl;

    @Parameter(names = {"-port"}, description = "The HTTP port to use in order to deploy the webservice", required = true)
    protected int port;

    protected WsProxyLauncher(String[] args) {
        super(INSTANCE_FILE_JAVA_PROPERTY_NAME);

        JCommander jCommander = new JCommander(this);
        jCommander.setProgramName(this.getClass().getCanonicalName());

        try {
            jCommander.parse(args);
        } catch (ParameterException e) {
            jCommander.usage();
            System.exit(1);
        }
    }

    public abstract String deployWsProxy();

    /**
     * {@inheritDoc}
     */
    @Override
    protected String run() {
        return this.deployWsProxy();
    }

}
