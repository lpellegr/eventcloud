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
package fr.inria.eventcloud.webservices.wsn;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.objectweb.proactive.extensions.p2p.structured.deployment.DeploymentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.deployment.EventCloudComponentsManager;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
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
public class PublishWsnServiceImpl extends WsnService<PublishProxy> implements
        PublishWsnApi {

    private static final Logger LOG =
            LoggerFactory.getLogger(PublishWsnServiceImpl.class);

    /**
     * Creates a {@link PublishWsnServiceImpl}.
     * 
     * @param componentPoolManager
     *            the component pool manager to be used for the deployment of
     *            the underlying publish proxy.
     * @param deploymentConfiguration
     *            the deployment configuration to use during the deployment of
     *            the underlying publish proxy.
     * @param registryUrl
     *            the URL of the EventClouds registry to connect to in order to
     *            create the underlying publish proxy.
     * @param streamUrl
     *            the URL which identifies the EventCloud on which the
     *            underlying publish proxy must be connected.
     */
    public PublishWsnServiceImpl(
            EventCloudComponentsManager componentPoolManager,
            DeploymentConfiguration deploymentConfiguration,
            String registryUrl, String streamUrl) {
        super(componentPoolManager, deploymentConfiguration, registryUrl,
                streamUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized PublishProxy getProxy() throws EventCloudIdNotManaged {
        if (super.proxy == null) {
            super.proxy =
                    super.componentPoolManager.getPublishProxy(
                            super.deploymentConfiguration, super.registryUrl,
                            new EventCloudId(super.streamUrl));
        }

        return super.proxy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void releaseProxy(EventCloudsRegistry registry, EventCloudId id) {
        registry.unregisterProxy(id, this.proxy);
        // super.componentPoolManager.releasePublishProxies(ImmutableList.of(this.proxy));
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

                    LOG.info("Translation output:\n{}", compoundEvent);

                    super.proxy.publish(compoundEvent);
                } catch (TranslationException e) {
                    LOG.error("Translation error", e);
                }
            }
        } else {
            LOG.error("Notify message received does not contain any notification message");
        }
    }

}
