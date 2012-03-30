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
package fr.inria.eventcloud.webservices.api.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.WsNotificationLogUtils;
import fr.inria.eventcloud.translators.wsn.WsNotificationTranslator;

/**
 * XML Adapter for {@link Collection} of {@link CompoundEvent} objects.
 * 
 * @author bsauvan
 */
public class CompoundEventCollectionAdapter extends
        XmlAdapter<Notify, Collection<CompoundEvent>> {

    private static Logger log =
            LoggerFactory.getLogger(CompoundEventCollectionAdapter.class);

    private WsNotificationTranslator translator;

    public CompoundEventCollectionAdapter() {
        this.translator = new WsNotificationTranslator();
    }

    /**
     * Converts the specified collection of compound events to its notify object
     * representation.
     * 
     * @param compoundEvents
     *            the collection of compound events to be converted.
     * @return the notify object representing the specified collection of
     *         compound events.
     */
    @Override
    public Notify marshal(Collection<CompoundEvent> compoundEvents) {
        Notify notify = new Notify();

        for (CompoundEvent compoundEvent : compoundEvents) {
            try {
                NotificationMessageHolderType notificationMessage =
                        this.translator.translateCompoundEvent(compoundEvent);
                notify.getNotificationMessage().add(notificationMessage);
            } catch (TranslationException e) {
                this.logAndThrowIllegalArgumentException(e.getMessage());
            }
        }

        return notify;
    }

    /**
     * Converts the specified notify object to its corresponding collection of
     * compound events.
     * 
     * @param notify
     *            the notify object to be converted.
     * 
     * @return the collection of compound events represented by the specified
     *         notify object.
     */
    @Override
    public Collection<CompoundEvent> unmarshal(Notify notify) {
        List<CompoundEvent> compoundEvents = new ArrayList<CompoundEvent>();
        List<NotificationMessageHolderType> notificationMessages =
                notify.getNotificationMessage();

        for (NotificationMessageHolderType notificationMessage : notificationMessages) {
            try {
                WsNotificationLogUtils.logNotificationMessageHolderType(notificationMessage);

                CompoundEvent compoundEvent =
                        this.translator.translateNotification(notificationMessage);

                compoundEvents.add(compoundEvent);

                log.info("Translation output:\n{}", compoundEvent);
            } catch (TranslationException e) {
                this.logAndThrowIllegalArgumentException(e.getMessage());
            }
        }

        log.info("New notification message handled");

        return compoundEvents;
    }

    private final void logAndThrowIllegalArgumentException(String msg) {
        log.error("Translation error:");
        log.error(msg);
        throw new IllegalArgumentException(msg);
    }

}
