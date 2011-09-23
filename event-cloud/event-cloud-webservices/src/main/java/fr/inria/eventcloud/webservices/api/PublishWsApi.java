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
package fr.inria.eventcloud.webservices.api;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.webservices.adapters.EventAdapter;

/**
 * Defines the publish operations that can be executed on an Event Cloud and can
 * be exposed as web services by a publish proxy component.
 * 
 * @author lpellegr
 * @author bsauvan
 */
@WebService(serviceName = "EventCloudPublish", portName = "EventCloudPublishPort", targetNamespace = "http://webservices.eventcloud.inria.fr/", name = "EventCloudPublishPortType")
public interface PublishWsApi {

    /**
     * Publishes the specified event.
     * 
     * @param event
     *            the event to publish.
     */
    @WebMethod(operationName = "Notify")
    public void publish(@WebParam(name = "xmlPayload") @XmlJavaTypeAdapter(EventAdapter.class) Event event);

}
