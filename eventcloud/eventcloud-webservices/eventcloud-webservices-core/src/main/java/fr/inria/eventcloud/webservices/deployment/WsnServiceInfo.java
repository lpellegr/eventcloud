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
package fr.inria.eventcloud.webservices.deployment;

import org.apache.cxf.endpoint.Server;

import fr.inria.eventcloud.webservices.wsn.WsnService;

/**
 * Provides information associated to a {@link WsnService WS-Notification
 * service}.
 * 
 * @author bsauvan
 */
public class WsnServiceInfo extends WsInfo {

    private final WsnService<?> service;

    private final Server server;

    /**
     * Creates a {@link WsnServiceInfo}.
     * 
     * @param streamUrl
     *            the URL which identifies the EventCloud which has been used to
     *            create the web service.
     * @param wsEndpointUrl
     *            the endpoint URL of the web service.
     * @param service
     *            the WS-Notification service.
     * @param server
     *            the Server instance of the web service.
     */
    public WsnServiceInfo(String streamUrl, String wsEndpointUrl,
            WsnService<?> service, Server server) {
        super(streamUrl, wsEndpointUrl);
        this.service = service;
        this.server = server;
    }

    /**
     * Returns the WS-Notification service.
     * 
     * @return the WS-Notification service.
     */
    public WsnService<?> getService() {
        return this.service;
    }

    /**
     * Returns the Server instance of the web service.
     * 
     * @return the Server instance of the web service.
     */
    public Server getServer() {
        return this.server;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        this.server.destroy();
        this.service.terminateProxy();
    }

}
