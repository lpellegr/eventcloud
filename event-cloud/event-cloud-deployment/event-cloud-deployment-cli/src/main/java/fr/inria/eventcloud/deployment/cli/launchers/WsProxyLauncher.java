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

/**
 * This class provides JCommander parameters which are compulsory to launch web
 * service proxies.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public abstract class WsProxyLauncher extends Launcher {

    @Parameter(names = {"--registry-url", "-r"}, description = "Eventclouds registry URL", required = true)
    protected String registryUrl;

    @Parameter(names = {"--eventcloud-url", "-e"}, description = "URL identifying an eventcloud which is deployed and running", required = true)
    protected String eventCloudIdUrl;

    @Parameter(names = {"--port", "-p"}, description = "Deploys the web service at the specified port", required = true)
    protected int port;

}
