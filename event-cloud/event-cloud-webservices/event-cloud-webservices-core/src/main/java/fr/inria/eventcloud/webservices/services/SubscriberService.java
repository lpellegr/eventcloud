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

import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslator;

/**
 * Defines a publish web service as defined by the WS-Notification
 * specification. All the calls to the notify request will be translated and
 * redirected to a {@link PublishProxy} in order to be published into an Event
 * Cloud.
 * 
 * @author lpellegr
 */
public class SubscriberService implements NotificationConsumer {

    private static Logger log =
            LoggerFactory.getLogger(SubscriberService.class);

    private WsNotificationTranslator translator;

    public List<Event> eventsReceived;

    public SubscriberService() {
        this.translator = new WsNotificationTranslator();
        this.eventsReceived = new ArrayList<Event>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(Notify notify) {
        for (NotificationMessageHolderType notificationMessage : notify.getNotificationMessage()) {
            Event event =
                    this.translator.translateNotificationMessageToEvent(notificationMessage);
            if (event != null) {
                this.eventsReceived.add(event);
                log.info("New event received:\n {}", event);
            }
        }
    }

}
