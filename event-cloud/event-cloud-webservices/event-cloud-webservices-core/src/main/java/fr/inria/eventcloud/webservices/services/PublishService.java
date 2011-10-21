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

import javax.jws.WebService;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;

import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.PublishProxy;

/**
 * Defines a publish web service as defined by the WS-Notification
 * specification. All the calls to the notify request will be translated and
 * redirected to a {@link PublishProxy} in order to be published into an Event
 * Cloud.
 * 
 * @author lpellegr
 */
@WebService(serviceName = "EventCloudPublish", portName = "EventCloudPublishPort", targetNamespace = "http://docs.oasis-open.org/wsn/bw-2", name = "EventCloudPublishPortType")
public class PublishService extends EventCloudService<PublishProxy> implements
        NotificationConsumer {

    public PublishService(String registryUrl, String eventCloudIdUrl) {
        super(registryUrl, eventCloudIdUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(Notify notify) {
        for (NotificationMessageHolderType notificationMessage : notify.getNotificationMessage()) {
            Event event =
                    this.translator.translateNotificationMessageToEvent(notificationMessage);
            log.info(
                    "Translated event to insert to the Event Cloud is {}",
                    event);
            super.proxy.publish(event);
        }

        log.info("New notify notification handled");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PublishProxy createProxy() {
        return ProxyFactory.getInstance(
                super.registryUrl, EventCloudId.fromUrl(super.eventCloudIdUrl))
                .createPublishProxy();
    }

}
