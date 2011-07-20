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
package fr.inria.eventcloud.proxies;

import java.io.InputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.PublishSubscribeApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingsNotificationListener;
import fr.inria.eventcloud.api.listeners.EventsNotificationListener;
import fr.inria.eventcloud.api.listeners.NotificationListener;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.messages.request.can.IndexSubscriptionRequest;
import fr.inria.eventcloud.messages.request.can.PublishQuadrupleRequest;
import fr.inria.eventcloud.pubsub.Notification;
import fr.inria.eventcloud.pubsub.NotificationId;
import fr.inria.eventcloud.pubsub.Solution;
import fr.inria.eventcloud.pubsub.Subscription;

/**
 * A PublishSubscribeProxy is a proxy that implements the
 * {@link PublishSubscribeApi}. It has to be used by a user who wants to execute
 * publish/subscribe asynchronous operations on an Event-Cloud. This class has
 * to be instantiated as a ProActive active object.
 * 
 * @author lpellegr
 * 
 * @see ProxyFactory
 */
public class PublishSubscribeProxy extends Proxy implements
        PublishSubscribeApi, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger log =
            LoggerFactory.getLogger(PublishSubscribeProxy.class);

    // Contains the subscriptions that have been registered from this proxy
    private ConcurrentMap<SubscriptionId, Subscription> subscriptions;

    // Contains the listeners to use in order to deliver the solutions
    private ConcurrentMap<SubscriptionId, NotificationListener<?>> listeners;

    // Contains the solutions that are being received
    private ConcurrentMap<NotificationId, Solution> solutions;

    /**
     * Empty constructor required by ProActive.
     */
    public PublishSubscribeProxy() {
        super();
    }

    // TODO: add support for ELA properties. At least for the maximum number of
    // requests per seconds (by using a queue and a scheduled Timer).
    public PublishSubscribeProxy(EventCloudProxy proxy) {
        super(proxy);

        this.subscriptions =
                new ConcurrentHashMap<SubscriptionId, Subscription>();
        this.listeners =
                new ConcurrentHashMap<SubscriptionId, NotificationListener<?>>();
        this.solutions = new ConcurrentHashMap<NotificationId, Solution>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Quadruple quad) {
        // TODO: use an asynchronous call with no response (see issue 16)
        try {
            super.proxy.selectTracker().getRandomPeer().send(
                    new PublishQuadrupleRequest(quad));
        } catch (DispatchException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Event event) {
        // TODO try to improve the publication of several quadruples
        // first insight: use a thread-pool
        for (Quadruple quad : event.getQuadruples()) {
            this.publish(quad);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Collection<Event> events) {
        // TODO use a thread-pool
        for (Event event : events) {
            this.publish(event);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(InputStream in, SerializationFormat format) {
        read(in, format, new QuadrupleAction() {
            @Override
            public void performAction(Quadruple quad) {
                publish(quad);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionId subscribe(String sparqlQuery,
                                    BindingsNotificationListener listener) {
        return this.indexSubscription(sparqlQuery, listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionId subscribe(String sparqlQuery,
                                    EventsNotificationListener listener) {
        return this.indexSubscription(sparqlQuery, listener);
    }

    private SubscriptionId indexSubscription(String sparqlQuery,
                                             NotificationListener<?> listener) {
        Subscription subscription =
                new Subscription(
                        PAActiveObject.getUrl(PAActiveObject.getStubOnThis()),
                        sparqlQuery);

        NotificationListener<?> result =
                this.listeners.putIfAbsent(subscription.getId(), listener);

        if (result != null) {
            log.warn("The subscription is canceled because a listener associated to the same subscription id has been detected: "
                    + subscription.getId());
            return subscription.getId();
        }

        try {
            super.proxy.selectTracker().getRandomPeer().send(
                    new IndexSubscriptionRequest(subscription));
            this.subscriptions.put(subscription.getId(), subscription);
        } catch (DispatchException e) {
            e.printStackTrace();
        }

        return subscription.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(SubscriptionId id) {
        // TODO Auto-generated method stub

    }

    private void deliver(NotificationId id) {
        // TODO delivers the solution according the type of the listener
        NotificationListener<?> listener = this.listeners.get(id);

        if (listener instanceof BindingsNotificationListener) {
            this.deliver(id, (BindingsNotificationListener) listener);
        } else if (listener instanceof EventsNotificationListener) {
            this.deliver(id, (EventsNotificationListener) listener);
        } else {
            log.error("Unknown notification listener: " + listener.getClass());
        }

        log.debug("Notification {} has been delivered", id);
    }

    private void deliver(NotificationId id,
                         BindingsNotificationListener listener) {
        listener.handleNotification(id.getSubscriptionId(), this.solutions.get(
                id).getBindings());
    }

    private void deliver(NotificationId id, EventsNotificationListener listener) {
        // TODO retrieve all the quadruples associated to each context value and
        // create events according to the context value, then deliver the events
        // by using the listener
    }

    public void receive(Notification notification) {
        Solution solution =
                new Solution(
                        this.subscriptions.get(notification.getId())
                                .getSubSubscriptions().length,
                        notification.getBinding());
        Solution existingSolution =
                this.solutions.putIfAbsent(notification.getId(), solution);

        if (existingSolution != null) {
            existingSolution.addSubSolution(notification.getBinding());
            solution = existingSolution;
        }

        // checks whether all the sub-solutions have been received or not
        if (solution.isReady()) {
            // TODO checks whether the ELA properties are verified
            // if yes, deliver the solution
            // else do nothing and wait for an ELA property that is verified
            this.deliver(notification.getId());
        }
    }

    public Subscription find(SubscriptionId id) {
        return this.subscriptions.get(id);
    }

}
