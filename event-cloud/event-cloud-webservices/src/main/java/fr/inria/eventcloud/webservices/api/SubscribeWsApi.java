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

import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.webservices.adapters.SubscriptionIdAdapter;

/**
 * Defines the subscribe operations that can be executed on an Event-Cloud and
 * can be exposed as web services by a subscribe proxy component.
 * 
 * @author lpellegr
 * @author bsauvan
 */
@WebService(serviceName = "EventCloudSubscribe", portName = "EventCloudSubscribePort", targetNamespace = "http://webservices.eventcloud.inria.fr/", name = "EventCloudSubscribePortType")
public interface SubscribeWsApi {

    /**
     * Subscribes for notifications of the specified SPARQL query.
     * 
     * @param wsNotifSubscriptionPayload
     *            the WS-Notification subscription payload representing the
     *            SPARQL query.
     * @param topicNameSpacePayload
     *            the topicNameSpace payload that defines an event.
     * @param topicsDefinitionPayloads
     *            the definition of the topics. Several string can be specified
     *            because each topic may corresponds to a message defined in a
     *            WSDL.
     * @param subscriberWsUrl
     *            the web service URL of the subscriber. The web service must
     *            implement the {@link SubscriberWsApi}.
     * 
     * @return the subscription identifier.
     */
    @WebMethod(operationName = "Subscribe")
    @XmlJavaTypeAdapter(SubscriptionIdAdapter.class)
    public SubscriptionId subscribe(@WebParam(name = "wsNotifSubscriptionPayload") String wsNotifSubscriptionPayload,
                                    @WebParam(name = "topicNameSpacePayload") String topicNameSpacePayload,
                                    @WebParam(name = "topicsDefinitionPayloads") String[] topicsDefinitionPayloads,
                                    @WebParam(name = "subscriberWsUrl") String subscriberWsUrl);

    /**
     * Unsubscribes by using the specified subscription identifier.
     * 
     * @param id
     *            the subscription identifier.
     */
    @WebMethod(operationName = "Unsubscribe")
    public void unsubscribe(@WebParam(name = "id") @XmlJavaTypeAdapter(SubscriptionIdAdapter.class) SubscriptionId id);

}
