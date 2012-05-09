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

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.cxf.wsn.util.WSNHelper;
import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.translators.wsn.notify.SemanticCompoundEventTranslator;

/**
 * This class provides static methods to create some basic WS-Notification
 * messages.
 * 
 * @author lpellegr
 */
public class WsNotificationMessageBuilder {

    /**
     * Creates a {@link Subscribe} WS-Notification message from the specified
     * subscriber's endpoint and topic information.
     * 
     * @param subscriberEndpoint
     *            subscriber's endpoint.
     * @param topicNamespace
     *            namespace associated to the topic the subscriber subscribes
     *            to.
     * @param topicNsPrefix
     *            prefix associated to topic namespace.
     * @param topicLocalPart
     *            local part associated to the topic.
     * 
     * @return a {@link Subscribe} WS-Notification message with the specified
     *         subscriber's endpoint and topic information.
     */
    public static Subscribe createSubscribeMessage(String subscriberEndpoint,
                                                   String topicNamespace,
                                                   String topicNsPrefix,
                                                   String topicLocalPart) {
        Subscribe subscribeRequest = new Subscribe();
        FilterType filterType = new FilterType();

        JAXBElement<TopicExpressionType> jaxbElement =
                new JAXBElement<TopicExpressionType>(
                        new QName(
                                "http://docs.oasis-open.org/wsn/b-2",
                                "TopicExpression"), TopicExpressionType.class,
                        createTopicExpressionType(
                                topicNamespace, topicNsPrefix, topicLocalPart));
        filterType.getAny().add(jaxbElement);
        subscribeRequest.setFilter(filterType);

        subscribeRequest.setConsumerReference(WSNHelper.createWSA(subscriberEndpoint));

        return subscribeRequest;
    }

    /**
     * Creates a notify message from the specified topic information and
     * {@link CompoundEvent}s. <strong>This method creates a new
     * {@link SemanticCompoundEventTranslator} each time it is invoked</strong>.
     * 
     * @param topicNamespace
     *            namespace associated to the topic the subscriber subscribes
     *            to.
     * @param topicNsPrefix
     *            prefix associated to topic namespace.
     * @param topicLocalPart
     *            local part associated to the topic.
     * @param compoundEvents
     *            the compound events to serialize inside the message.
     * 
     * @return a notify message with the specified topic information and
     *         {@link CompoundEvent}s.
     */
    public static Notify createNotifyMessage(String topicNamespace,
                                             String topicNsPrefix,
                                             String topicLocalPart,
                                             CompoundEvent... compoundEvents) {
        return createNotifyMessage(
                new SemanticCompoundEventTranslator(), topicNamespace,
                topicNsPrefix, topicLocalPart, compoundEvents);
    }

    /**
     * Creates a notify message from the specified topic information and
     * {@link CompoundEvent}s by using the given {@code translator}.
     * 
     * @param translator
     *            the translator used to translate compound events.
     * @param topicNamespace
     *            namespace associated to the topic the subscriber subscribes
     *            to.
     * @param topicNsPrefix
     *            prefix associated to topic namespace.
     * @param topicLocalPart
     *            local part associated to the topic.
     * @param compoundEvents
     *            the compound events to serialize inside the message.
     * 
     * @return a notify message with the specified topic information and
     *         {@link CompoundEvent}s.
     */
    public static Notify createNotifyMessage(SemanticCompoundEventTranslator translator,
                                             String topicNamespace,
                                             String topicNsPrefix,
                                             String topicLocalPart,
                                             CompoundEvent... compoundEvents) {
        Notify result = new Notify();

        for (CompoundEvent event : compoundEvents) {
            try {
                NotificationMessageHolderType message =
                        translator.translate(event);
                message.setTopic(createTopicExpressionType(
                        topicNamespace, topicNsPrefix, topicLocalPart));
                result.getNotificationMessage().add(message);
            } catch (TranslationException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private static TopicExpressionType createTopicExpressionType(String topicNamespace,
                                                                 String topicNsPrefix,
                                                                 String topicLocalPart) {
        TopicExpressionType topicExpressionType = new TopicExpressionType();
        topicExpressionType.getOtherAttributes().put(
                new QName(topicNamespace, topicLocalPart, topicNsPrefix),
                topicNamespace);

        topicExpressionType.getContent().add(
                topicNsPrefix + ":" + topicLocalPart);

        return topicExpressionType;
    }

}
