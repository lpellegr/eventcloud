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
package fr.inria.eventcloud.webservices.proxies;

import org.objectweb.proactive.annotation.multiactivity.MemberOf;

import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.proxies.SubscribeProxyImpl;
import fr.inria.eventcloud.webservices.api.SubscribeWsApi;
import fr.inria.eventcloud.webservices.listeners.WsBindingNotificationListener;
import fr.inria.eventcloud.webservices.listeners.WsCompoundEventNotificationListener;
import fr.inria.eventcloud.webservices.listeners.WsSignalNotificationListener;

/**
 * Extension of {@link SubscribeProxyImpl} in order to be able to expose a
 * subscribe proxy as a web service.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class SubscribeWsProxyImpl extends SubscribeProxyImpl implements
        SubscribeWsApi {

    private static final long serialVersionUID = 160L;

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
    @MemberOf("parallelSelfCompatible")
    public String subscribeSignal(String sparqlQuery,
                                  String subscriberWsEndpointUrl) {
        Subscription subscription = new Subscription(sparqlQuery);

        super.subscribe(subscription, new WsSignalNotificationListener(
                subscriberWsEndpointUrl));

        return subscription.getId().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelSelfCompatible")
    public String subscribeBinding(String sparqlQuery,
                                   String subscriberWsEndpointUrl) {
        Subscription subscription = new Subscription(sparqlQuery);

        super.subscribe(subscription, new WsBindingNotificationListener(
                subscriberWsEndpointUrl));

        return subscription.getId().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelSelfCompatible")
    public String subscribeCompoundEvent(String sparqlQuery,
                                         String subscriberWsEndpointUrl) {

        Subscription subscription = new Subscription(sparqlQuery);

        super.subscribe(subscription, new WsCompoundEventNotificationListener(
                subscriberWsEndpointUrl));

        return subscription.getId().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelSelfCompatible")
    public void unsubscribe(String id) {
        this.unsubscribe(SubscriptionId.parseSubscriptionId(id));
    }

}
