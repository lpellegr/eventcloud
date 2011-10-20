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

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.JaxWsClientFactoryBean;
import org.oasis_open.docs.wsn.b_2.GetCurrentMessage;
import org.oasis_open.docs.wsn.b_2.GetCurrentMessageResponse;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.InvalidFilterFault;
import org.oasis_open.docs.wsn.bw_2.InvalidMessageContentExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidProducerPropertiesExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.MultipleTopicsSpecifiedFault;
import org.oasis_open.docs.wsn.bw_2.NoCurrentMessageOnTopicFault;
import org.oasis_open.docs.wsn.bw_2.NotifyMessageNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.SubscribeCreationFailedFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.bw_2.TopicNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableInitialTerminationTimeFault;
import org.oasis_open.docs.wsn.bw_2.UnrecognizedPolicyRequestFault;
import org.oasis_open.docs.wsn.bw_2.UnsupportedPolicyRequestFault;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse;
import org.oasis_open.docs.wsrf.rpw_2.InvalidResourcePropertyQNameFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnavailableFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

import com.petalslink.wsn.service.wsnproducer.NotificationProducer;

import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.EventNotificationListener;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;
import fr.inria.eventcloud.utils.ReflectionUtils;
import fr.inria.eventcloud.webservices.api.SubscriberWsApi;

/**
 * Defines a subscribe web service as defined by the WS-Notification
 * specification. All the calls to the Notify request will be translated and
 * redirected to a {@link PublishProxy} in order to be published into an Event
 * Cloud.
 * 
 * @author lpellegr
 */
@WebService(serviceName = "EventCloudSubscribe", portName = "EventCloudSubscribePort", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", name = "EventCloudSubscribePortType")
public class SubscribeService extends EventCloudService<SubscribeProxy>
        implements NotificationProducer {

    private static final String NOTIFY_METHOD_NAME = "Notify";

    private Map<SubscriptionId, Client> subscribers;

    public SubscribeService(String registryUrl, String eventCloudIdUrl) {
        super(registryUrl, eventCloudIdUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GetCurrentMessageResponse getCurrentMessage(GetCurrentMessage currentMessage)
            throws TopicNotSupportedFault, InvalidTopicExpressionFault,
            ResourceUnknownFault, TopicExpressionDialectUnknownFault,
            NoCurrentMessageOnTopicFault, MultipleTopicsSpecifiedFault {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GetResourcePropertyResponse getResourceProperty(QName qname)
            throws ResourceUnknownFault, InvalidResourcePropertyQNameFault,
            ResourceUnavailableFault {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscribeResponse subscribe(Subscribe subscribeRequest)
            throws NotifyMessageNotSupportedFault, TopicNotSupportedFault,
            InvalidFilterFault, InvalidTopicExpressionFault,
            ResourceUnknownFault, TopicExpressionDialectUnknownFault,
            UnrecognizedPolicyRequestFault, SubscribeCreationFailedFault,
            InvalidMessageContentExpressionFault,
            UnsupportedPolicyRequestFault,
            UnacceptableInitialTerminationTimeFault,
            InvalidProducerPropertiesExpressionFault {

        if (this.subscribers == null) {
            this.subscribers = new HashMap<SubscriptionId, Client>();
        }

        // TODO: maintain mapping between subscription Id, subscriber
        SubscriptionId subscriptionId =
                super.proxy.subscribe(
                        super.translator.translateSubscribeToSparqlQuery(subscribeRequest),
                        new EventNotificationListener() {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void onNotification(SubscriptionId id,
                                                       Event solution) {

                                NotificationMessageHolderType msg =
                                        translator.translateEventToNotificationMessage(solution);
                                Notify notifyMessage = new Notify();
                                notifyMessage.getNotificationMessage().add(msg);

                                try {
                                    subscribers.get(id).invoke(
                                            NOTIFY_METHOD_NAME,
                                            new Object[] {notifyMessage});
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

        if (!this.subscribers.containsKey(subscriptionId)) {
            W3CEndpointReference consumerReference =
                    subscribeRequest.getConsumerReference();
            Object address =
                    ReflectionUtils.getFieldValue(consumerReference, "address");

            String subscriberAddress = null;
            if (address != null) {
                Object uri = ReflectionUtils.getFieldValue(address, "uri");

                if (uri != null) {
                    subscriberAddress = (String) uri;
                }
            }

            if (subscriberAddress != null) {
                JaxWsClientFactoryBean clientFactory =
                        new JaxWsClientFactoryBean();
                clientFactory.setServiceClass(SubscriberWsApi.class);
                clientFactory.setAddress(subscriberAddress);
                Client client = clientFactory.create();
                this.subscribers.put(subscriptionId, client);
            } else {
                log.info("Subscribe request received but no subscriber address is specified: the subscriber will receive no notification");
            }
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
