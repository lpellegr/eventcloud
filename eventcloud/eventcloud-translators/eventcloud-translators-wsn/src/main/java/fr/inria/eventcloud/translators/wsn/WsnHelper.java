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
package fr.inria.eventcloud.translators.wsn;

import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.apache.cxf.wsn.util.WSNHelper;
import org.apache.xerces.dom.ElementNSImpl;
import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.w3c.dom.Element;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.utils.ReflectionUtils;

/**
 * This class provides static methods to create some basic WS-Notification
 * messages but also some methods to retrieve messages content.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class WsnHelper {

    private static final transient WsnTranslator translator =
            new WsnTranslator();

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

    /**
     * Creates a {@link SubscribeResponse} with the specified subscription
     * reference address and the specified {@link SubscriptionId subscription
     * identifier}.
     * 
     * @param subscriptionId
     *            the {@link SubscriptionId subscription identifier}.
     * @param subscriptionReferenceAddress
     *            the subscription reference address.
     * 
     * @return a {@link SubscribeResponse} with the specified subscription
     *         reference address and the specified {@link SubscriptionId
     *         subscription identifier}.
     */
    public static SubscribeResponse createSubscribeResponse(SubscriptionId subscriptionId,
                                                            String subscriptionReferenceAddress) {
        SubscribeResponse subscribeResponse =
                createSubscribeResponse(subscriptionReferenceAddress);
        subscribeResponse.getAny().add(createJaxbElement(subscriptionId));
        return subscribeResponse;
    }

    /**
     * Creates a {@link SubscribeResponse} with the specified subscription
     * reference address.
     * 
     * @param subscriptionReferenceAddress
     *            the subscription reference address.
     * 
     * @return a {@link SubscribeResponse} with the specified subscription
     *         reference address.
     */
    public static SubscribeResponse createSubscribeResponse(String subscriptionReferenceAddress) {
        SubscribeResponse subscribeResponse = new SubscribeResponse();
        subscribeResponse.setSubscriptionReference(WSNHelper.createWSA(subscriptionReferenceAddress));
        return subscribeResponse;
    }

    /**
     * Returns the {@link SubscriptionId subscription identifier} contained into
     * the specified {@link SubscribeResponse}.
     * 
     * @param subscribeResponse
     *            the {@link SubscribeResponse} containing the
     *            {@link SubscriptionId subscription identifier}.
     * 
     * @return the {@link SubscriptionId subscription identifier} contained into
     *         the specified {@link SubscribeResponse}.
     */
    public static SubscriptionId getSubcriptionId(SubscribeResponse subscribeResponse) {
        if (subscribeResponse.getAny().size() > 0) {
            return getSubcriptionId(subscribeResponse.getAny().get(0));
        } else {
            return null;
        }
    }

    /**
     * Creates an {@link Unsubscribe unsubscribe request} with the specified
     * {@link SubscriptionId subscription identifier}.
     * 
     * @param subscriptionId
     *            the {@link SubscriptionId subscription identifier}.
     * 
     * @return an {@link Unsubscribe unsubscribe request} with the specified
     *         {@link SubscriptionId subscription identifier}.
     */
    public static Unsubscribe createUnsubscribeRequest(SubscriptionId subscriptionId) {
        Unsubscribe unsubscribeRequest = new Unsubscribe();
        unsubscribeRequest.getAny().add(createJaxbElement(subscriptionId));
        return unsubscribeRequest;
    }

    /**
     * Returns the {@link SubscriptionId subscription identifier} contained into
     * the specified {@link Unsubscribe unsubscribe request}.
     * 
     * @param unsubscribeRequest
     *            the {@link Unsubscribe unsubscribe request} containing the
     *            {@link SubscriptionId subscription identifier}.
     * 
     * @return the {@link SubscriptionId subscription identifier} contained into
     *         the specified {@link Unsubscribe unsubscribe request}.
     */
    public static SubscriptionId getSubcriptionId(Unsubscribe unsubscribeRequest) {
        if (unsubscribeRequest.getAny().size() > 0) {
            return getSubcriptionId(unsubscribeRequest.getAny().get(0));
        } else {
            return null;
        }
    }

    /**
     * Creates a {@link JAXBElement} with the specified {@link SubscriptionId
     * subscription identifier}.
     * 
     * @param subscriptionId
     *            the {@link SubscriptionId subscription identifier}.
     * 
     * @return a {@link JAXBElement} with the specified {@link SubscriptionId
     *         subscription identifier}.
     */
    public static JAXBElement<String> createJaxbElement(SubscriptionId subscriptionId) {
        return new JAXBElement<String>(
                new QName("http://evencloud.inria.fr", "SubscriptionId"),
                String.class, subscriptionId.toString());
    }

    /**
     * Returns the {@link SubscriptionId subscription identifier} contained into
     * the specified object.
     * 
     * @param any
     *            the object containing the {@link SubscriptionId subscription
     *            identifier}.
     * 
     * @return the {@link SubscriptionId subscription identifier} contained into
     *         the specified object.
     */
    @SuppressWarnings("unchecked")
    public static SubscriptionId getSubcriptionId(Object any) {
        if (any instanceof JAXBElement<?>) {
            return SubscriptionId.parseSubscriptionId(((JAXBElement<String>) any).getValue());
        } else if (any instanceof ElementNSImpl) {
            return SubscriptionId.parseSubscriptionId(((ElementNSImpl) any).getTextContent());
        } else {
            return null;
        }
    }

    /**
     * Creates a notify message from the specified subscription and producer
     * reference endpoint, topic information and payload.
     * 
     * @param subscriptionReference
     *            the subscription reference endpoint of the notify message to
     *            build.
     * @param topic
     *            the qname associated to the topic of the notify message to
     *            build.
     * @param producerReference
     *            the producer reference endpoint of the notify message to
     *            build.
     * @param payload
     *            the payload to include in the notify message to build.
     * 
     * @return a notify message with the specified producer reference endpoint,
     *         topic information and payload.
     */
    public static Notify createNotifyMessage(String subscriptionReference,
                                             QName topic,
                                             String producerReference,
                                             Object payload) {
        Notify notify = new Notify();
        NotificationMessageHolderType notificationMessage =
                new NotificationMessageHolderType();

        notificationMessage.setSubscriptionReference(createW3cEndpointReference(subscriptionReference));

        notificationMessage.setTopic(createTopicExpressionType(topic));

        notificationMessage.setProducerReference(createW3cEndpointReference(producerReference));

        Message message = new Message();
        message.setAny(payload);
        notificationMessage.setMessage(message);

        notify.getNotificationMessage().add(notificationMessage);

        return notify;
    }

    /**
     * Creates a notify message from the specified topic information and
     * {@link CompoundEvent}s by using the corresponding {@code translator}.
     * 
     * @param subscriptionReference
     *            the subscription reference endpoint of the notify message to
     *            build.
     * @param topic
     *            the qname associated to the topic of the notify message to
     *            build.
     * @param compoundEvents
     *            the compound events to serialize inside the message.
     * 
     * @return a notify message with the specified topic information and
     *         {@link CompoundEvent}s.
     * @throws TranslationException
     *             if an error during the translation occurs.
     */
    public static Notify createNotifyMessage(String subscriptionReference,
                                             QName topic,
                                             CompoundEvent... compoundEvents)
            throws TranslationException {
        Notify notify = new Notify();

        for (CompoundEvent event : compoundEvents) {
            NotificationMessageHolderType message = translator.translate(event);
            message.setSubscriptionReference(createW3cEndpointReference(subscriptionReference));
            if (event.getGraph().getURI().endsWith(
                    WsnTranslatorConstants.SIMPLE_TOPIC_EXPRESSION_MARKER)) {
                message.setTopic(createTopicExpressionTypeWithSimpleExpressionType(topic));
            } else {
                message.setTopic(createTopicExpressionType(topic));
            }
            notify.getNotificationMessage().add(message);
        }

        return notify;
    }

    /**
     * Creates a W3C endpoint reference from the specified address.
     * 
     * @param address
     *            the address of the W3C endpoint reference to build.
     * 
     * @return a W3C endpoint reference from the specified address.
     */
    public static W3CEndpointReference createW3cEndpointReference(String address) {
        W3CEndpointReferenceBuilder endPointReferenceBuilder =
                new W3CEndpointReferenceBuilder();

        endPointReferenceBuilder.address(address);

        return endPointReferenceBuilder.build();
    }

    /**
     * Creates a topic expression type from the specified topic information.
     * 
     * @param topic
     *            the qname associated to the topic of the topic expression type
     *            to build.
     * 
     * @return a topic expression type from the specified topic information.
     */
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
     * Creates a topic expression type with a {@code simpleExpressionType}
     * element from the specified topic information.
     * 
     * @param topic
     *            the qname associated to the topic of the topic expression type
     *            to build.
     * 
     * @return a topic expression type with a {@code simpleExpressionType}
     *         element from the specified topic information.
     */
    public static TopicExpressionType createTopicExpressionTypeWithSimpleExpressionType(QName topic) {
        TopicExpressionType topicExpressionType = new TopicExpressionType();
        topicExpressionType.setDialect("http://www.w3.org/TR/1999/REC-xpath-19991116");
        JAXBElement<QName> simpleTopicExpression =
                new JAXBElement<QName>(
                        WsnTranslatorConstants.SIMPLE_TOPIC_EXPRESSION_QNAME,
                        QName.class, null, topic);
        topicExpressionType.getContent().add(simpleTopicExpression);

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
     * Indicates whether the topic of the specified {@link Subscribe} message is
     * defined by a {@code simpleTopicExpression} element or not.
     * 
     * @param subscribe
     *            the subscribe message to analyze.
     * 
     * @return true if the topic of the specified {@link Subscribe} message is
     *         defined by a {@code simpleTopicExpression} element, false
     *         otherwise.
     */
    public static boolean hasSimpleTopicExpression(Subscribe subscribe) {
        return hasSimpleTopicExpression(getTopicExpressionType(subscribe));
    }

    /**
     * Indicates whether the topic of the specified
     * {@link NotificationMessageHolderType} is defined by a
     * {@code simpleTopicExpression} element or not.
     * 
     * @param notificationMessage
     *            the {@link NotificationMessageHolderType} to analyze.
     * 
     * @return true if the topic of the specified
     *         {@link NotificationMessageHolderType} is defined by a
     *         {@code simpleTopicExpression} element, false otherwise.
     */
    public static boolean hasSimpleTopicExpression(NotificationMessageHolderType notificationMessage) {
        return hasSimpleTopicExpression(notificationMessage.getTopic());
    }

    private static boolean hasSimpleTopicExpression(TopicExpressionType topicExpressionType) {
        List<Object> content = topicExpressionType.getContent();
        if (content.size() > 0) {
            try {
                Element topicElement = (Element) content.get(0);
                return topicElement.getLocalName()
                        .equals(
                                WsnTranslatorConstants.SIMPLE_TOPIC_EXPRESSION_QNAME.getLocalPart());
            } catch (ClassCastException cce) {
                return false;
            }
        } else {
            throw new IllegalArgumentException("No topic content set");
        }
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
        return getTopic(getTopicExpressionType(subscribe));
    }

    /**
     * Extracts and returns the topic qname contained by the specified
     * {@link NotificationMessageHolderType}.
     * 
     * @param notificationMessage
     *            the {@link NotificationMessageHolderType} to analyze.
     * 
     * @return the topic qname contained by the specified
     *         {@link NotificationMessageHolderType}.
     */
    public static QName getTopic(NotificationMessageHolderType notificationMessage) {
        return getTopic(notificationMessage.getTopic());
    }

    private static QName getTopic(TopicExpressionType topicExpressionType) {
        List<Object> content = topicExpressionType.getContent();
        if (content.size() > 0) {
            String topicLocalPart = null;
            String topicPrefix = null;
            String topicNamespace = null;

            if (content.get(0) instanceof String) {
                String topic =
                        ((String) content.get(0)).trim().replaceAll("\n", "");

                topicLocalPart = org.apache.xml.utils.QName.getLocalPart(topic);
                topicPrefix = org.apache.xml.utils.QName.getPrefixPart(topic);

                for (Entry<QName, String> entry : topicExpressionType.getOtherAttributes()
                        .entrySet()) {
                    // TODO: compare by using prefix declaration and not
                    // local parts. It is possible to have two local part
                    // values that are the same but each one is using a
                    // different prefix. In such a case, the namespace
                    // extracted may be wrong. Before to fix this problem,
                    // issue #43 has to be resolved.
                    if (entry.getKey().getLocalPart().equals(topicLocalPart)
                            || entry.getKey()
                                    .getLocalPart()
                                    .equals(topicPrefix)) {
                        topicNamespace = entry.getValue();

                        if (!topicNamespace.endsWith("/")) {
                            topicNamespace = topicNamespace + "/";
                        }

                        break;
                    }
                }
            } else {
                Element topicElement = (Element) content.get(0);

                if (topicElement.getLocalName()
                        .equals(
                                WsnTranslatorConstants.SIMPLE_TOPIC_EXPRESSION_QNAME.getLocalPart())) {
                    String topic =
                            topicElement.getTextContent().trim().replaceAll(
                                    "\n", "");
                    topicLocalPart =
                            org.apache.xml.utils.QName.getLocalPart(topic);
                    topicPrefix =
                            org.apache.xml.utils.QName.getPrefixPart(topic);
                    topicNamespace =
                            topicElement.lookupNamespaceURI(topicPrefix);
                } else {
                    throw new IllegalArgumentException(
                            "Unable to extract topic content");
                }
            }

            return new QName(topicNamespace, topicLocalPart, topicPrefix);
        } else {
            throw new IllegalArgumentException("No topic content set");
        }
    }

    @SuppressWarnings("unchecked")
    private static TopicExpressionType getTopicExpressionType(Subscribe subscribe) {
        FilterType filterType = subscribe.getFilter();
        if (filterType != null) {
            List<Object> any = filterType.getAny();
            if (any.size() > 0) {
                return ((JAXBElement<TopicExpressionType>) any.get(0)).getValue();
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
