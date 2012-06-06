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
package fr.inria.eventcloud.webservices.services;

import javax.jws.WebService;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.WsnLogUtils;

/**
 * Defines a publish web service as defined by the WS-Notification
 * specification. All the calls to the notify request will be translated and
 * redirected to a {@link PublishProxy} in order to be published into an
 * eventcloud.
 * 
 * @author lpellegr
 */
@WebService(serviceName = "EventCloudPublish", portName = "EventCloudPublishPort", targetNamespace = "http://docs.oasis-open.org/wsn/bw-2", name = "EventCloudPublishPortType")
public class PublishServiceImpl extends
        EventCloudTranslatableProxyService<PublishProxy> implements
        NotificationConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(PublishServiceImpl.class);

    public PublishServiceImpl(String registryUrl, String eventCloudIdUrl) {
        super(registryUrl, eventCloudIdUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(Notify notify) {
        if (super.proxy == null) {
            return;
        }

        if (notify.getNotificationMessage().size() > 0) {
            for (NotificationMessageHolderType notificationMessage : notify.getNotificationMessage()) {
                try {
                    WsnLogUtils.logNotificationMessageHolderType(notificationMessage);

                    CompoundEvent compoundEvent =
                            super.translator.translate(notificationMessage);

                    log.info("Translation output:\n{}", compoundEvent);

                    super.proxy.publish(compoundEvent);
                } catch (TranslationException e) {
                    log.error("Translation error:");
                    logAndThrowIllegalArgumentException(e.getMessage());
                }
            }

            log.info("New notification message handled");
        } else {
            logAndThrowIllegalArgumentException("Notify message received does not contain any notification message");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PublishProxy createProxy() throws EventCloudIdNotManaged {
        return ProxyFactory.newPublishProxy(
                super.registryUrl, new EventCloudId(super.streamUrl));
    }

}
