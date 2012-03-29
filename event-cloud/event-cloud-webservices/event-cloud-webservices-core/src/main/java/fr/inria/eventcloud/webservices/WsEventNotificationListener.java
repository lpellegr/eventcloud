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
package fr.inria.eventcloud.webservices;

import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.JaxWsClientFactoryBean;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;
import fr.inria.eventcloud.translators.wsn.WsNotificationMessageBuilder;
import fr.inria.eventcloud.translators.wsn.notify.SemanticCompoundEventTranslator;

/**
 * An {@link CompoundEventNotificationListener}
 * 
 * @author lpellegr
 */
public class WsEventNotificationListener extends
        CompoundEventNotificationListener {

    private static final long serialVersionUID = 1L;

    private static final Logger log =
            LoggerFactory.getLogger(WsEventNotificationListener.class);

    private static final String NOTIFY_METHOD_NAME = "Notify";

    private static SemanticCompoundEventTranslator translator =
            new SemanticCompoundEventTranslator();

    private final QName streamQName;

    private final String subscriberWsUrl;

    private transient Client wsClient;

    /**
     * Creates an {@link CompoundEventNotificationListener} with the specified
     * subscriber URL to invoke the Web service to notify.
     * 
     * @param streamUrl
     *            the stream URL.
     * 
     * @param subscriberWsUrl
     *            the subscriber URL.
     */
    public WsEventNotificationListener(String streamUrl, String subscriberWsUrl) {
        this.subscriberWsUrl = subscriberWsUrl;

        int index = streamUrl.lastIndexOf('/');

        this.streamQName =
                new QName(
                        streamUrl.substring(0, index),
                        streamUrl.substring(index + 1), "s");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNotification(SubscriptionId id, CompoundEvent solution) {
        if (this.wsClient == null) {
            JaxWsClientFactoryBean clientFactory = new JaxWsClientFactoryBean();
            clientFactory.setServiceClass(NotificationConsumer.class);
            clientFactory.setAddress(this.subscriberWsUrl);
            this.wsClient = clientFactory.create();
        }

        Notify notify =
                WsNotificationMessageBuilder.createNotifyMessage(
                        translator, this.streamQName.getNamespaceURI(),
                        this.streamQName.getPrefix(),
                        this.streamQName.getLocalPart(), solution);

        try {
            this.wsClient.invoke(NOTIFY_METHOD_NAME, new Object[] {notify});
            log.info(
                    "Web service {} invoked to notify for:\n {}",
                    this.subscriberWsUrl, solution);
        } catch (Exception e) {
            log.error("Error during the invocation of the Notify web method", e);
        }
    }

}
