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
package fr.inria.eventcloud.webservices.proxies;

import java.util.HashMap;
import java.util.Map;

import org.oasis_open.docs.wsn.b_2.GetCurrentMessage;
import org.oasis_open.docs.wsn.b_2.GetCurrentMessageResponse;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.MultipleTopicsSpecifiedFault;
import org.oasis_open.docs.wsn.bw_2.NoCurrentMessageOnTopicFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.bw_2.TopicNotSupportedFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.properties.AlterableElaProperty;
import fr.inria.eventcloud.proxies.EventCloudCache;
import fr.inria.eventcloud.proxies.SubscribeProxyImpl;
import fr.inria.eventcloud.webservices.api.SubscribeInfos;
import fr.inria.eventcloud.webservices.api.SubscribeWsApi;
import fr.inria.eventcloud.webservices.listeners.WsEventNotificationListener;

/**
 * SubscribeWsProxyImpl is an extension of {@link SubscribeProxyImpl} in order
 * to be able to expose the proxy as a web service.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class SubscribeWsProxyImpl extends SubscribeProxyImpl implements
        SubscribeWsApi {

    private static final long serialVersionUID = 1L;

    /**
     * ADL name of the subscribe web service proxy component.
     */
    public static final String SUBSCRIBE_WEBSERVICE_PROXY_ADL =
            "fr.inria.eventcloud.webservices.proxies.SubscribeWsProxy";

    /**
     * Functional interface name of the subscribe web service proxy component.
     */
    public static final String SUBSCRIBE_WEBSERVICES_ITF =
            "subscribe-webservices";

    // contains the subscriber web service endpoint URLs to use in order to
    // deliver
    // the solutions
    private Map<SubscriptionId, String> subscribers;

    /**
     * Empty constructor required by ProActive.
     */
    public SubscribeWsProxyImpl() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttributes(EventCloudCache proxy, String componentUri,
                              AlterableElaProperty[] properties) {
        if (this.eventCloudCache == null) {
            super.setAttributes(proxy, componentUri, properties);
            this.subscribers = new HashMap<SubscriptionId, String>();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GetCurrentMessageResponse getCurrentMessage(GetCurrentMessage currentMessage)
            throws NoCurrentMessageOnTopicFault, TopicNotSupportedFault,
            ResourceUnknownFault, MultipleTopicsSpecifiedFault,
            TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionId subscribe(SubscribeInfos subscribeInfos) {
        Subscription subscription =
                new Subscription(
                        subscribeInfos.getSparqlQuery(),
                        subscribeInfos.getSubscriberWsEndpointUrl());

        this.subscribers.put(
                subscription.getId(),
                subscribeInfos.getSubscriberWsEndpointUrl());

        this.subscribe(subscription, new WsEventNotificationListener(
                super.getEventCloudCache().getId().getStreamUrl(),
                subscribeInfos.getSubscriberWsEndpointUrl()));

        return subscription.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(SubscriptionId id) {
        super.unsubscribe(id);
        this.subscribers.remove(id);
    }

}
