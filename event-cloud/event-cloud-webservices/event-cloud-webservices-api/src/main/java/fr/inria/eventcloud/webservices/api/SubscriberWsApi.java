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

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.webservices.api.adapters.EventCollectionAdapter;

/**
 * Defines the notification operation that is exposed as a web service by the
 * subscriber of a subscription and is called by the subscribe proxy component
 * when an event that matches the subscription is received.
 * 
 * @author lpellegr
 * @author bsauvan
 */
@WebService(serviceName = "EventCloudSubscriber", portName = "EventCloudSubscriberPort", targetNamespace = "http://docs.oasis-open.org/wsn/bw-2", name = "EventCloudSubscriberPortType")
@XmlSeeAlso({
        org.oasis_open.docs.wsn.t_1.ObjectFactory.class,
        org.oasis_open.docs.wsn.b_2.ObjectFactory.class,
        org.oasis_open.docs.wsrf.r_2.ObjectFactory.class,
        org.oasis_open.docs.wsrf.bf_2.ObjectFactory.class})
public interface SubscriberWsApi {

    /**
     * Notifies that a collection of events matching a subscription have been
     * received.
     * 
     * @param events
     *            the collection of events.
     */
    @Oneway
    @WebMethod(operationName = "Notify", action = "http://www.petalslink.com/wsn/service/WsnConsumer/Notify")
    public void notify(@WebParam(partName = "Notify", name = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") @XmlJavaTypeAdapter(EventCollectionAdapter.class) Collection<Event> events);

}
