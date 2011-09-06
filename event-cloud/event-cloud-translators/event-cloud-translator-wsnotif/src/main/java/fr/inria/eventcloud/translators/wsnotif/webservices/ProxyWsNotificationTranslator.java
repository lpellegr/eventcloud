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
package fr.inria.eventcloud.translators.wsnotif.webservices;

import java.net.URI;

import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslator;

/**
 * Defines the methods exposed by a WS-Notification translator.
 * 
 * @author bsauvan
 */
public interface ProxyWsNotificationTranslator extends WsNotificationTranslator {

    /**
     * Translates a {@code xmlPayload} standing for a WS-Notification
     * notification to an {@link Event}. Because there is no XSD information
     * that is given to the method, the literals are not annotated with a
     * datatype.
     * 
     * @param xmlPayload
     *            the XML payload.
     * @param eventId
     *            the identifier associated to the event.
     * 
     * @return an {@link Event}.
     */
    public Event translateWsNotifNotificationToEvent(String xmlPayload,
                                                     URI eventId);

    /**
     * Translates a {@code xmlPayload} standing for a WS-Notification
     * notification to an {@link Event} where the literal values associated to
     * the quadruple contained by the event are annotated by using the
     * {@code xsdPayload}.
     * 
     * @param xmlPayload
     *            the XML payload.
     * @param xsdPayload
     *            the XSD payload.
     * @param eventId
     *            the identifier associated to the event.
     * 
     * @return an {@link Event}.
     */
    public Event translateWsNotifNotificationToEvent(String xmlPayload,
                                                     String xsdPayload,
                                                     URI eventId);

    /**
     * Translates a {@code xmlPayload} standing for a WS-Notification
     * subscription to a SPARQL query as String.
     * 
     * @param wsNotifSubscriptionPayload
     *            the WS-Notification subscription payload to translate to a
     *            SPARQL query.
     * @param topicNameSpacePayload
     *            the topicNameSpace payload that defines an event.
     * @param topicsDefinitionPayloads
     *            the definition of the topics. Several inputStream can be
     *            specified because each topic may corresponds to a message
     *            defined in a WSDL.
     * 
     * @return a SPARQL query associated to the information that have been given
     *         into parameters or {@code null} if some information are missing.
     */
    public String translateWsNotifSubscriptionToSparqlQuery(String wsNotifSubscriptionPayload,
                                                            String topicNameSpacePayload,
                                                            String... topicsDefinitionPayloads);

    /**
     * Translates an {@link Event} to a WS-Notification notification XML
     * payload.
     * 
     * @param event
     *            the event to translate.
     * @return the WS-Notification XML payload corresponding to the event.
     */
    public String translateEventToWsNotifNotification(Event event);

}
