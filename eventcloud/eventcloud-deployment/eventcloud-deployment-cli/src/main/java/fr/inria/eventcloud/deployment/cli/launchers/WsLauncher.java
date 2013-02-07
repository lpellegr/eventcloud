/**
 * Copyright (c) 2011-2013 INRIA.
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
package fr.inria.eventcloud.deployment.cli.launchers;

import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.deployment.local.LocalNodeProvider;

import com.beust.jcommander.Parameter;

/**
 * Provides JCommander parameters which are compulsory to launch web services.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public abstract class WsLauncher extends Launcher {

    protected static final NodeProvider LOCAL_NODE_PROVIDER =
            getLocalNodeProvider();

    @Parameter(names = {"--registry-url", "-r"}, description = "EventClouds registry URL", required = true)
    protected String registryUrl;

    @Parameter(names = {"--eventcloud-url", "-e"}, description = "URL identifying an EventCloud which is deployed and running", required = true)
    protected String eventCloudUrl;

    @Parameter(names = {"--number-id", "-n"}, description = "Identification number which will be part of the web service endpoint URL", required = true)
    protected int numberId;

    private static final NodeProvider getLocalNodeProvider() {
        NodeProvider nodeProvider = new LocalNodeProvider();

        nodeProvider.start();

        return nodeProvider;
    }

    protected String getTopicName(String streamUrl) {
        return streamUrl.substring(streamUrl.lastIndexOf('/') + 1);
    }

}
