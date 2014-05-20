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
package fr.inria.eventcloud.webservices.listeners;

import javax.xml.ws.WebServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.SignalNotificationListener;
import fr.inria.eventcloud.webservices.api.subscribers.SignalSubscriberWsApi;
import fr.inria.eventcloud.webservices.factories.WsClientFactory;

/**
 * A {@link SignalNotificationListener} which invokes a web service.
 * 
 * @author bsauvan
 */
public class WsSignalNotificationListener extends SignalNotificationListener {

    private static final long serialVersionUID = 160L;

    private static final Logger LOG =
            LoggerFactory.getLogger(WsSignalNotificationListener.class);

    private final String subscriberWsEndpointUrl;

    private SignalSubscriberWsApi subscriberWsClient;

    /**
     * Creates a {@link WsSignalNotificationListener} with the specified
     * {@link SignalSubscriberWsApi signal subscriber} web service endpoint URL.
     * 
     * @param subscriberWsEndpointUrl
     *            the {@link SignalSubscriberWsApi signal subscriber} web
     *            service endpoint URL.
     */
    public WsSignalNotificationListener(String subscriberWsEndpointUrl) {
        this.subscriberWsEndpointUrl = subscriberWsEndpointUrl;
        this.subscriberWsClient =
                WsClientFactory.createWsClient(
                        SignalSubscriberWsApi.class,
                        this.subscriberWsEndpointUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNotification(SubscriptionId id, String eventId) {
        try {
            this.subscriberWsClient.notifySignal(id.toString());

            LOG.info(
                    "Subscriber {} notified about solution",
                    this.subscriberWsEndpointUrl);
        } catch (WebServiceException e) {
            LOG.error("Failed to send notification to "
                    + this.subscriberWsEndpointUrl, e.getCause());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSubscriberUrl() {
        return this.subscriberWsEndpointUrl;
    }

}
