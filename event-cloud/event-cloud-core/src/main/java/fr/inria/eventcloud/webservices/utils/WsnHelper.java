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

import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.apache.cxf.wsn.util.WSNHelper;
import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

import fr.inria.eventcloud.utils.ReflectionUtils;

/**
 * This class provides static methods to create some basic WS-Notification
 * messages but also some methods to retrieve messages content.
 * 
 * @author lpellegr
 */
public class WsnHelper {

    /**
     * Creates a {@link Subscribe} WS-Notification message from the specified
     * subscriber's endpoint and topic information.
     * 
     * @param subscriberEndpoint
     *            subscriber's endpoint.
     * @param topic
     *            the qname associated to the topic to subscribe to.
     * 
     * @return a {@link Subscribe} WS-Notification message with the specified
     *         subscriber's endpoint and topic information.
     */
    public static Subscribe createSubscribeMessage(String subscriberEndpoint,
                                                   QName topic) {
        Subscribe subscribeRequest = new Subscribe();
        FilterType filterType = new FilterType();

        JAXBElement<TopicExpressionType> jaxbElement =
                new JAXBElement<TopicExpressionType>(
                        new QName(
                                "http://docs.oasis-open.org/wsn/b-2",
                                "TopicExpression"), TopicExpressionType.class,
                        createTopicExpressionType(topic));
        filterType.getAny().add(jaxbElement);
        subscribeRequest.setFilter(filterType);

        subscribeRequest.setConsumerReference(WSNHelper.createWSA(subscriberEndpoint));

        return subscribeRequest;
    }

    public static SubscribeResponse createSubscribeResponse(String subscriptionReferenceAddress) {
        SubscribeResponse response = new SubscribeResponse();
        response.setSubscriptionReference(WSNHelper.createWSA(subscriptionReferenceAddress));
        return response;
    }

    public static Notify createNotifyMessage(String consumerAddress,
                                             QName topic, Object payload) {
        Notify result = new Notify();
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

        result.getNotificationMessage().add(notificationMessage);

        return result;
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

    /**
     * Returns the address declared in the specified
     * {@link W3CEndpointReference}.
     * 
     * @param endpointReference
     *            the endpoint reference to analyze.
     * 
     * @return the address declared in the specified
     *         {@link W3CEndpointReference}.
     */
    public static String getAddress(W3CEndpointReference endpointReference) {
        Object address =
                ReflectionUtils.getFieldValue(endpointReference, "address");

        if (address != null) {
            return (String) ReflectionUtils.getFieldValue(address, "uri");
        }

        return null;
    }

    /**
     * Extracts and returns the topic qname contained by the specified
     * {@link Subscribe} message.
     * 
     * @param subscribe
     *            the subscribe message to analyze.
     * 
     * @return the topic qname contained by the specified {@link Subscribe}
     *         message.
     */
    public static QName getTopic(Subscribe subscribe) {
        FilterType filterType = subscribe.getFilter();
        if (filterType != null) {
            List<Object> any = filterType.getAny();
            if (any.size() > 0) {
                @SuppressWarnings("unchecked")
                TopicExpressionType topicExpressionType =
                        ((JAXBElement<TopicExpressionType>) any.get(0)).getValue();

                List<Object> content = topicExpressionType.getContent();
                if (content.size() > 0) {
                    String topic =
                            ((String) content.get(0)).trim().replaceAll(
                                    "\n", "");

                    String topicLocalPart =
                            org.apache.xml.utils.QName.getLocalPart(topic);
                    // String topicPrefix =
                    // org.apache.xml.utils.QName.getPrefixPart(topic);
                    String topicNamespace = null;

                    for (Entry<QName, String> entry : topicExpressionType.getOtherAttributes()
                            .entrySet()) {
                        // TODO: compare by using prefix declaration and not
                        // local parts. It is possible to have two local part
                        // values that are the same but each one is using a
                        // different prefix. In such a case, the namespace
                        // extracted may be wrong. Before to fix this problem,
                        // issue #43 has to be resolved.
                        if (entry.getKey()
                                .getLocalPart()
                                .equals(topicLocalPart)) {
                            topicNamespace = entry.getValue();

                            if (!topicNamespace.endsWith("/")) {
                                topicNamespace = topicNamespace + "/";
                            }

                            break;
                        }
                    }

                    return new QName(topicNamespace, topicLocalPart, "topic");
                } else {
                    throw new IllegalArgumentException(
                            "No topic content set in the subscribe message");
                }
            } else {
                throw new IllegalArgumentException(
                        "No any object set in the subscribe message");
            }
        } else {
            throw new IllegalArgumentException(
                    "No filter set in the subscribe message");
        }
    }

}
