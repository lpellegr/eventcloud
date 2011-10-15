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
package fr.inria.eventcloud.webservices.api.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;

import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslator;

/**
 * XML Adapter for {@link Event} objects.
 * 
 * @author bsauvan
 */
public class EventAdapter extends
        XmlAdapter<NotificationMessageHolderType, Event> {

    private WsNotificationTranslator translator;

    public EventAdapter() {
        this.translator = new WsNotificationTranslator();
    }

    /**
     * Converts the specified event to its WS-Notification representation.
     * 
     * @param event
     *            the event to be converted.
     * 
     * @return the WS-Notification message representation.
     */
    @Override
    public NotificationMessageHolderType marshal(Event event) throws Exception {
        return this.translator.translateEventToNotificationMessage(event);
    }

    /**
     * Converts the specified WS-Notification message to an {@link Event}
     * representation.
     * 
     * @param notificationMessage
     *            the WS-Notification message.
     * 
     * @return the event representation.
     */
    @Override
    public Event unmarshal(NotificationMessageHolderType notificationMessage)
            throws Exception {
        return this.translator.translateNotificationMessageToEvent(notificationMessage);
    }

}
