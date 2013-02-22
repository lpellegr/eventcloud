/**
 * Copyright (c) 2011-2013 INRIA.
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

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.w3c.dom.Element;

import eu.play_project.play_commons.constants.Event;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.translators.wsn.notify.SemanticCompoundEventTranslator;
import fr.inria.eventcloud.translators.wsn.notify.SemanticNotificationTranslator;
import fr.inria.eventcloud.translators.wsn.notify.XmlCompoundEventTranslator;
import fr.inria.eventcloud.translators.wsn.notify.XmlNotificationTranslator;
import fr.inria.eventcloud.translators.wsn.subscribe.TopicSubscriptionTranslator;

/**
 * Translator for WS-Notification messages.
 * 
 * @author bsauvan
 * @author ialshaba
 * @author lpellegr
 */
public class WsnTranslator {

    /**
     * Translates the specified {@link NotificationMessageHolderType
     * WS-Notification message} to a {@link CompoundEvent compound event}
     * according to its type.
     * 
     * @param notificationMessage
     *            the WS-Notification message to be translated.
     * 
     * @return the compound event corresponding to the specified WS-Notification
     *         message.
     */
    public CompoundEvent translate(NotificationMessageHolderType notificationMessage)
            throws TranslationException {
        if (notificationMessage.getMessage() != null) {
            if (notificationMessage.getMessage().getAny() != null) {
                // root element inside wsnt:Message
                Element e = (Element) notificationMessage.getMessage().getAny();

                CompoundEvent result;

                // checks which translator to use according to content type
                // (this is supposed to ease the transition because all sources
                // are not yet publishing semantic payloads)
                if (e.getNamespaceURI().equals(
                        Event.WSN_MSG_ELEMENT.getNamespaceURI())
                        && e.getNodeName().equals(
                                Event.WSN_MSG_ELEMENT.getPrefix() + ":"
                                        + Event.WSN_MSG_ELEMENT.getLocalPart())
                        && e.getAttributeNS(
                                Event.WSN_MSG_ELEMENT.getNamespaceURI(),
                                Event.WSN_MSG_SYNTAX_ATTRIBUTE) != null) {
                    // message content is a native semantic payload
                    result =
                            this.translateSemanticNotification(notificationMessage);
                } else {
                    result = this.translateXmlNotification(notificationMessage);
                }

                return result;
            } else {
                throw new TranslationException(
                        "No any object set in the notification message");
            }
        } else {
            throw new TranslationException(
                    "No message object set in the notification message");
        }
    }

    /**
     * Translates a {@link NotificationMessageHolderType WS-Notification
     * message} (i.e. a message that contains arbitrary XML elements as payload)
     * to its corresponding {@link CompoundEvent compound event}.
     * 
     * @param notificationMessage
     *            the WS-Notification message to be translated.
     * 
     * @return the compound event corresponding to the specified WS-Notification
     *         message.
     */
    public CompoundEvent translateXmlNotification(NotificationMessageHolderType notificationMessage)
            throws TranslationException {
        return XmlNotificationTranslator.getInstance().translate(
                notificationMessage);
    }

    /**
     * Translates a semantic {@link NotificationMessageHolderType
     * WS-Notification message} (i.e. a message that contains directly a
     * semantic payload) to its corresponding {@link CompoundEvent compound
     * event}.
     * 
     * @param notificationMessage
     *            the WS-Notification message to be translated.
     * 
     * @return the compound event corresponding to the specified WS-Notification
     *         message.
     */
    public CompoundEvent translateSemanticNotification(NotificationMessageHolderType notificationMessage)
            throws TranslationException {
        return SemanticNotificationTranslator.getInstance().translate(
                notificationMessage);
    }

    /**
     * Translates the specified {@link CompoundEvent compound event} to its
     * corresponding {@link NotificationMessageHolderType WS-Notification
     * message}.
     * 
     * @param event
     *            the compound event to be translated.
     * 
     * @return the notification message corresponding to the specified
     *         WS-Notification message.
     */
    public NotificationMessageHolderType translate(CompoundEvent event)
            throws TranslationException {
        if (event.getGraph().getURI().contains(
                WsnConstants.XML_TRANSLATION_MARKER)) {
            return this.translateXmlCompoundEvent(event);
        } else {
            return this.translateSemanticCompoundEvent(event);
        }
    }

    /**
     * Translates the specified {@link CompoundEvent compound event} (which
     * derives from a {@link NotificationMessageHolderType WS-Notification
     * message} with an arbitrary payload) to its corresponding
     * {@link NotificationMessageHolderType WS-Notification message}.
     * 
     * @param event
     *            the compound event to be translated.
     * 
     * @return the WS-Notification message corresponding to the specified
     *         compound event.
     */
    public NotificationMessageHolderType translateXmlCompoundEvent(CompoundEvent event)
            throws TranslationException {
        return XmlCompoundEventTranslator.getInstance().translate(event);
    }

    /**
     * Translates the specified {@link CompoundEvent compound event} (which
     * derives from a semantic WS-Notification message) to its corresponding
     * {@link NotificationMessageHolderType WS-Notification message}.
     * 
     * @param event
     *            the compound event to be translated.
     * 
     * @return the semantic {@link NotificationMessageHolderType WS-Notification
     *         message} corresponding to the specified compound event.
     */
    public NotificationMessageHolderType translateSemanticCompoundEvent(CompoundEvent event)
            throws TranslationException {
        return SemanticCompoundEventTranslator.getInstance().translate(event);
    }

    /**
     * Translates the specified {@link Subscribe WS-Notification Subscribe
     * message} to its corresponding SPARQL query.
     * 
     * @param subscribe
     *            the WS-Notification Subscribe message to be translated.
     * 
     * @return the SPARQL query corresponding to the specified WS-Notification
     *         Subscribe message.
     */
    public String translate(Subscribe subscribe) throws TranslationException {
        // TODO add support for both types of subscription (topic or
        // content-based) and call the correct method according to the type
        return this.translateTopicSubscription(subscribe);
    }

    /**
     * Translates the specified {@link Subscribe WS-Notification Subscribe
     * message} (which is supposed to be a topic based subscription) to its
     * corresponding SPARQL query.
     * 
     * @param subscribe
     *            the WS-Notification Subscribe message to be translated.
     * 
     * @return the SPARQL query corresponding to the specified WS-Notification
     *         Subscribe message.
     */
    public String translateTopicSubscription(Subscribe subscribe)
            throws TranslationException {
        return TopicSubscriptionTranslator.getInstance().translate(subscribe);
    }

    /**
     * Translates the specified {@link Subscribe WS-Notification Subscribe
     * message} (which is supposed to be a content based subscription) to its
     * corresponding SPARQL query.
     * 
     * @param subscribe
     *            the WS-Notification Subscribe message to be translated.
     * 
     * @return the SPARQL query corresponding to the specified WS-Notification
     *         Subscribe message.
     */
    public String translateContentBasedSubscription(Subscribe subscribe) {
        // TODO implement
        throw new UnsupportedOperationException();
    }
}
