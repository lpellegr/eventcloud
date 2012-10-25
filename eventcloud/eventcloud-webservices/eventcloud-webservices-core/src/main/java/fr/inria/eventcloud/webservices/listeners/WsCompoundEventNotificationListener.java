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
package fr.inria.eventcloud.webservices.listeners;

import javax.xml.ws.WebServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;
import fr.inria.eventcloud.webservices.api.subscribers.CompoundEventSubscriberWsApi;
import fr.inria.eventcloud.webservices.factories.WsClientFactory;

/**
 * A {@link CompoundEventNotificationListener} which invokes a web service.
 * 
 * @author bsauvan
 */
public class WsCompoundEventNotificationListener extends
        CompoundEventNotificationListener {

    private static final long serialVersionUID = 130L;

    private static final Logger log =
            LoggerFactory.getLogger(WsCompoundEventNotificationListener.class);

    private final String subscriberWsEndpointUrl;

    private CompoundEventSubscriberWsApi subscriberWsClient;

    /**
     * Creates a {@link WsCompoundEventNotificationListener} with the specified
     * {@link CompoundEventSubscriberWsApi compound event subscriber} web
     * service endpoint URL.
     * 
     * @param subscriberWsEndpointUrl
     *            the {@link CompoundEventSubscriberWsApi compound event
     *            subscriber} web service endpoint URL.
     */
    public WsCompoundEventNotificationListener(String subscriberWsEndpointUrl) {
        this.subscriberWsEndpointUrl = subscriberWsEndpointUrl;
        this.subscriberWsClient =
                WsClientFactory.createWsClient(
                        CompoundEventSubscriberWsApi.class,
                        this.subscriberWsEndpointUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNotification(SubscriptionId id, CompoundEvent event) {
        try {
            this.subscriberWsClient.notifyCompoundEvent(id.toString(), event);

            log.info(
                    "Subscriber {} notified about:\n {}",
                    this.subscriberWsEndpointUrl, event);
        } catch (WebServiceException e) {
            log.error("Failed to send notification to "
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
