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
package fr.inria.eventcloud.webservices.adapters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslatorImpl;
import fr.inria.eventcloud.webservices.configuration.EventCloudWsProperties;

/**
 * WS-Notification translator specialized for proxy web services.
 * 
 * @author bsauvan
 */
public class WsNotificationTranslatorAdapter extends
        WsNotificationTranslatorImpl {

    /**
     * Translates a {@code xmlPayload} standing for a WS-Notification
     * notification to an event. Because there is no XSD information that is
     * given to the method, the literals are not annotated with a datatype. The
     * event identifier associated to the event is created from the information
     * contained by the payload.
     * 
     * @param xmlPayload
     *            the XML payload.
     * 
     * @return the event corresponding to the specified WS-Notification
     *         notification XML payload.
     */
    public Event translateWsNotifNotificationToEvent(String xmlPayload) {
        // TODO: generate event identifier with the information extracted from
        // the payload

        try {
            return this.translateWsNotifNotificationToEvent(
                    xmlPayload,
                    new URI(
                            EventCloudWsProperties.EVENT_CLOUD_ID_PREFIX.getValue()
                                    + "/" + UUID.randomUUID().toString()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Translates a {@code xmlPayload} standing for a WS-Notification
     * notification to an event. Because there is no XSD information that is
     * given to the method, the literals are not annotated with a datatype.
     * 
     * @param xmlPayload
     *            the XML payload.
     * @param eventId
     *            the identifier associated to the event.
     * 
     * @return the event corresponding to the specified WS-Notification
     *         notification XML payload.
     */
    public Event translateWsNotifNotificationToEvent(String xmlPayload,
                                                     URI eventId) {
        InputStream xmlPayloadIs = this.convertStringToInputStream(xmlPayload);

        return this.translateWsNotifNotificationToEvent(
                xmlPayloadIs, null, eventId);
    }

    /**
     * Translates a {@code xmlPayload} standing for a WS-Notification
     * notification to an event where the literal values associated to the
     * quadruple contained by the event are annotated by using the
     * {@code xsdPayload}.
     * 
     * @param xmlPayload
     *            the XML payload.
     * @param xsdPayload
     *            the XSD payload.
     * @param eventId
     *            the identifier associated to the event.
     * 
     * @return the event corresponding to the specified WS-Notification
     *         notification XML payload.
     */
    public Event translateWsNotifNotificationToEvent(String xmlPayload,
                                                     String xsdPayload,
                                                     URI eventId) {
        InputStream xmlPayloadIs = this.convertStringToInputStream(xmlPayload);
        InputStream xsdPayloadIs = this.convertStringToInputStream(xsdPayload);

        return this.translateWsNotifNotificationToEvent(
                xmlPayloadIs, xsdPayloadIs, eventId);
    }

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
     * @return the SPARQL query associated to the information that have been
     *         given into parameters or {@code null} if some information are
     *         missing.
     */
    public String translateWsNotifSubscriptionToSparqlQuery(String wsNotifSubscriptionPayload,
                                                            String topicNameSpacePayload,
                                                            String... topicsDefinitionPayloads) {
        InputStream wsNotifSubscriptionPayloadIs =
                this.convertStringToInputStream(wsNotifSubscriptionPayload);
        InputStream topicNameSpacePayloadIs =
                this.convertStringToInputStream(topicNameSpacePayload);
        InputStream[] topicsDefinitionPayloadsIs =
                new InputStream[topicsDefinitionPayloads.length];
        for (int i = 0; i < topicsDefinitionPayloadsIs.length; i++) {
            topicsDefinitionPayloadsIs[i] =
                    this.convertStringToInputStream(topicsDefinitionPayloads[i]);
        }

        return this.translateWsNotifSubscriptionToSparqlQuery(
                wsNotifSubscriptionPayloadIs, topicNameSpacePayloadIs,
                topicsDefinitionPayloadsIs);
    }

    /**
     * Translates an event to a WS-Notification notification XML payload.
     * 
     * @param event
     *            the event to translate.
     * @return the WS-Notification XML payload corresponding to the event.
     */
    public String translateEventToWsNotifNotification(Event event) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.translateEventToWsNotifNotification(baos, event);

        return baos.toString();
    }

    private InputStream convertStringToInputStream(String s) {
        if (s != null) {
            return new ByteArrayInputStream(s.getBytes());
        } else {
            return null;
        }
    }

}
