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
package fr.inria.eventcloud.api.webservices;

import fr.inria.eventcloud.api.SubscriptionId;

/**
 * Defines the subscribe operations that can be executed on an Event-Cloud and
 * can be exposed as web services by a subscribe proxy component.
 * 
 * @author lpellegr
 * @author bsauvan
 */
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
    public SubscriptionId subscribe(String wsNotifSubscriptionPayload,
                                    String topicNameSpacePayload,
                                    String[] topicsDefinitionPayloads,
                                    String subscriberWsUrl);

    /**
     * Unsubscribes by using the specified subscription identifier.
     * 
     * @param id
     *            the subscription identifier.
     */
    public void unsubscribe(SubscriptionId id);

}
