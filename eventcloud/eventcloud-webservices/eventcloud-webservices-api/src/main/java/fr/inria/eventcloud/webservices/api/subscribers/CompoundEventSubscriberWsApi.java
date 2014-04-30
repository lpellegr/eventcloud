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
package fr.inria.eventcloud.webservices.api.subscribers;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.webservices.api.adapters.CompoundEventAdapter;

/**
 * Defines the notification operation that is exposed as a web service by the
 * subscriber of a subscription and which is called by the subscribe proxy
 * component when a {@link CompoundEvent} that matches the subscription is
 * received.
 * 
 * @author bsauvan
 */
@WebService(serviceName = "EventCloudCompoundEventSubscriber", portName = "EventCloudCompoundEventSubscriberPort", name = "EventCloudCompoundEventSubscriberPortType", targetNamespace = "http://webservices.eventcloud.inria.fr/")
public interface CompoundEventSubscriberWsApi {

    /**
     * Notifies that a compound event matching a subscription has been received.
     * 
     * @param subscriptionId
     *            the subscription identifier which has been matched.
     * @param compoundEvent
     *            a compound event that matches the subscription.
     */
    @WebMethod(operationName = "notifyCompoundEvent")
    void notifyCompoundEvent(@WebParam(name = "id") String subscriptionId,
                             @WebParam(name = "event") @XmlJavaTypeAdapter(CompoundEventAdapter.class) CompoundEvent compoundEvent);

}
