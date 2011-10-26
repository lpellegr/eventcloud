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

import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslator;

/**
 * XML Adapter for {@link Collection} of {@link Event} objects.
 * 
 * @author bsauvan
 */
public class EventCollectionAdapter extends
        XmlAdapter<Notify, Collection<Event>> {
    private WsNotificationTranslator translator;

    public EventCollectionAdapter() {
        this.translator = new WsNotificationTranslator();
    }

    /**
     * Converts the specified collection of events to its notify object
     * representation.
     * 
     * @param events
     *            the collection of events to be converted.
     * @return the notify object representing the specified collection of
     *         events.
     */
    @Override
    public Notify marshal(Collection<Event> events) throws Exception {
        Notify notify = new Notify();

        for (Event event : events) {
            NotificationMessageHolderType notificationMessage =
                    this.translator.translateEventToNotificationMessage(event);
            notify.getNotificationMessage().add(notificationMessage);
        }

        return notify;
    }

    /**
     * Converts the specified notify object to its corresponding collection of
     * events.
     * 
     * @param notify
     *            the notify object to be converted.
     * @return the collection of events represented by the specified notify
     *         object.
     */
    @Override
    public Collection<Event> unmarshal(Notify notify) throws Exception {
        Collection<Event> events = new Collection<Event>();
        List<NotificationMessageHolderType> notificationMessages =
                notify.getNotificationMessage();

        for (NotificationMessageHolderType notificationMessage : notificationMessages) {
            events.add(this.translator.translateNotificationMessageToEvent(notificationMessage));
        }

        return events;
    }

}
