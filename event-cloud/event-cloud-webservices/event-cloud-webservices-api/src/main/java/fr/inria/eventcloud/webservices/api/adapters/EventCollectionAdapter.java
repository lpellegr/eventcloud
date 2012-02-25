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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.translators.wsn.WsNotificationTranslator;

/**
 * XML Adapter for {@link Collection} of {@link CompoundEvent} objects.
 * 
 * @author bsauvan
 */
public class EventCollectionAdapter extends
        XmlAdapter<Notify, Collection<CompoundEvent>> {

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
    public Notify marshal(Collection<CompoundEvent> events) throws Exception {
        Notify notify = new Notify();

        for (CompoundEvent event : events) {
            NotificationMessageHolderType notificationMessage =
                    this.translator.translateCompoundEvent(event);
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
     * 
     * @return the collection of events represented by the specified notify
     *         object.
     */
    @Override
    public Collection<CompoundEvent> unmarshal(Notify notify) throws Exception {
        List<CompoundEvent> events = new ArrayList<CompoundEvent>();
        List<NotificationMessageHolderType> notificationMessages =
                notify.getNotificationMessage();

        for (NotificationMessageHolderType notificationMessage : notificationMessages) {
            events.add(this.translator.translateNotification(notificationMessage));
        }

        return events;
    }

}
