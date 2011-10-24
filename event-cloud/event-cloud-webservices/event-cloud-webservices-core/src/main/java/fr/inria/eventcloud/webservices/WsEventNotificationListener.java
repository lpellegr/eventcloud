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
package fr.inria.eventcloud.webservices;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.JaxWsClientFactoryBean;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.EventNotificationListener;
import fr.inria.eventcloud.translators.wsnotif.notify.EventToNotificationMessageTranslator;

/**
 * An {@link EventNotificationListener}
 * 
 * @author lpellegr
 */
public class WsEventNotificationListener extends EventNotificationListener {

    private static final long serialVersionUID = 1L;

    private static final Logger log =
            LoggerFactory.getLogger(WsEventNotificationListener.class);

    private static final String NOTIFY_METHOD_NAME = "Notify";

    private static EventToNotificationMessageTranslator translator =
            new EventToNotificationMessageTranslator();

    private String subscriberWsUrl;

    private transient Client wsClient;

    /**
     * Creates an {@link EventNotificationListener} with the specified
     * subscriber URL to invoke the Web service to notify.
     */
    public WsEventNotificationListener(String subscriberWsUrl) {
        this.subscriberWsUrl = subscriberWsUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNotification(SubscriptionId id, Event solution) {
        if (this.wsClient == null) {
            JaxWsClientFactoryBean clientFactory = new JaxWsClientFactoryBean();
            clientFactory.setServiceClass(NotificationConsumer.class);
            clientFactory.setAddress(this.subscriberWsUrl);
            this.wsClient = clientFactory.create();
        }

        Notify notify = new Notify();
        NotificationMessageHolderType notificationMessage =
                translator.translate(solution);
        notify.getNotificationMessage().add(notificationMessage);
        try {
            this.wsClient.invoke(NOTIFY_METHOD_NAME, new Object[] {notify});
            log.info(
                    "Web service {} invoked to notify for:\n {}",
                    this.subscriberWsUrl, solution);
        } catch (Exception e) {
            log.error(
                    "Error during the invocation of the Notify Web service ", e);
        }
    }

}
