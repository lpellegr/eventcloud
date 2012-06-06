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

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.w3c.dom.Element;

import eu.play_project.play_commons.constants.Event;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.translators.wsn.notify.CompoundEventTranslator;
import fr.inria.eventcloud.translators.wsn.notify.NotificationTranslator;
import fr.inria.eventcloud.translators.wsn.notify.SemanticCompoundEventTranslator;
import fr.inria.eventcloud.translators.wsn.notify.SemanticNotificationTranslator;
import fr.inria.eventcloud.translators.wsn.subscribe.TopicSubscriptionTranslator;

/**
 * Translator for WS-Notification messages.
 * 
 * @author bsauvan
 * @author ialshaba
 * @author lpellegr
 */
public class WsNotificationTranslator {

    /**
     * Translates the specified {@link NotificationMessageHolderType} message to
     * a {@link CompoundEvent} according to its type.
     * 
     * @param notification
     *            the subscribe message to be translated.
     * 
     * @return a compound event.
     */
    public CompoundEvent translate(NotificationMessageHolderType notification)
            throws TranslationException {
        if (notification.getMessage() != null) {
            if (notification.getMessage().getAny() != null) {
                // root element inside wsnt:Message
                Element e = (Element) notification.getMessage().getAny();

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
                    result = this.translateSemanticNotification(notification);
                } else {
                    result = this.translateNotification(notification);
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
     * Translates a notification message (i.e. a message that contains arbitrary
     * XML elements as payload) to its corresponding {@link CompoundEvent}.
     * 
     * @param notificationMessage
     *            the notification message to be translated.
     * 
     * @return the compound event corresponding to the specified notification
     *         message.
     */
    public CompoundEvent translateNotification(NotificationMessageHolderType notificationMessage)
            throws TranslationException {
        return NotificationTranslator.getInstance().translate(
                notificationMessage);
    }

    /**
     * Translates a semantic notification message (i.e. a message that contains
     * directly a semantic payload) to its corresponding {@link CompoundEvent}.
     * 
     * @param notificationMessage
     *            the notification message to be translated.
     * 
     * @return the compound event corresponding to the specified notification
     *         message.
     */
    public CompoundEvent translateSemanticNotification(NotificationMessageHolderType notificationMessage)
            throws TranslationException {
        return SemanticNotificationTranslator.getInstance().translate(
                notificationMessage);
    }

    /**
     * Translates the specified compound event (which derives from a
     * notification message with an arbitrary payload) to its corresponding
     * notification message.
     * 
     * @param event
     *            the compound event to be translated.
     * 
     * @return a notification message.
     */
    public NotificationMessageHolderType translateCompoundEvent(CompoundEvent event)
            throws TranslationException {
        return CompoundEventTranslator.getInstance().translate(event);
    }

    /**
     * Translates the specified compound event (which derives from a semantic
     * notification message) to its corresponding notification message.
     * 
     * @param event
     *            the compound event to be translated.
     * 
     * @return a semantic notification message.
     */
    public NotificationMessageHolderType translateSemanticCompoundEvent(CompoundEvent event)
            throws TranslationException {
        return SemanticCompoundEventTranslator.getInstance().translate(event);
    }

    /**
     * Translates the specified {@link Subscribe} message to its corresponding
     * SPARQL query.
     * 
     * @param subscription
     *            the subscribe message to be translated.
     * 
     * @return the SPARQL query corresponding to the specified subscribe
     *         message.
     */
    public String translate(Subscribe subscription) throws TranslationException {
        // TODO add support for both types of subscription (topic or
        // content-based) and call the correct method according to the type
        return this.translateTopicSubscription(subscription);
    }

    public String translateTopicSubscription(Subscribe subscribe)
            throws TranslationException {
        return TopicSubscriptionTranslator.getInstance().translate(subscribe);
    }

    public String translateContentBasedSubscription(Subscribe subscribe)
            throws TranslationException {
        // TODO implement
        return null;
    }

}
