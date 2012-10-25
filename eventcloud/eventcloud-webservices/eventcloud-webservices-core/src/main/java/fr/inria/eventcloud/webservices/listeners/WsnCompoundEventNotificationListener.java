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

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.WsnHelper;
import fr.inria.eventcloud.webservices.factories.WsClientFactory;

/**
 * A {@link CompoundEventNotificationListener} which invokes a WS-Notification
 * web service.
 * 
 * @author lpellegr
 */
public class WsnCompoundEventNotificationListener extends
        CompoundEventNotificationListener {

    private static final long serialVersionUID = 130L;

    private static final Logger log =
            LoggerFactory.getLogger(WsnCompoundEventNotificationListener.class);

    private final QName streamQName;

    private final String subscriberWsEndpointUrl;

    private transient NotificationConsumer subscriberWsClient;

    /**
     * Creates a {@link WsnCompoundEventNotificationListener} with the specified
     * subscriber web service endpoint URL to invoke the WS-Notification web
     * service to notify.
     * 
     * @param streamUrl
     *            the stream URL.
     * @param subscriberWsEndpointUrl
     *            the subscriber web service endpoint URL.
     */
    public WsnCompoundEventNotificationListener(String streamUrl,
            String subscriberWsEndpointUrl) {
        this.subscriberWsEndpointUrl = subscriberWsEndpointUrl;

        int index = streamUrl.lastIndexOf('/');

        this.streamQName =
                new QName(
                        streamUrl.substring(0, index),
                        streamUrl.substring(index + 1), "s");
    }

    private synchronized NotificationConsumer getSubscriberWsClient() {
        if (this.subscriberWsClient == null) {
            this.subscriberWsClient =
                    WsClientFactory.createWsClient(
                            NotificationConsumer.class,
                            this.subscriberWsEndpointUrl);
        }

        return this.subscriberWsClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNotification(SubscriptionId id, CompoundEvent solution) {
        try {
            this.getSubscriberWsClient().notify(
                    WsnHelper.createNotifyMessage(
                            this.subscriberWsEndpointUrl, this.streamQName,
                            solution));

            log.info(
                    "Subscriber {} notified about:\n {}",
                    this.subscriberWsEndpointUrl, solution);
        } catch (TranslationException e) {
            log.error("Error during translation", e);
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
