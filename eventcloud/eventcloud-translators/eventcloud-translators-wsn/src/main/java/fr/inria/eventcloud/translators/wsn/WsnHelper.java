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
package fr.inria.eventcloud.translators.wsn;

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
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.play_project.play_commons.eventformat.xml.DocumentBuilder;
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
     * Creates a {@link Subscribe WS-Notification Subscribe message} from the
     * specified subscriber's endpoint and topic information.
     * 
     * @param subscriberEndpoint
     *            subscriber's endpoint.
     * @param topic
     *            the QName associated to the topic to subscribe to.
     * 
     * @return a WS-Notification Subscribe message with the specified
     *         subscriber's endpoint and topic information.
     */
    public static Subscribe createSubscribeMessage(String subscriberEndpoint,
                                                   QName topic) {
        Subscribe subscribeRequest = new Subscribe();
        FilterType filterType = new FilterType();

        JAXBElement<TopicExpressionType> jaxbElement =
                new JAXBElement<TopicExpressionType>(
                        WsnConstants.TOPIC_EXPRESSION_QNAME,
                        TopicExpressionType.class,
                        createTopicExpressionType(topic));
        filterType.getAny().add(jaxbElement);
        subscribeRequest.setFilter(filterType);

        subscribeRequest.setConsumerReference(WSNHelper.createWSA(subscriberEndpoint));

        return subscribeRequest;
    }

    /**
     * Creates a {@link SubscribeResponse subscribe response} with the specified
     * subscription reference address and the specified {@link SubscriptionId
     * subscription identifier}.
     * 
     * @param subscriptionId
     *            the subscription identifier.
     * @param subscriptionReferenceAddress
     *            the subscription reference address.
     * 
     * @return a subscribe response with the specified subscription reference
     *         address and the specified subscription identifier.
     */
    public static SubscribeResponse createSubscribeResponse(SubscriptionId subscriptionId,
                                                            String subscriptionReferenceAddress) {
        SubscribeResponse subscribeResponse = new SubscribeResponse();
        ClassLoader classLoader =
                Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(
                    WsnHelper.class.getClassLoader());
            W3CEndpointReference subscriptionReference =
                    new W3CEndpointReferenceBuilder().address(
                            subscriptionReferenceAddress).referenceParameter(
                            createElement(subscriptionId)).build();
            subscribeResponse.setSubscriptionReference(subscriptionReference);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }

        return subscribeResponse;
    }

    /**
     * Creates a {@link SubscribeResponse subscribe response} with the specified
     * subscription reference address.
     * 
     * @param subscriptionReferenceAddress
     *            the subscription reference address.
     * 
     * @return a subscribe response with the specified subscription reference
     *         address.
     */
    public static SubscribeResponse createSubscribeResponse(String subscriptionReferenceAddress) {
        SubscribeResponse subscribeResponse = new SubscribeResponse();
        subscribeResponse.setSubscriptionReference(WSNHelper.createWSA(subscriptionReferenceAddress));
        return subscribeResponse;
    }

    /**
     * Returns the {@link SubscriptionId subscription identifier} contained into
     * the specified {@link SubscribeResponse subscribe response}.
     * 
     * @param subscribeResponse
     *            the subscribe response containing the subscription identifier.
     * 
     * @return the subscription identifier contained into the specified
     *         subscribe response.
     */
    @SuppressWarnings("unchecked")
    public static SubscriptionId getSubcriptionId(SubscribeResponse subscribeResponse) {
        W3CEndpointReference subscriptionReference =
                subscribeResponse.getSubscriptionReference();

        if (subscriptionReference != null) {
            Object referenceParameters =
                    ReflectionUtils.getFieldValue(
                            subscriptionReference, "referenceParameters");

            if (referenceParameters != null) {
                List<Element> elements =
                        (List<Element>) ReflectionUtils.getFieldValue(
                                referenceParameters, "elements");

                if (elements.size() > 0) {
                    return getSubcriptionId(elements.get(0));
                }
            }
        }

        return null;
    }

    /**
     * Creates an {@link Unsubscribe unsubscribe request} with the specified
     * {@link SubscriptionId subscription identifier}.
     * 
     * @param subscriptionId
     *            the subscription identifier.
     * 
     * @return an unsubscribe request with the specified subscription
     *         identifier.
     */
    public static Unsubscribe createUnsubscribeRequest(SubscriptionId subscriptionId) {
        Unsubscribe unsubscribeRequest = new Unsubscribe();
        unsubscribeRequest.getAny().add(createElement(subscriptionId));
        return unsubscribeRequest;
    }

    /**
     * Returns the {@link SubscriptionId subscription identifier} contained into
     * the specified {@link Unsubscribe unsubscribe request}.
     * 
     * @param unsubscribeRequest
     *            the unsubscribe request containing the subscription
     *            identifier.
     * 
     * @return the subscription identifier contained into the specified
     *         unsubscribe request.
     */
    public static SubscriptionId getSubcriptionId(Unsubscribe unsubscribeRequest) {
        if (unsubscribeRequest.getAny().size() > 0) {
            return getSubcriptionId(unsubscribeRequest.getAny().get(0));
        } else {
            return null;
        }
    }

    /**
     * Creates an {@link Element} with the specified {@link SubscriptionId
     * subscription identifier}.
     * 
     * @param subscriptionId
     *            the subscription identifier.
     * 
     * @return an Element with the specified subscription identifier.
     */
    public static Element createElement(SubscriptionId subscriptionId) {
        Document document = DocumentBuilder.createDocument();
        Element element =
                document.createElementNS(
                        WsnConstants.SUBSCRIPTION_ID_NAMESPACE,
                        WsnConstants.SUBSCRIPTION_ID_QUALIFIED_NAME);
        element.setTextContent(subscriptionId.toString());

        return element;
    }

    /**
     * Returns the {@link SubscriptionId subscription identifier} contained into
     * the specified object.
     * 
     * @param object
     *            the object containing the subscription identifier.
     * 
     * @return the subscription identifier contained into the specified object.
     */
    public static SubscriptionId getSubcriptionId(Object object) {
        if (object instanceof Element) {
            return SubscriptionId.parseSubscriptionId(((Element) object).getTextContent());
        } else {
            return null;
        }
    }

    /**
     * Creates a {@link Notify WS-Notification message} from the specified
     * subscription and producer reference endpoint, topic information and
     * payload.
     * 
     * @param subscriptionReference
     *            the subscription reference endpoint of the WS-Notification
     *            message to build.
     * @param topic
     *            the QName associated to the topic of the WS-Notification
     *            message to build.
     * @param producerReference
     *            the producer reference endpoint of the WS-Notification message
     *            to build.
     * @param payload
     *            the payload to include in the WS-Notification message to
     *            build.
     * 
     * @return a WS-Notification message with the specified producer reference
     *         endpoint, topic information and payload.
     */
    public static Notify createNotifyMessage(String subscriptionReference,
                                             QName topic,
                                             String producerReference,
                                             Object payload) {
        Notify notify = new Notify();
        NotificationMessageHolderType notificationMessage =
                new NotificationMessageHolderType();

        notificationMessage.setSubscriptionReference(createW3cEndpointReference(subscriptionReference));

        notificationMessage.setTopic(createTopicExpressionTypeWithSimpleExpressionType(topic));

        notificationMessage.setProducerReference(createW3cEndpointReference(producerReference));

        Message message = new Message();
        message.setAny(payload);
        notificationMessage.setMessage(message);

        notify.getNotificationMessage().add(notificationMessage);

        return notify;
    }

    /**
     * Creates a {@link Notify WS-Notification message} message from the
     * specified topic information and {@link CompoundEvent compound events} by
     * using the corresponding {@code translator}.
     * 
     * @param subscriptionReference
     *            the subscription reference endpoint of the WS-Notification
     *            message to build.
     * @param topic
     *            the QName associated to the topic of the WS-Notification
     *            message to build.
     * @param compoundEvents
     *            the compound events to serialize inside the message.
     * 
     * @return a WS-Notification message with the specified topic information
     *         and compound events.
     * 
     * @throws TranslationException
     *             if an error during the translation occurs.
     */
    public static Notify createNotifyMessage(String subscriptionReference,
                                             QName topic,
                                             CompoundEvent... compoundEvents)
            throws TranslationException {
        Notify notify = new Notify();

        for (CompoundEvent event : compoundEvents) {
            NotificationMessageHolderType notificationMessage =
                    translator.translate(event);
            notificationMessage.setSubscriptionReference(createW3cEndpointReference(subscriptionReference));
            if (event.getGraph().getURI().endsWith(
                    WsnConstants.SIMPLE_TOPIC_EXPRESSION_MARKER)) {
                notificationMessage.setTopic(createTopicExpressionTypeWithSimpleExpressionType(topic));
            } else {
                notificationMessage.setTopic(createTopicExpressionType(topic));
            }
            notify.getNotificationMessage().add(notificationMessage);
        }

        return notify;
    }

    /**
     * Creates a {@link Notify WS-Notification message} message from the
     * specified topic information and {@link NotificationMessageHolderType
     * notification messages}.
     * 
     * @param subscriptionReference
     *            the subscription reference endpoint of the WS-Notification
     *            message to build.
     * @param topic
     *            the QName associated to the topic of the WS-Notification
     *            message to build.
     * @param simpleTopicExpression
     *            indicates if the topic expression type must contains a
     *            simpleExpressionType element.
     * @param notificationMessages
     *            the notification messages of the WS-Notification message to
     *            build.
     * 
     * @return a WS-Notification message with the specified topic information
     *         and notification messages.
     * 
     * @throws TranslationException
     *             if an error during the translation occurs.
     */
    public static Notify createNotifyMessage(String subscriptionReference,
                                             QName topic,
                                             boolean simpleTopicExpression,
                                             NotificationMessageHolderType... notificationMessages)
            throws TranslationException {
        Notify notify = new Notify();

        for (NotificationMessageHolderType notificationMessage : notificationMessages) {
            notificationMessage.setSubscriptionReference(createW3cEndpointReference(subscriptionReference));
            if (simpleTopicExpression) {
                notificationMessage.setTopic(createTopicExpressionTypeWithSimpleExpressionType(topic));
            } else {
                notificationMessage.setTopic(createTopicExpressionType(topic));
            }
            notify.getNotificationMessage().add(notificationMessage);
        }

        return notify;
    }

    /**
     * Creates a {@link W3CEndpointReference W3C endpoint reference} from the
     * specified address.
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
     *            the QName associated to the topic of the topic expression type
     *            to build.
     * 
     * @return a topic expression type from the specified topic information.
     */
    public static TopicExpressionType createTopicExpressionType(QName topic) {
        TopicExpressionType topicExpressionType = new TopicExpressionType();
        topicExpressionType.getOtherAttributes().put(
                topic, topic.getNamespaceURI());
        topicExpressionType.setDialect(WsnConstants.TOPIC_EXPRESSION_DIALECT);
        topicExpressionType.getContent().add(
                topic.getPrefix() + ":" + topic.getLocalPart());

        return topicExpressionType;
    }

    /**
     * Creates a topic expression type with a {@code simpleExpressionType}
     * element from the specified topic information.
     * 
     * @param topic
     *            the QName associated to the topic of the topic expression type
     *            to build.
     * 
     * @return a topic expression type with a {@code simpleExpressionType}
     *         element from the specified topic information.
     */
    public static TopicExpressionType createTopicExpressionTypeWithSimpleExpressionType(QName topic) {
        TopicExpressionType topicExpressionType = new TopicExpressionType();
        topicExpressionType.setDialect(WsnConstants.SIMPLE_TOPIC_EXPRESSION_DIALECT);
        JAXBElement<QName> simpleTopicExpression =
                new JAXBElement<QName>(
                        WsnConstants.SIMPLE_TOPIC_EXPRESSION_QNAME,
                        QName.class, null, topic);
        topicExpressionType.getContent().add(simpleTopicExpression);

        return topicExpressionType;
    }

    /**
     * Returns the address declared in the specified
     * {@link W3CEndpointReference W3C endpoint reference}.
     * 
     * @param endpointReference
     *            the endpoint reference to analyze.
     * 
     * @return the address declared in the specified W3C endpoint reference.
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
     * Indicates whether the topic of the specified {@link Subscribe
     * WS-Notification Subscribe message} is defined by a
     * {@code simpleTopicExpression} element or not.
     * 
     * @param subscribe
     *            the WS-Notification Subscribe message to analyze.
     * 
     * @return true if the topic of the specified WS-Notification Subscribe
     *         message is defined by a {@code simpleTopicExpression} element,
     *         false otherwise.
     */
    public static boolean hasSimpleTopicExpression(Subscribe subscribe) {
        return hasSimpleTopicExpression(getTopicExpressionType(subscribe));
    }

    /**
     * Indicates whether the topic of the specified
     * {@link NotificationMessageHolderType WS-Notification message} is defined
     * by a {@code simpleTopicExpression} element or not.
     * 
     * @param notificationMessage
     *            the WS-Notification message to analyze.
     * 
     * @return true if the topic of the specified WS-Notification message is
     *         defined by a {@code simpleTopicExpression} element, false
     *         otherwise.
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
                                WsnConstants.SIMPLE_TOPIC_EXPRESSION_QNAME.getLocalPart());
            } catch (ClassCastException cce) {
                return false;
            }
        } else {
            throw new IllegalArgumentException("No topic content set");
        }
    }

    /**
     * Extracts and returns the topic QName contained by the specified
     * {@link Subscribe WS-Notification Subscribe message}.
     * 
     * @param subscribe
     *            the WS-Notification Subscribe message to analyze.
     * 
     * @return the topic QName contained by the specified WS-Notification
     *         Subscribe message.
     */
    public static QName getTopic(Subscribe subscribe) {
        return getTopic(getTopicExpressionType(subscribe));
    }

    /**
     * Extracts and returns the topic QName contained by the specified
     * {@link NotificationMessageHolderType WS-Notification message}.
     * 
     * @param notificationMessage
     *            the WS-Notification message to analyze.
     * 
     * @return the topic QName contained by the specified WS-Notification
     *         message.
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

                        break;
                    }
                }
            } else {
                Element topicElement = (Element) content.get(0);

                if (topicElement.getLocalName()
                        .equals(
                                WsnConstants.SIMPLE_TOPIC_EXPRESSION_QNAME.getLocalPart())) {
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

            if ((topicNamespace != null) && (!topicNamespace.endsWith("/"))) {
                topicNamespace = topicNamespace + "/";
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

            if (any != null) {
                for (Object obj : any) {
                    try {
                        return ((JAXBElement<TopicExpressionType>) obj).getValue();
                    } catch (ClassCastException e) {
                        // Not a TopicExpressionType, ignore it
                    }
                }
            }

            throw new IllegalArgumentException(
                    "No topic expression type set in the subscribe message");
        } else {
            throw new IllegalArgumentException(
                    "No filter set in the subscribe message");
        }
    }

}
