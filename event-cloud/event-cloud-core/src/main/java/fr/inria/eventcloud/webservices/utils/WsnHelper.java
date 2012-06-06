/**
 * Copyright (c) 2011-2012 INRIA.
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
package fr.inria.eventcloud.webservices.utils;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

/**
 * This class provides static methods to create some basic WS-Notification
 * messages.
 * 
 * @author lpellegr
 */
public class WsnHelper {

    public static Notify createNotifyMessage(String consumerAddress,
                                             QName topic, Object payload) {
        Notify notify = new Notify();
        NotificationMessageHolderType notificationMessage =
                new NotificationMessageHolderType();

        notificationMessage.setTopic(createTopicExpressionType(topic));

        W3CEndpointReferenceBuilder endPointReferenceBuilder =
                new W3CEndpointReferenceBuilder();
        endPointReferenceBuilder.address(consumerAddress);
        notificationMessage.setProducerReference(endPointReferenceBuilder.build());

        Message message = new Message();
        message.setAny(payload);
        notificationMessage.setMessage(message);

        notify.getNotificationMessage().add(notificationMessage);

        return notify;
    }

    public static TopicExpressionType createTopicExpressionType(QName topic) {
        TopicExpressionType topicExpressionType = new TopicExpressionType();
        topicExpressionType.getOtherAttributes().put(
                topic, topic.getNamespaceURI());
        topicExpressionType.setDialect("http://docs.oasis-open.org/wsn/t-1/TopicExpression/Concrete");
        topicExpressionType.getContent().add(
                topic.getPrefix() + ":" + topic.getLocalPart());

        return topicExpressionType;
    }

}
