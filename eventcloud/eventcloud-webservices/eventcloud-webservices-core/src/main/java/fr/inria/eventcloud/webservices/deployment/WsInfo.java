/**
 * Copyright (c) 2011-2014 INRIA.
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

/**
 * Provides information associated to a web service.
 * 
 * @author bsauvan
 */
public abstract class WsInfo {

    protected final String streamUrl;

    protected final String wsEndpointUrl;

    /**
     * Creates a {@link WsInfo}.
     * 
     * @param streamUrl
     *            the URL which identifies the EventCloud which has been used to
     *            create the web service.
     * @param wsEndpointUrl
     *            the endpoint URL of the web service.
     */
    public WsInfo(String streamUrl, String wsEndpointUrl) {
        this.streamUrl = streamUrl;
        this.wsEndpointUrl = wsEndpointUrl;
    }

    /**
     * Returns the URL which identifies the EventCloud which has been used to
     * create the web service.
     * 
     * @return the URL which identifies the EventCloud which has been used to
     *         create the web service.
     */
    public String getStreamUrl() {
        return this.streamUrl;
    }

    /**
     * Returns the endpoint URL of the web service.
     * 
     * @return the endpoint URL of the web service.
     */
    public String getWsEndpointUrl() {
        return this.wsEndpointUrl;
    }

    /**
     * Destroys the web service.
     */
    public abstract void destroy();

}
