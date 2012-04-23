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

import java.util.HashMap;
import java.util.Map;

import javax.jws.WebService;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.oasis_open.docs.wsn.b_2.GetCurrentMessage;
import org.oasis_open.docs.wsn.b_2.GetCurrentMessageResponse;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.NotificationProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.SubscribeProxy;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.WsNotificationLogUtils;
import fr.inria.eventcloud.utils.ReflectionUtils;
import fr.inria.eventcloud.webservices.WsEventNotificationListener;

/**
 * Defines a subscribe web service as defined by the WS-Notification
 * specification. All the calls to the subscribe request will be translated and
 * redirected to a {@link SubscribeProxy} in order to be treated into an Event
 * Cloud.
 * 
 * @author lpellegr
 */
@WebService(serviceName = "EventCloudSubscribe", portName = "EventCloudSubscribePort", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", name = "EventCloudSubscribePortType")
public class SubscribeServiceImpl extends
        EventCloudTranslatableProxyService<SubscribeProxy> implements
        NotificationProducer {

    private final Map<SubscriptionId, String> subscribers;

    private static final Logger log =
            LoggerFactory.getLogger(SubscribeServiceImpl.class);

    public SubscribeServiceImpl(String registryUrl, String eventCloudIdUrl) {
        super(registryUrl, eventCloudIdUrl);
        this.subscribers = new HashMap<SubscriptionId, String>();
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

        WsNotificationLogUtils.logSubscribe(subscribe);

        W3CEndpointReference consumerReference =
                subscribe.getConsumerReference();

        if (consumerReference != null) {
            Object address =
                    ReflectionUtils.getFieldValue(consumerReference, "address");

            if (address != null) {
                String subscriberUrl =
                        (String) ReflectionUtils.getFieldValue(address, "uri");

                if (subscriberUrl != null) {
                    try {
                        String sparqlQuery =
                                super.translator.translate(subscribe);

                        log.info("Subscriber endpoint is {}", subscriberUrl);
                        log.info("Translation output:\n{}", sparqlQuery);

                        Subscription subscription =
                                new Subscription(sparqlQuery);

                        this.subscribers.put(
                                subscription.getId(), subscriberUrl);

                        super.proxy.subscribe(
                                subscription, new WsEventNotificationListener(
                                        super.streamUrl, subscriberUrl));
                    } catch (TranslationException e) {
                        log.error("Translation error:");
                        logAndThrowIllegalArgumentException(e.getMessage());
                    }
                } else {
                    logAndThrowIllegalArgumentException("Subscribe message received but no subscriber address is specified: the subscriber cannot receive any notification");
                }
            } else {
                logAndThrowIllegalArgumentException("Consumer address cannot be extracted from subscribe message");
            }
        } else {
            logAndThrowIllegalArgumentException("Subscribe message does not contain consumer reference");
        }

        return new SubscribeResponse();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscribeProxy createProxy() throws EventCloudIdNotManaged {
        return ProxyFactory.getInstance(
                super.registryUrl, new EventCloudId(super.streamUrl))
                .createSubscribeProxy();
    }

}
