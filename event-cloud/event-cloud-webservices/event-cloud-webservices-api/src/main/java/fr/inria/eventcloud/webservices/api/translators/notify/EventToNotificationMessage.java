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
package fr.inria.eventcloud.webservices.api.translators.notify;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.w3c.dom.Element;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.configuration.EventCloudProperties;

/**
 * Translator for {@link Event events} to {@link NotificationMessageHolderType
 * notification messages}.
 * 
 * @author bsauvan
 */
public class EventToNotificationMessage {

    /**
     * Translates the specified event to its corresponding notification message.
     * 
     * @param event
     *            the event to be translated.
     * @return the notification message corresponding to the specified event.
     */
    public NotificationMessageHolderType translate(Event event) {
        String subscriptionAddress = null;
        String topic = null;
        String producerAddress = null;
        List<Element> metadatas = new ArrayList<Element>();
        Element messagePayload = null;

        for (Quadruple quad : event.getQuadruples()) {
            if (quad.getPredicate()
                    .equals(
                            Node.createURI(EventCloudProperties.EVENT_CLOUD_NS.getValue()
                                    + "event/" + "nbquads"))) {
                continue;
            }

            String predicateValue = quad.getPredicate().getURI();

            if (predicateValue.equals(NotificationMessageConstants.SUBSCRIPTION_ADDRESS_TEXT)) {
                subscriptionAddress = quad.getObject().getLiteralLexicalForm();
            } else if (predicateValue.equals(NotificationMessageConstants.TOPIC_TEXT)) {
                topic = quad.getObject().getLiteralLexicalForm();
            } else if (predicateValue.equals(NotificationMessageConstants.PRODUCER_ADDRESS_TEXT)) {
                producerAddress = quad.getObject().getLiteralLexicalForm();
            } else if (predicateValue.startsWith(NotificationMessageConstants.PRODUCER_METADATA_TEXT)) {
                metadatas.add(this.getMetadataElement(quad));
            } else {
                messagePayload = this.getElement(quad, messagePayload);
            }
        }

        NotificationMessageHolderType notificationMessage =
                new NotificationMessageHolderType();

        if (subscriptionAddress != null) {
            W3CEndpointReferenceBuilder endPointReferenceBuilder =
                    new W3CEndpointReferenceBuilder();
            endPointReferenceBuilder.address(subscriptionAddress);
            notificationMessage.setSubscriptionReference(endPointReferenceBuilder.build());
        }

        if (topic != null) {
            TopicExpressionType topicExpression = new TopicExpressionType();
            topicExpression.getContent().add(topic);
            notificationMessage.setTopic(topicExpression);
        }

        if (producerAddress != null) {
            W3CEndpointReferenceBuilder endPointReferenceBuilder =
                    new W3CEndpointReferenceBuilder();
            endPointReferenceBuilder.address(producerAddress);
            for (Element metadata : metadatas) {
                endPointReferenceBuilder.metadata(metadata);
            }
            notificationMessage.setSubscriptionReference(endPointReferenceBuilder.build());
        }

        if (messagePayload != null) {
            Message message = new Message();
            message.setAny(messagePayload);
            notificationMessage.setMessage(message);
        }

        return notificationMessage;
    }

    private Element getMetadataElement(Quadruple quad) {
        org.jdom.Element element = new org.jdom.Element("");

        return null;// element;
    }

    private Element getElement(Quadruple quad, Element previousDoc) {

        return null;
    }

}
