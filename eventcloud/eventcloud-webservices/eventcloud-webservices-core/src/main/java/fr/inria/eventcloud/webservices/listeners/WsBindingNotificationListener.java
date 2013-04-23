/**
 * Copyright (c) 2011-2013 INRIA.
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

import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.wrappers.BindingWrapper;
import fr.inria.eventcloud.webservices.api.subscribers.BindingSubscriberWsApi;
import fr.inria.eventcloud.webservices.factories.WsClientFactory;

/**
 * A {@link BindingNotificationListener} which invokes a web service.
 * 
 * @author bsauvan
 */
public class WsBindingNotificationListener extends BindingNotificationListener {

    private static final long serialVersionUID = 150L;

    private static final Logger log =
            LoggerFactory.getLogger(WsBindingNotificationListener.class);

    private final String subscriberWsEndpointUrl;

    private BindingSubscriberWsApi subscriberWsClient;

    /**
     * Creates a {@link WsBindingNotificationListener} with the specified
     * {@link BindingSubscriberWsApi binding subscriber} web service endpoint
     * URL.
     * 
     * @param subscriberWsEndpointUrl
     *            the {@link BindingSubscriberWsApi binding subscriber} web
     *            service endpoint URL.
     */
    public WsBindingNotificationListener(String subscriberWsEndpointUrl) {
        this.subscriberWsEndpointUrl = subscriberWsEndpointUrl;
        this.subscriberWsClient =
                WsClientFactory.createWsClient(
                        BindingSubscriberWsApi.class,
                        this.subscriberWsEndpointUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNotification(SubscriptionId id, Binding binding) {
        try {
            this.subscriberWsClient.notifyBinding(
                    id.toString(), new BindingWrapper(binding));

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
