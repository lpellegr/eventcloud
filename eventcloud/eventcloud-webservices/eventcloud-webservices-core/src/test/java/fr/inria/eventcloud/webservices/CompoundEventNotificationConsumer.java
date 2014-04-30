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
package fr.inria.eventcloud.webservices;

import java.util.ArrayList;
import java.util.List;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.WsnTranslator;

/**
 * Provides an implementation of {@link NotificationConsumer} by translating all
 * incoming notifications into {@link CompoundEvent} and by storing them into an
 * in-memory list. This list can be retrieved at any time for any purpose.
 * 
 * @author lpellegr
 */
public class CompoundEventNotificationConsumer implements NotificationConsumer {

    private static Logger log =
            LoggerFactory.getLogger(CompoundEventNotificationConsumer.class);

    private final WsnTranslator translator;

    public final List<CompoundEvent> eventsReceived;

    /**
     * Creates a {@link CompoundEventNotificationConsumer}.
     */
    public CompoundEventNotificationConsumer() {
        this.translator = new WsnTranslator();
        this.eventsReceived = new ArrayList<CompoundEvent>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(Notify notify) {
        for (NotificationMessageHolderType notificationMessage : notify.getNotificationMessage()) {
            try {
                CompoundEvent event =
                        this.translator.translate(notificationMessage);

                synchronized (this.eventsReceived) {
                    this.eventsReceived.add(event);
                    this.eventsReceived.notifyAll();
                }

                log.info("New compound event received:\n{}", event);
            } catch (TranslationException e) {
                log.error("Translation failed:\n {}", e.getMessage());
                throw new IllegalArgumentException(e);
            }
        }
    }

}
