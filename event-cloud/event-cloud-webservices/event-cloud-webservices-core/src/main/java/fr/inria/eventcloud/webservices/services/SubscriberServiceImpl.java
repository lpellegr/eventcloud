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
package fr.inria.eventcloud.webservices.services;

import java.util.ArrayList;
import java.util.List;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.WsNotificationTranslator;

/**
 * Defines a publish web service as defined by the WS-Notification
 * specification. All the calls to the notify request will be translated and
 * redirected to a {@link PublishProxy} in order to be published into an Event
 * Cloud.
 * 
 * @author lpellegr
 */
public class SubscriberServiceImpl implements NotificationConsumer {

    private static Logger log =
            LoggerFactory.getLogger(SubscriberServiceImpl.class);

    private final WsNotificationTranslator translator;

    public final List<CompoundEvent> eventsReceived;

    public SubscriberServiceImpl() {
        this.translator = new WsNotificationTranslator();
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
                    this.eventsReceived.notifyAll();
                    this.eventsReceived.add(event);
                }

                log.info("New compound event received:\n{}", event);
            } catch (TranslationException e) {
                log.error("Translation failed:\n {}", e.getMessage());
                throw new IllegalArgumentException(e);
            }
        }
    }

}
