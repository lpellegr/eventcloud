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

import java.util.HashMap;
import java.util.Map;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.oasis_open.docs.wsn.b_2.GetCurrentMessage;
import org.oasis_open.docs.wsn.b_2.GetCurrentMessageResponse;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse;

import com.petalslink.wsn.service.wsnproducer.NotificationProducer;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.SubscribeProxy;
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
public class SubscribeService extends EventCloudService<SubscribeProxy>
        implements NotificationProducer {

    private Map<SubscriptionId, String> subscribers;

    public SubscribeService(String registryUrl, String eventCloudIdUrl) {
        super(registryUrl, eventCloudIdUrl);
        this.subscribers = new HashMap<SubscriptionId, String>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GetCurrentMessageResponse getCurrentMessage(GetCurrentMessage currentMessage) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GetResourcePropertyResponse getResourceProperty(QName qname) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscribeResponse subscribe(Subscribe subscribe) {
        String sparqlQuery =
                super.translator.translateSubscribeToSparqlQuery(subscribe);

        log.info("Translated SPARQL query is {}", sparqlQuery);

        if (sparqlQuery != null) {
            W3CEndpointReference consumerReference =
                    subscribe.getConsumerReference();

            if (consumerReference != null) {
                Object address =
                        ReflectionUtils.getFieldValue(
                                consumerReference, "address");
                if (address != null) {
                    String subscriberUrl =
                            (String) ReflectionUtils.getFieldValue(
                                    address, "uri");
                    if (subscriberUrl != null) {
                        log.info("Subscriber URL is {}", subscriberUrl);

                        SubscriptionId id =
                                super.proxy.subscribe(
                                        sparqlQuery,
                                        new WsEventNotificationListener(
                                                subscriberUrl));

                        this.subscribers.put(id, subscriberUrl);
                    } else {
                        log.info("Subscribe notification received but no subscriber address is specified: the subscriber will receive no notification");
                    }

                    log.info("New subscribe notification handled");
                }

                log.error("Consumer reference cannot be extracted from subscribe notification");
            } else {
                log.error("Subscribe notification does not contain consumer reference");
            }

        } else {
            log.error("SPARQL query cannot be extracted from subscribe notification");
        }

        return new SubscribeResponse();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscribeProxy createProxy() {
        return ProxyFactory.getInstance(
                super.registryUrl, EventCloudId.fromUrl(super.eventCloudIdUrl))
                .createSubscribeProxy();
    }

}
