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
package fr.inria.eventcloud.webservices.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import fr.inria.eventcloud.api.Event;

/**
 * XML Adapter for {@link Event} objects.
 * 
 * @author bsauvan
 */
public class EventAdapter extends XmlAdapter<String, Event> {
    private WsNotificationTranslatorAdapter translator;

    public EventAdapter() {
        this.translator = new WsNotificationTranslatorAdapter();
    }

    /**
     * Converts the specified event to its WS-Notification notification XML
     * payload representation.
     * 
     * @param event
     *            the event to be converted.
     * @return the WS-Notification notification XML payload representing the
     *         specified event.
     */
    @Override
    public String marshal(Event event) throws Exception {
        return this.translator.translateEventToWsNotifNotification(event);
    }

    /**
     * Converts the specified WS-Notification notification XML payload to its
     * corresponding event.
     * 
     * @param xmlPayload
     *            the WS-Notification notification XML payload to be converted.
     * @return the event represented by the specified WS-Notification
     *         notification XML payload.
     */
    @Override
    public Event unmarshal(String xmlPayload) throws Exception {
        return this.translator.translateWsNotifNotificationToEvent(xmlPayload);
    }

}
