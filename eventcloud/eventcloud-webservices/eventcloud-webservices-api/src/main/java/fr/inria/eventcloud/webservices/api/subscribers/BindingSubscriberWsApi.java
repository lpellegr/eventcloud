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
package fr.inria.eventcloud.webservices.api.subscribers;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.wrappers.BindingWrapper;
import fr.inria.eventcloud.webservices.api.adapters.BindingWrapperAdapter;

/**
 * Defines the notification operation that is exposed as a web service by the
 * subscriber of a subscription and which is called by the subscribe proxy
 * component when a {@link Binding} that matches the subscription is received.
 * 
 * @author bsauvan
 */
@WebService(serviceName = "EventCloudBindingSubscriber", portName = "EventCloudBindingSubscriberPort", name = "EventCloudBindingSubscriberPortType", targetNamespace = "http://webservices.eventcloud.inria.fr/")
public interface BindingSubscriberWsApi {

    /**
     * Notifies that a binding matching a subscription has been received.
     * 
     * @param subscriptionId
     *            the subscription identifier which has been matched.
     * @param binding
     *            a binding that matches the subscription.
     */
    @WebMethod(operationName = "notifyBinding")
    void notifyBinding(@WebParam(name = "id") String subscriptionId,
    // Here, we have to use BindingWrapper and not Binding because
    // jaxb does not allow interface
                       @WebParam(name = "binding") @XmlJavaTypeAdapter(BindingWrapperAdapter.class) BindingWrapper binding);

}
