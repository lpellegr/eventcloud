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
package fr.inria.eventcloud.webservices.api;

import java.util.Collection;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.webservices.api.adapters.CompoundEventAdapter;
import fr.inria.eventcloud.webservices.api.adapters.CompoundEventCollectionAdapter;
import fr.inria.eventcloud.webservices.api.adapters.QuadrupleAdapter;

/**
 * Defines the publish operations that can be executed on an EventCloud and can
 * be exposed as web services by a publish proxy component.
 * 
 * @author bsauvan
 */
@WebService(serviceName = "EventCloudPublishWs", portName = "EventCloudPublishWsPort", name = "EventCloudPublishWsPortType", targetNamespace = "http://webservices.eventcloud.inria.fr/")
public interface PublishWsApi {

    /**
     * Publishes the specified quadruple.
     * 
     * @param quad
     *            the quadruple to publish.
     */
    @WebMethod(operationName = "publishQuadruple")
    public void publish(@WebParam(name = "quad") @XmlJavaTypeAdapter(QuadrupleAdapter.class) Quadruple quad);

    /**
     * Publishes the specified compound event.
     * 
     * @param event
     *            the compound event to publish.
     */
    @WebMethod(operationName = "publishCompoundEvent")
    public void publish(@WebParam(name = "event") @XmlJavaTypeAdapter(CompoundEventAdapter.class) CompoundEvent event);

    /**
     * Publishes the specified collection of compound events.
     * 
     * @param events
     *            the collection of compound events to publish.
     */
    @WebMethod(operationName = "publishCompoundEventCollection")
    public void publish(@WebParam(name = "events") @XmlJavaTypeAdapter(CompoundEventCollectionAdapter.class) Collection<CompoundEvent> events);

}
