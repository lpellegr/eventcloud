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
package fr.inria.eventcloud.webservices.proxies;

import fr.inria.eventcloud.proxies.PutGetProxyImpl;
import fr.inria.eventcloud.webservices.api.PutGetWsApi;

/**
 * Extension of {@link PutGetProxyImpl} in order to be able to expose a put/get
 * proxy as a web service.
 * 
 * @author bsauvan
 */
public class PutGetWsProxyImpl extends PutGetProxyImpl implements PutGetWsApi {

    /**
     * ADL name of the put/get proxy web service component.
     */
    public static final String PUTGET_WEBSERVICE_PROXY_ADL =
            "fr.inria.eventcloud.webservices.proxies.PutGetWsProxy";

    /**
     * Functional interface name of the put/get web service proxy component.
     */
    public static final String PUTGET_WEBSERVICES_ITF = "putget-webservices";

    /**
     * Empty constructor required by ProActive.
     */
    public PutGetWsProxyImpl() {
        super();
    }

}
