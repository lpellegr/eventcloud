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

import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingWrapperNotificationListener;
import fr.inria.eventcloud.api.wrappers.BindingWrapper;
import fr.inria.eventcloud.webservices.api.subscribers.BindingWrapperSubscriberWsApi;
import fr.inria.eventcloud.webservices.factories.WsClientFactory;

/**
 * A {@link BindingWrapperNotificationListener} which invokes a web service.
 * 
 * @author bsauvan
 */
public class WsBindingWrapperNotificationListener extends
        BindingWrapperNotificationListener {

    private static final long serialVersionUID = 130L;

    private static final Logger log =
            LoggerFactory.getLogger(WsBindingWrapperNotificationListener.class);

    private final String subscriberWsEndpointUrl;

    private BindingWrapperSubscriberWsApi subscriberWsClient;

    /**
     * Creates a {@link WsBindingWrapperNotificationListener} with the specified
     * {@link BindingWrapperSubscriberWsApi binding subscriber} web service
     * endpoint URL.
     * 
     * @param subscriberWsEndpointUrl
     *            the {@link BindingWrapperSubscriberWsApi binding subscriber}
     *            web service endpoint URL.
     */
    public WsBindingWrapperNotificationListener(String subscriberWsEndpointUrl) {
        this.subscriberWsEndpointUrl = subscriberWsEndpointUrl;
        this.subscriberWsClient =
                WsClientFactory.createWsClient(
                        BindingWrapperSubscriberWsApi.class,
                        this.subscriberWsEndpointUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNotification(SubscriptionId id, BindingWrapper binding) {
        try {
            this.subscriberWsClient.notifyBinding(id.toString(), binding);

            log.info(
                    "Subscriber {} notified about:\n {}",
                    this.subscriberWsEndpointUrl, binding);
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
