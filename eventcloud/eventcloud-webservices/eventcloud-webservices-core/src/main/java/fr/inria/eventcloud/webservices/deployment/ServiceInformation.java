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
package fr.inria.eventcloud.webservices.deployment;

import org.apache.cxf.endpoint.Server;

import fr.inria.eventcloud.webservices.services.EventCloudProxyService;

/**
 * This class is used to keep information associated to a web service exposing a
 * proxy.
 * 
 * @author bsauvan
 */
public class ServiceInformation {

    private final EventCloudProxyService<?> service;

    private final Server server;

    private final String endpointAddress;

    private final String streamUrl;

    private final int port;

    public ServiceInformation(EventCloudProxyService<?> service, Server server,
            String endpointAddress, String streamUrl, int port) {
        this.service = service;
        this.server = server;
        this.endpointAddress = endpointAddress;
        this.streamUrl = streamUrl;
        this.port = port;
    }

    public EventCloudProxyService<?> getService() {
        return this.service;
    }

    public String getEndpointAddress() {
        return this.endpointAddress;
    }

    public Server getServer() {
        return this.server;
    }

    public String getStreamUrl() {
        return this.streamUrl;
    }

    public int getPort() {
        return this.port;
    }

    public void destroy() {
        this.server.destroy();
        this.service.terminateProxy();
    }

}
