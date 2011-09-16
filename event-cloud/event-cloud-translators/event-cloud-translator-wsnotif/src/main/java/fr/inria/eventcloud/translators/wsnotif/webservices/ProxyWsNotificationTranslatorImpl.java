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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslatorImpl;

/**
 * A concrete implementation of {@link ProxyWsNotificationTranslator}.
 * 
 * @author bsauvan
 */
public class ProxyWsNotificationTranslatorImpl extends
        WsNotificationTranslatorImpl implements ProxyWsNotificationTranslator {

    /**
     * {@inheritDoc}
     */
    @Override
    public Event translateWsNotifNotificationToEvent(String xmlPayload) {
        // TODO: generate event identifier with the information extracted from
        // the payload

        try {
            return this.translateWsNotifNotificationToEvent(
                    xmlPayload,
                    new URI(
                            EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue()
                                    + "/" + UUID.randomUUID().toString()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Event translateWsNotifNotificationToEvent(String xmlPayload,
                                                     URI eventId) {
        InputStream xmlPayloadIs = this.convertStringToInputStream(xmlPayload);

        return this.translateWsNotifNotificationToEvent(
                xmlPayloadIs, null, eventId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Event translateWsNotifNotificationToEvent(String xmlPayload,
                                                     String xsdPayload,
                                                     URI eventId) {
        InputStream xmlPayloadIs = this.convertStringToInputStream(xmlPayload);
        InputStream xsdPayloadIs = this.convertStringToInputStream(xsdPayload);

        return this.translateWsNotifNotificationToEvent(
                xmlPayloadIs, xsdPayloadIs, eventId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    public String translateEventToWsNotifNotification(Event event) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.translateEventToWsNotifNotification(baos, event);

        return baos.toString();
    }

    private InputStream convertStringToInputStream(String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

}
