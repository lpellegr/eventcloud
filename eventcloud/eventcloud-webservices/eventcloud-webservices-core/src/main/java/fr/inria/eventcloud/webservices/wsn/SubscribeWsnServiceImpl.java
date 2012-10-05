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
package fr.inria.eventcloud.webservices.wsn;

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.oasis_open.docs.wsn.b_2.GetCurrentMessage;
import org.oasis_open.docs.wsn.b_2.GetCurrentMessageResponse;
import org.oasis_open.docs.wsn.b_2.Renew;
import org.oasis_open.docs.wsn.b_2.RenewResponse;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.SubscribeProxy;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.WsnHelper;
import fr.inria.eventcloud.translators.wsn.WsnLogUtils;
import fr.inria.eventcloud.webservices.api.SubscribeWsnApi;
import fr.inria.eventcloud.webservices.listeners.WsnCompoundEventNotificationListener;

/**
 * Concrete implementation of {@link SubscribeWsnApi}. All the calls to the
 * subscribe and unsubscribe methods will be translated and redirected to a
 * {@link SubscribeProxy} in order to be treated into an EventCloud.
 * 
 * @author lpellegr
 */
public class SubscribeWsnServiceImpl extends WsnService<SubscribeApi> implements
        SubscribeWsnApi {

    private final Map<SubscriptionId, String> subscribers;

    private static final Logger log =
            LoggerFactory.getLogger(SubscribeWsnServiceImpl.class);

    /**
     * Creates a {@link SubscribeWsnServiceImpl}.
     * 
     * @param registryUrl
     *            the URL of the EventClouds registry to connect to in order to
     *            create the underlying subscribe proxy.
     * @param streamUrl
     *            the URL which identifies the EventCloud on which the
     *            underlying publish proxy must be connected.
     */
    public SubscribeWsnServiceImpl(String registryUrl, String streamUrl) {
        super(registryUrl, streamUrl);
        this.subscribers = new HashMap<SubscriptionId, String>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscribeApi createProxy() throws EventCloudIdNotManaged {
        return ProxyFactory.newSubscribeProxy(
                super.registryUrl, new EventCloudId(super.streamUrl));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GetCurrentMessageResponse getCurrentMessage(GetCurrentMessage currentMessage) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscribeResponse subscribe(Subscribe subscribe) {
        if (super.proxy == null) {
            return null;
        }

        WsnLogUtils.logSubscribe(subscribe);

        W3CEndpointReference consumerReference =
                subscribe.getConsumerReference();

        if (consumerReference != null) {
            String subscriberWsEndpointUrl =
                    WsnHelper.getAddress(consumerReference);

            if (subscriberWsEndpointUrl != null) {
                try {
                    String sparqlQuery = super.translator.translate(subscribe);

                    log.info(
                            "Subscriber endpoint is {}",
                            subscriberWsEndpointUrl);
                    log.info("Translation output:\n{}", sparqlQuery);

                    Subscription subscription =
                            new Subscription(
                                    sparqlQuery, subscriberWsEndpointUrl);

                    this.subscribers.put(
                            subscription.getId(), subscriberWsEndpointUrl);

                    super.proxy.subscribe(
                            subscription,
                            new WsnCompoundEventNotificationListener(
                                    super.streamUrl, subscriberWsEndpointUrl));

                    return WsnHelper.createSubscribeResponse(
                            subscription.getId(), subscriberWsEndpointUrl);
                } catch (TranslationException e) {
                    log.error("Translation error:");
                    logAndThrowIllegalArgumentException(e.getMessage());
                }
            } else {
                logAndThrowIllegalArgumentException("Subscribe message received but no subscriber address is specified: the subscriber cannot receive any notification");
            }
        } else {
            logAndThrowIllegalArgumentException("Subscribe message does not contain consumer reference");
        }

        return WsnHelper.createSubscribeResponse("http://eventcloud.inria.fr/notification:NotificationService@Endpoint");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RenewResponse renew(Renew renewRequest) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnsubscribeResponse unsubscribe(Unsubscribe unsubscribeRequest) {
        super.proxy.unsubscribe(WsnHelper.getSubcriptionId(unsubscribeRequest));
        return new UnsubscribeResponse();
    }

}
