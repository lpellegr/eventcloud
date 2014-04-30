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
package fr.inria.eventcloud.webservices.api;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import fr.inria.eventcloud.webservices.api.subscribers.BindingSubscriberWsApi;
import fr.inria.eventcloud.webservices.api.subscribers.CompoundEventSubscriberWsApi;
import fr.inria.eventcloud.webservices.api.subscribers.SignalSubscriberWsApi;

/**
 * Defines the subscribe operations that can be executed on an EventCloud and
 * can be exposed as web services by a subscribe proxy component.
 * 
 * @author bsauvan
 */
@WebService(serviceName = "EventCloudSubscribeWs", portName = "EventCloudSubscribeWsPort", name = "EventCloudSubscribeWsPortType", targetNamespace = "http://webservices.eventcloud.inria.fr/")
public interface SubscribeWsApi {

    /**
     * Subscribes to interest with the specified SPARQL query and the given
     * {@link SignalSubscriberWsApi signal subscriber web service} endpoint URL.
     * 
     * @param sparqlQuery
     *            the SPARQL query.
     * @param subscriberWsEndpointUrl
     *            the endpoint URL of the {@link SignalSubscriberWsApi signal
     *            subscriber web service} to notify.
     * 
     * @return the subscription identifier.
     */
    @WebMethod(operationName = "subscribeSignal")
    String subscribeSignal(@WebParam(name = "sparqlQuery") String sparqlQuery,
                           @WebParam(name = "subscriberWsEndpointUrl") String subscriberWsEndpointUrl);

    /**
     * Subscribes to interest with the specified SPARQL query and the given
     * {@link BindingSubscriberWsApi binding subscriber web service} endpoint
     * URL.
     * 
     * @param sparqlQuery
     *            the SPARQL query.
     * @param subscriberWsEndpointUrl
     *            the endpoint URL of the {@link BindingSubscriberWsApi binding
     *            subscriber web service} to notify.
     * 
     * @return the subscription identifier.
     */
    @WebMethod(operationName = "subscribeBinding")
    String subscribeBinding(@WebParam(name = "sparqlQuery") String sparqlQuery,
                            @WebParam(name = "subscriberWsEndpointUrl") String subscriberWsEndpointUrl);

    /**
     * Subscribes to interest with the specified SPARQL query and the given
     * {@link CompoundEventSubscriberWsApi compound event subscriber web
     * service} endpoint URL.
     * 
     * @param sparqlQuery
     *            the SPARQL query.
     * @param subscriberWsEndpointUrl
     *            the endpoint URL of the {@link CompoundEventSubscriberWsApi
     *            compound event subscriber web service} to notify.
     * 
     * @return the subscription identifier.
     */
    @WebMethod(operationName = "subscribeCompoundEvent")
    String subscribeCompoundEvent(@WebParam(name = "sparqlQuery") String sparqlQuery,
                                  @WebParam(name = "subscriberWsEndpointUrl") String subscriberWsEndpointUrl);

    /**
     * Unsubscribes by using the specified subscription identifier.
     * 
     * @param id
     *            the subscription identifier.
     */
    @WebMethod(operationName = "unsubscribe")
    void unsubscribe(@WebParam(name = "id") String id);

}
