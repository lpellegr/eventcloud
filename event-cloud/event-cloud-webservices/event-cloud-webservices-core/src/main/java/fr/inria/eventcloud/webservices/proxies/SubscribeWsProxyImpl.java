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
package fr.inria.eventcloud.webservices.proxies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.JaxWsClientFactoryBean;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;
import fr.inria.eventcloud.api.properties.AlterableElaProperty;
import fr.inria.eventcloud.proxies.EventCloudCache;
import fr.inria.eventcloud.proxies.SubscribeProxyImpl;
import fr.inria.eventcloud.webservices.api.SubscribeInfos;
import fr.inria.eventcloud.webservices.api.SubscribeWsApi;
import fr.inria.eventcloud.webservices.api.SubscriberWsApi;

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

    private static final String NOTIFY_METHOD_NAME = "Notify";

    // contains the subscriber web service clients to use in order to deliver
    // the solutions
    private Map<SubscriptionId, Client> subscribers;

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
    public void init(EventCloudCache proxy, String componentUri,
                     AlterableElaProperty[] properties) {
        if (this.proxy == null) {
            super.init(proxy, componentUri, properties);
            this.subscribers = new HashMap<SubscriptionId, Client>();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionId subscribe(SubscribeInfos subscribeInfos) {

        // TODO: to translate the WS-Notification subscription to a SPARQL query
        // by using:
        // this.translator.translateSubscribeToSparqlQuery(subscription)
        Subscription subscription =
                new Subscription(subscribeInfos.getSparqlQuery());

        JaxWsClientFactoryBean clientFactory = new JaxWsClientFactoryBean();
        clientFactory.setServiceClass(SubscriberWsApi.class);
        clientFactory.setAddress(subscribeInfos.getSubscriberWsUrl());
        Client client = clientFactory.create();

        this.subscribers.put(subscription.getId(), client);

        super.subscribe(subscription, new CompoundEventNotificationListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void onNotification(SubscriptionId id, CompoundEvent event) {
                try {
                    List<CompoundEvent> events = new ArrayList<CompoundEvent>();
                    events.add(event);
                    subscribers.get(id).invoke(
                            NOTIFY_METHOD_NAME, new Object[] {events});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return subscription.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(SubscriptionId id) {
        super.unsubscribe(id);
        Client client = this.subscribers.remove(id);
        client.destroy();
    }
}
