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
package fr.inria.eventcloud.proxies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;
import fr.inria.eventcloud.api.listeners.NotificationListener;
import fr.inria.eventcloud.api.listeners.NotificationListenerType;
import fr.inria.eventcloud.api.listeners.SignalNotificationListener;
import fr.inria.eventcloud.api.properties.AlterableElaProperty;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.messages.request.can.IndexSubscriptionRequest;
import fr.inria.eventcloud.messages.request.can.ReconstructCompoundEventRequest;
import fr.inria.eventcloud.messages.request.can.UnsubscribeRequest;
import fr.inria.eventcloud.messages.response.can.QuadruplePatternResponse;
import fr.inria.eventcloud.pubsub.Notification;
import fr.inria.eventcloud.pubsub.NotificationId;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.Solution;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.pubsub.Subsubscription;
import fr.inria.eventcloud.utils.LongLong;

/**
 * SubscribeProxyImpl is a concrete implementation of {@link SubscribeProxy}.
 * This class has to be instantiated as a ProActive/GCM component.
 * 
 * @author lpellegr
 * @author bsauvan
 * 
 * @see ProxyFactory
 */
public class SubscribeProxyImpl extends ProxyCache implements SubscribeProxy,
        SubscribeProxyAttributeController {

    private static final long serialVersionUID = 1L;

    private static final Logger log =
            LoggerFactory.getLogger(SubscribeProxy.class);

    // contains the subscriptions that have been registered from this proxy
    private Map<SubscriptionId, Subscription> subscriptions;

    // contains the listeners to use in order to deliver the solutions
    private Map<SubscriptionId, NotificationListener<?>> listeners;

    // contains the solutions that are being received
    private ConcurrentMap<NotificationId, Solution> solutions;

    // TODO: this set has to be replaced by a DataBag. The number of events ids
    // received will grow quickly and after some time it is possible to get an
    // OutOfMemory exception. That's why it would be nice to have the
    // possibility to define a threshold that defines what is the maximum number
    // of eventIds to store in memory. Then by using a DataBag and when the
    // threshold is reached, the data write are spilled to the disk
    // example from pig:
    // http://pig.apache.org/docs/r0.7.0/api/org/apache/pig/data/DataBag.html
    private ConcurrentMap<Node, SubscriptionId> eventIdsReceived;

    private String componentUri;

    /**
     * Empty constructor required by ProActive.
     */
    public SubscribeProxyImpl() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponentActivity(Body body) {
        super.initComponentActivity(body);

        // FIXME: to avoid some deadlock with components the method
        // setImmediateServices has to be handled in immediate services. This
        // configuration should be done in the ProActive source code.
        body.setImmediateService("setImmediateServices", false);
        body.setImmediateService("receive", false);
    }

    /**
     * {@inheritDoc}
     */
    // TODO: add support for ELA properties. At least for the maximum number of
    // requests per seconds (by using a queue and a scheduled Timer?).
    @Override
    public void setAttributes(EventCloudCache proxy, String componentUri,
                              AlterableElaProperty[] properties) {
        if (super.eventCloudCache == null) {
            super.eventCloudCache = proxy;
            super.proxy = Proxies.newProxy(super.eventCloudCache.getTrackers());

            this.componentUri = componentUri;
            this.subscriptions = new HashMap<SubscriptionId, Subscription>();
            this.listeners =
                    new HashMap<SubscriptionId, NotificationListener<?>>();
            this.solutions = new ConcurrentHashMap<NotificationId, Solution>();
            this.eventIdsReceived =
                    new ConcurrentHashMap<Node, SubscriptionId>();
            // TODO: use the properties field to initialize ELA properties
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> void subscribe(fr.inria.eventcloud.api.Subscription subscription,
                              NotificationListener<T> listener) {
        String sparqlQuery = subscription.getSparqlQuery();

        if (listener instanceof CompoundEventNotificationListener) {
            // rewrites the sparql query to keep only the graph variable in
            // the result variables. Indeed we need only the graph variable
            // (which identifies the event which is matched) to
            // reconstruct the compound event
            sparqlQuery =
                    PublishSubscribeUtils.removeResultVarsExceptGraphVar(sparqlQuery);
        }

        Subscription internalSubscription =
                new Subscription(
                        subscription.getId(), null, subscription.getId(),
                        subscription.getCreationTime(), sparqlQuery,
                        this.componentUri, listener.getType());

        NotificationListener<?> result =
                this.listeners.put(subscription.getId(), listener);

        if (result != null) {
            // same subscription with same creation time
            throw new IllegalArgumentException(
                    "Listener already exists for subscription id: "
                            + subscription.getId());
        }

        if (this.subscriptions.put(
                internalSubscription.getId(), internalSubscription) != null) {
            // same subscription with same creation time
            throw new IllegalArgumentException(
                    "Subscription already exists for subscription id: "
                            + subscription.getId());
        }

        try {
            super.sendv(new IndexSubscriptionRequest(internalSubscription));
        } catch (DispatchException e) {
            e.printStackTrace();
        }

        log.info(
                "New subscription has been registered from {} with id {}",
                PAActiveObject.getBodyOnThis().getUrl(), subscription.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final CompoundEvent reconstructCompoundEvent(Subscription subscription,
                                                        Binding binding) {

        if (!subscription.getGraphNode().isVariable()) {
            throw new IllegalArgumentException(
                    "The subscription graph node is not a variable");
        }

        Node eventId;
        if ((eventId = binding.get(Var.alloc("g"))) == null) {
            throw new IllegalArgumentException(
                    "The specified binding does not contain a graph value");
        }

        return this.reconstructCompoundEvent(subscription.getId(), eventId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final CompoundEvent reconstructCompoundEvent(SubscriptionId id,
                                                        Node eventId) {
        if (!eventId.isURI()
                || !eventId.getURI().startsWith(
                        EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue())) {
            throw new IllegalArgumentException(
                    "The event id must be an URI starting by "
                            + EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue()
                            + ": " + eventId);
        }

        // the reconstruction operation for an event which has been already
        // received is cancelled
        if (this.eventIdsReceived.containsKey(eventId)) {
            return null;
        }

        int expectedNumberOfQuadruples = -1;

        List<Quadruple> quadsReceived = new ArrayList<Quadruple>();
        Set<LongLong> quadHashesReceived = new HashSet<LongLong>();

        QuadruplePattern reconstructPattern =
                new QuadruplePattern(eventId, Node.ANY, Node.ANY, Node.ANY);

        // perform polling while all the quadruples have not been retrieved
        while (quadsReceived.size() != expectedNumberOfQuadruples) {
            // the reconstruct operation is stopped if an another thread has
            // already reconstructed the compound event before the current one
            if (this.eventIdsReceived.containsKey(eventId)) {
                return null;
            }

            log.info(
                    "Reconstructing compound event for subscription {} and graph value {} ({}/{})",
                    new Object[] {
                            id, eventId, quadsReceived.size(),
                            expectedNumberOfQuadruples});

            List<Quadruple> quads;
            try {
                quads =
                        ((QuadruplePatternResponse) PAFuture.getFutureValue(super.selectPeer()
                                .send(
                                        new ReconstructCompoundEventRequest(
                                                reconstructPattern,
                                                quadHashesReceived)))).getResult();
            } catch (DispatchException e) {
                log.error(e.getMessage(), e);
                quads = new ArrayList<Quadruple>();
            }

            for (Quadruple quad : quads) {
                if (quad.getPredicate().equals(
                        PublishSubscribeConstants.EVENT_NB_QUADRUPLES_NODE)) {
                    expectedNumberOfQuadruples =
                            (Integer) quad.getObject().getLiteralValue();
                }
                quadsReceived.add(quad);
                quadHashesReceived.add(quad.hashValue());
            }

            try {
                Thread.sleep(EventCloudProperties.RECONSTRUCTION_RETRY_THRESHOLD.getValue());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (this.eventIdsReceived.putIfAbsent(eventId, id) != null) {
            // an another thread has already reconstructed the same event
            return null;
        }

        // We create an event from quadruples which comes from a previous event.
        // Hence we do not need to add new meta information
        return new CompoundEvent(quadsReceived, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(SubscriptionId id) {
        if (!this.subscriptions.containsKey(id)) {
            throw new IllegalArgumentException(
                    "No subscription registered with the specified subscription id: "
                            + id);
        }

        // once the subscription id is removed from the list of the
        // subscriptions which are matched, the notifications which are received
        // for this subscription are ignored
        Subscription subscription = this.subscriptions.remove(id);
        this.listeners.remove(id);
        this.eventIdsReceived.values().remove(id);

        // updates the network to stop sending notifications
        for (Subsubscription subSubscription : subscription.getSubSubscriptions()) {
            try {
                super.selectPeer()
                        .send(
                                new UnsubscribeRequest(
                                        subscription.getOriginalId(),
                                        subSubscription.getAtomicQuery(),
                                        subscription.getType() == NotificationListenerType.BINDING));
            } catch (DispatchException e) {
                e.printStackTrace();
            }
        }
    }

    private void deliver(NotificationId id) {
        NotificationListener<?> listener =
                this.listeners.get(id.getSubscriptionId());

        Solution solution = this.solutions.remove(id);

        if (listener instanceof BindingNotificationListener) {
            this.deliver(id, (BindingNotificationListener) listener, solution);
        } else if (listener instanceof CompoundEventNotificationListener) {
            this.deliver(
                    id, (CompoundEventNotificationListener) listener, solution);
        } else if (listener instanceof SignalNotificationListener) {
            this.deliver(id, (SignalNotificationListener) listener);
        } else {
            log.error(
                    "Unknown notification listener for delivery: {}",
                    listener.getClass());
        }

        log.info("Notification {} has been delivered", id);
    }

    private final void deliver(NotificationId id,
                               BindingNotificationListener listener,
                               Solution solution) {
        listener.onNotification(id.getSubscriptionId(), solution.getSolution());
    }

    private final void deliver(NotificationId id,
                               CompoundEventNotificationListener listener,
                               Solution solution) {
        CompoundEvent compoundEvent =
                this.reconstructCompoundEvent(
                        this.subscriptions.get(id.getSubscriptionId()),
                        solution.getSolution());

        if (compoundEvent != null) {
            listener.onNotification(id.getSubscriptionId(), compoundEvent);
        }
    }

    private final void deliver(NotificationId id,
                               SignalNotificationListener listener) {
        listener.onNotification(id.getSubscriptionId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void receive(Notification notification) {
        SubscriptionId subscriptionId =
                notification.getId().getSubscriptionId();

        Subscription subscription = this.subscriptions.get(subscriptionId);

        // this condition is used to ignore the notifications which may be
        // received after an unsubscribe operation because the unsubscribe
        // operation is not atomic
        if (!this.subscriptions.containsKey(subscriptionId)) {
            return;
        }

        log.debug(
                "New notification received {} on {} for subscription id {}",
                new Object[] {notification, this.componentUri, subscriptionId});

        // avoid creation of solution object when possible
        Solution solution = this.solutions.get(notification.getId());

        if (solution == null) {
            solution =
                    new Solution(
                            subscription.getSubSubscriptions().length,
                            notification.getBinding());

            Solution solution2 = null;
            if ((solution2 =
                    this.solutions.putIfAbsent(notification.getId(), solution)) != null) {
                solution = solution2;
                // an another thread has already put the solution
                solution.addSubSolution(notification.getBinding());
            }
        } else {
            solution.addSubSolution(notification.getBinding());
        }

        // checks whether all the sub-solutions have been received
        if (solution.isReady()
                || subscription.getType() == NotificationListenerType.COMPOUND_EVENT
                || subscription.getType() == NotificationListenerType.SIGNAL) {
            // TODO checks whether the ELA properties are verified
            // if yes, deliver the solution else do nothing and wait for an ELA
            // property that is verified
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

    /**
     * Returns the URI at which the component is bind.
     * 
     * @return the URI at which the component is bind.
     */
    public String getComponentUri() {
        return this.componentUri;
    }

}
