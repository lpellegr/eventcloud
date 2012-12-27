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
package fr.inria.eventcloud.webservices.wsn;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.WsnLogUtils;
import fr.inria.eventcloud.webservices.api.PublishWsnApi;

/**
 * Concrete implementation of {@link PublishWsnApi}. All the calls to the notify
 * method will be translated and redirected to a {@link PublishProxy} in order
 * to be published into an EventCloud.
 * 
 * @author lpellegr
 */
public class PublishWsnServiceImpl extends WsnService<PublishApi> implements
        PublishWsnApi {

    private static final Logger log =
            LoggerFactory.getLogger(PublishWsnServiceImpl.class);

    /**
     * Creates a {@link PublishWsnServiceImpl}.
     * 
     * @param registryUrl
     *            the URL of the EventClouds registry to connect to in order to
     *            create the underlying publish proxy.
     * @param streamUrl
     *            the URL which identifies the EventCloud on which the
     *            underlying publish proxy must be connected.
     */
    public PublishWsnServiceImpl(String registryUrl, String streamUrl) {
        super(registryUrl, streamUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PublishApi createProxy() throws EventCloudIdNotManaged {
        return ProxyFactory.newPublishProxy(
                super.registryUrl, new EventCloudId(super.streamUrl));
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
                    log.error("Translation error", e);
                }
            }
        } else {
            log.error("Notify message received does not contain any notification message");
        }
    }

}
