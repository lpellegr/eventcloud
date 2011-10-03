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
package fr.inria.eventcloud.webservices.proxies;

import fr.inria.eventcloud.proxies.PublishProxyImpl;
import fr.inria.eventcloud.webservices.api.PublishWsApi;

/**
 * PublishWsProxyImpl is an extension of {@link PublishProxyImpl} in order to be
 * able to expose the proxy as a web service.
 * 
 * @author bsauvan
 */
public class PublishWsProxyImpl extends PublishProxyImpl implements
        PublishWsApi {

    /**
     * Empty constructor required by ProActive.
     */
    public PublishWsProxyImpl() {
        super();
    }

}
