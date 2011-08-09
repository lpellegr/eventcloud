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

import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.component.body.ComponentInitActive;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.listeners.EventNotificationListener;
import fr.inria.eventcloud.api.listeners.NotificationListener;
import fr.inria.eventcloud.api.properties.AlterableElaProperty;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.messages.request.can.IndexSubscriptionRequest;
import fr.inria.eventcloud.pubsub.Notification;
import fr.inria.eventcloud.pubsub.NotificationId;
import fr.inria.eventcloud.pubsub.Solution;
import fr.inria.eventcloud.pubsub.Subscription;

/**
 * SubscribeProxyImpl is a concrete implementation of {@link SubscribeProxy}.
 * This class has to be instantiated as a ProActive/GCM component.
 * <p>
 * Currently the receive operation is handled sequentially because it is not set
 * as IS. This means we don't have to synchronize the datastructure that are
 * used inside this method. However it would be nice to evaluate/decide if it is
 * interesting to put the receive operation as IS (TODO).
 * 
 * @author lpellegr
 * @author bsauvan
 * 
 * @see ProxyFactory
 */
public class SubscribeProxyImpl extends ProxyCache implements ComponentInitActive,
        SubscribeProxy {

    private static final long serialVersionUID = 1L;

    private static final Logger log =
            LoggerFactory.getLogger(SubscribeProxy.class);

    // contains the subscriptions that have been registered from this proxy
    private Map<SubscriptionId, Subscription> subscriptions;

    // contains the listeners to use in order to deliver the solutions
    private Map<SubscriptionId, NotificationListener<?>> listeners;

    // contains the solutions that are being received
    private Map<NotificationId, Solution> solutions;

    /**
     * Empty constructor required by ProActive.
     */
    public SubscribeProxyImpl() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public void initComponentActivity(Body body) {
        body.setImmediateService("init", false);
    }

    /**
     * {@inheritDoc}
     */
    // TODO: add support for ELA properties. At least for the maximum number of
    // requests per seconds (by using a queue and a scheduled Timer).
    public void init(EventCloudCache proxy, AlterableElaProperty[] properties) {
        if (this.proxy == null) {
            this.proxy = proxy;
            this.subscriptions = new HashMap<SubscriptionId, Subscription>();
            this.listeners =
                    new HashMap<SubscriptionId, NotificationListener<?>>();
            this.solutions = new HashMap<NotificationId, Solution>();
            // TODO: use the properties field to initialize ELA properties
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionId subscribe(String sparqlQuery,
                                    BindingNotificationListener listener) {
        return this.indexSubscription(sparqlQuery, listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionId subscribe(String sparqlQuery,
                                    EventNotificationListener listener) {
        return this.indexSubscription(sparqlQuery, listener);
    }

    private SubscriptionId indexSubscription(String sparqlQuery,
                                             NotificationListener<?> listener) {
        Subscription subscription =
                new Subscription(
                        PAActiveObject.getUrl(PAActiveObject.getStubOnThis()),
                        sparqlQuery);

        log.debug(
                "New subscription has been registered from {} with id {}",
                PAActiveObject.getBodyOnThis().getUrl(), subscription.getId());

        NotificationListener<?> result =
                this.listeners.put(subscription.getId(), listener);

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
        NotificationListener<?> listener =
                this.listeners.get(id.getSubscriptionId());

        if (listener instanceof BindingNotificationListener) {
            this.deliver(id, (BindingNotificationListener) listener);
        } else if (listener instanceof EventNotificationListener) {
            this.deliver(id, (EventNotificationListener) listener);
        } else {
            log.error("Unknown notification listener: " + listener.getClass());
        }

        log.debug("Notification {} has been delivered", id);
    }

    private void deliver(NotificationId id, BindingNotificationListener listener) {
        listener.onNotification(id.getSubscriptionId(), this.solutions.get(id)
                .getSolution());
    }

    private void deliver(NotificationId id, EventNotificationListener listener) {
        // TODO retrieve all the quadruples associated to each context value and
        // create events according to the context value, then deliver the events
        // by using the listener
        System.out.println("PublishSubscribeProxyImpl.enclosing_method()");
    }

    public void receive(Notification notification) {
        log.debug(
                "New notification received on {} from {} for subscription id {}",
                new Object[] {
                        PAActiveObject.getBodyOnThis().getUrl(),
                        notification.getSource(),
                        notification.getId().getSubscriptionId()});

        System.out.println("PublishSubscribeProxyImpl.receive() notification contains binding which is "
                + notification.getBinding()
                + " and notification id is "
                + notification.getId());

        Solution solution = this.solutions.get(notification.getId());
        if (solution == null) {
            solution =
                    new Solution(
                            this.subscriptions.get(
                                    notification.getId().getSubscriptionId())
                                    .getSubSubscriptions().length,
                            notification.getBinding());
            this.solutions.put(notification.getId(), solution);
        } else {
            solution.addSubSolution(notification.getBinding());
        }

        // checks whether all the sub-solutions have been received or not
        if (solution.isReady()) {
            // TODO checks whether the ELA properties are verified
            // if yes, deliver the solution
            // else do nothing and wait for an ELA property that is verified
            this.deliver(notification.getId());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Subscription find(SubscriptionId id) {
        return this.subscriptions.get(id);
    }

}
