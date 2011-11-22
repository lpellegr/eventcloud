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
package fr.inria.eventcloud.translators.wsnotif;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Subscribe;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.translators.wsnotif.notify.EventToNotificationMessageTranslator;
import fr.inria.eventcloud.translators.wsnotif.notify.NotificationMessageToEventTranslator;
import fr.inria.eventcloud.translators.wsnotif.subscribe.SubscribeToSparqlQueryTranslator;

/**
 * Translator for WS notification objects.
 * 
 * @author bsauvan
 */
public class WsNotificationTranslator {

    /**
     * Translates the specified event to its corresponding notification message.
     * 
     * @param event
     *            the event to be translated.
     * 
     * @return the notification message corresponding to the specified event.
     */
    public NotificationMessageHolderType translateEventToNotificationMessage(CompoundEvent event) {
        return new EventToNotificationMessageTranslator().translate(event);
    }

    /**
     * Translates the specified notification message to its corresponding event.
     * 
     * @param notificationMessage
     *            the notification message to be translated.
     * 
     * @return the event corresponding to the specified notification message.
     */
    public CompoundEvent translateNotificationMessageToEvent(NotificationMessageHolderType notificationMessage) {
        return new NotificationMessageToEventTranslator().translate(notificationMessage);
    }

    /**
     * Translates the specified subscribe notification to its corresponding
     * SPARQL query.
     * 
     * @param subscribe
     *            the subscribe notification to be translated.
     * 
     * @return the SPARQL query corresponding to the specified subscribe
     *         notification.
     */
    public String translateSubscribeToSparqlQuery(Subscribe subscribe) {
        return new SubscribeToSparqlQueryTranslator().translate(subscribe);
    }

}
