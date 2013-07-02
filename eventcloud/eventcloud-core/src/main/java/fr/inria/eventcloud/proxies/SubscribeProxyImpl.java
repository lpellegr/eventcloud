/**
 * Copyright (c) 2011-2013 INRIA.
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
package fr.inria.eventcloud.proxies;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.component.body.ComponentEndActive;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxies;
import org.objectweb.proactive.extensions.p2p.structured.utils.UnicodeUtils;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.HashCode;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.CompoundEvent;
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
import fr.inria.eventcloud.formatters.QuadruplesFormatter;
import fr.inria.eventcloud.messages.request.can.ReconstructCompoundEventRequest;
import fr.inria.eventcloud.messages.request.can.RemoveEphemeralSubscriptionRequest;
import fr.inria.eventcloud.messages.request.can.UnsubscribeRequest;
import fr.inria.eventcloud.messages.response.can.QuadruplePatternResponse;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.pubsub.Subsubscription;
import fr.inria.eventcloud.pubsub.notifications.BindingNotification;
import fr.inria.eventcloud.pubsub.notifications.Notification;
import fr.inria.eventcloud.pubsub.notifications.NotificationId;
import fr.inria.eventcloud.pubsub.notifications.PollingSignalNotification;
import fr.inria.eventcloud.pubsub.notifications.QuadruplesNotification;
import fr.inria.eventcloud.pubsub.notifications.SignalNotification;
import fr.inria.eventcloud.pubsub.solutions.BindingSolution;
import fr.inria.eventcloud.pubsub.solutions.QuadruplesSolution;

/**
 * SubscribeProxyImpl is a concrete implementation of {@link SubscribeProxy}.
 * This class has to be instantiated as a ProActive/GCM component.
 * 
 * @author lpellegr
 * @author bsauvan
 * 
 * @see ProxyFactory
 */
@DefineGroups({@Group(name = "parallel", selfCompatible = true)})
public class SubscribeProxyImpl extends AbstractProxy implements
        ComponentEndActive, SubscribeProxy, SubscribeProxyAttributeController {

    private static final long serialVersionUID = 150L;

    /**
     * ADL name of the subscribe proxy component.
     */
    public static final String SUBSCRIBE_PROXY_ADL =
            "fr.inria.eventcloud.proxies.SubscribeProxy";

    /**
     * Functional interface name of the subscribe proxy component.
     */
    public static final String SUBSCRIBE_SERVICES_ITF = "subscribe-services";

    /**
     * GCM Virtual Node name of the subscribe proxy component.
     */
    public static final String SUBSCRIBE_PROXY_VN = "SubscribeProxyVN";

    private static final Logger log =
            LoggerFactory.getLogger(SubscribeProxyImpl.class);

    private CacheManager cacheManager;

    // contains the ids of the events that have been delivered.
    // it is used to remove duplicates that may be received with SBCE1 and 2
    private Cache eventsDeliveredCache;

    // contains the subscriptions that have been registered from this proxy
    private ConcurrentMap<SubscriptionId, SubscriptionEntry<?>> subscriptions;

    // contains the binding solutions that are being received
    private ConcurrentMap<NotificationId, BindingSolution> bindingSolutions;

    // contains the quadruples solutions that are being received
    private ConcurrentMap<NotificationId, QuadruplesSolution> quadruplesSolutions;

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
        body.setImmediateService("setAttributes", false);

        this.createEventsDeliveredCache(body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runComponentActivity(Body body) {
        new MultiActiveService(body).multiActiveServing(
                EventCloudProperties.MAO_HARD_LIMIT_SUBSCRIBE_PROXIES.getValue(),
                true, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endComponentActivity(Body body) {
        this.closeEventsDeliveredCache();
    }

    private void createEventsDeliveredCache(Body body) {
        String cachePath =
                EventCloudProperties.getDefaultTemporaryPath() + "ehcache"
                        + File.separatorChar + body.getID().toString();

        Cache eventsDeliveredCache =
                new Cache(
                        new CacheConfiguration("cache", 0).memoryStoreEvictionPolicy(
                                MemoryStoreEvictionPolicy.LRU)
                                .eternal(true)
                                .persistence(
                                        new PersistenceConfiguration().strategy(Strategy.LOCALTEMPSWAP))
                                .transactionalMode(TransactionalMode.OFF));
        eventsDeliveredCache.disableDynamicFeatures();

        Configuration cacheManagerConfig =
                new Configuration().diskStore(new DiskStoreConfiguration().path(cachePath));
        this.cacheManager = CacheManager.create(cacheManagerConfig);
        this.cacheManager.addCache(eventsDeliveredCache);

        this.eventsDeliveredCache = this.cacheManager.getCache("cache");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttributes(EventCloudCache proxy, String componentUri,
                              AlterableElaProperty[] properties) {
        if (super.eventCloudCache == null) {
            super.eventCloudCache = proxy;
            super.proxy = Proxies.newProxy(super.eventCloudCache.getTrackers());

            this.componentUri = componentUri;

            // even if we could have
            // EventCloudProperties.MAO_SOFT_LIMIT_SUBSCRIBE_PROXIES
            // threads handling subscriptions in parallel, the subscribe method
            // is not supposed to be called very often. We may allow some
            // contention at the price of less memory to be used
            this.subscriptions =
                    new ConcurrentHashMap<SubscriptionId, SubscriptionEntry<?>>(
                            100, 0.90f, 2);

            this.bindingSolutions =
                    new ConcurrentHashMap<NotificationId, BindingSolution>(
                            // At most
                            // EventCloudProperties.MAO_SOFT_LIMIT_SUBSCRIBE_PROXIES
                            // threads can update the map in parallel. Each of
                            // them can add part of a solution. The solution is
                            // eventually removed when all the sub solutions are
                            // received. Thus if we suppose that the average
                            // number of sub solutions that compose a solution
                            // is
                            // EventCloudProperties.AVERAGE_NB_QUADRUPLES_PER_COMPOUND_EVENT
                            // we get the following formula
                            EventCloudProperties.MAO_HARD_LIMIT_SUBSCRIBE_PROXIES.getValue()
                                    * EventCloudProperties.AVERAGE_NB_QUADRUPLES_PER_COMPOUND_EVENT.getValue(),
                            0.75f,
                            EventCloudProperties.MAO_HARD_LIMIT_SUBSCRIBE_PROXIES.getValue());
            this.quadruplesSolutions =
                    new ConcurrentHashMap<NotificationId, QuadruplesSolution>(
                            EventCloudProperties.MAO_HARD_LIMIT_SUBSCRIBE_PROXIES.getValue()
                                    * EventCloudProperties.AVERAGE_NB_QUADRUPLES_PER_COMPOUND_EVENT.getValue(),
                            0.75f,
                            EventCloudProperties.MAO_HARD_LIMIT_SUBSCRIBE_PROXIES.getValue());

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    SubscribeProxyImpl.this.closeEventsDeliveredCache();
                }
            }));
        }
    }

    private void closeEventsDeliveredCache() {
        this.cacheManager.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public void subscribe(fr.inria.eventcloud.api.Subscription subscription,
                          BindingNotificationListener listener) {
        this.indexSubscription(subscription, listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public void subscribe(fr.inria.eventcloud.api.Subscription subscription,
                          CompoundEventNotificationListener listener) {

        String sparqlQuery;

        if (EventCloudProperties.isSbce1PubSubAlgorithmUsed()
                || EventCloudProperties.isSbce2PubSubAlgorithmUsed()) {
            // rewrites the SPARQL query to keep only the graph variable in
            // the result variables. Indeed we need only the graph variable
            // (which identifies the event which is matched) to
            // reconstruct the compound event

            sparqlQuery =
                    PublishSubscribeUtils.removeResultVarsExceptGraphVar(subscription.getSparqlQuery());
        } else {
            sparqlQuery = subscription.getSparqlQuery();
        }

        this.indexSubscription(subscription, sparqlQuery, listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public void subscribe(fr.inria.eventcloud.api.Subscription subscription,
                          SignalNotificationListener listener) {
        this.indexSubscription(subscription, listener);
    }

    private void indexSubscription(fr.inria.eventcloud.api.Subscription subscription,
                                   NotificationListener<?> listener) {
        this.indexSubscription(
                subscription, subscription.getSparqlQuery(), listener);
    }

    private void indexSubscription(fr.inria.eventcloud.api.Subscription subscription,
                                   String sparqlQuery,
                                   NotificationListener<?> listener) {
        Subscription internalSubscription =
                createInternalSubscription(
                        subscription, this.componentUri, sparqlQuery,
                        listener.getType());

        if (this.subscriptions.putIfAbsent(
                subscription.getId(),
                new SubscriptionEntry<NotificationListener<?>>(
                        internalSubscription, listener)) != null) {
            // same subscription with same creation time
            throw new IllegalArgumentException(
                    "Subscription already registered for subscription id: "
                            + internalSubscription.getId());
        }

        super.selectPeer().subscribe(internalSubscription);

        log.info(
                "New subscription has been registered from {} with id {}",
                PAActiveObject.getBodyOnThis().getUrl(),
                internalSubscription.getId());
    }

    private static Subscription createInternalSubscription(fr.inria.eventcloud.api.Subscription subscription,
                                                           String componentUri,
                                                           String sparqlSubscription,
                                                           NotificationListenerType listenerType) {
        return new Subscription(
                subscription.getId(), null, subscription.getId(),
                subscription.getCreationTime(), sparqlSubscription,
                componentUri, subscription.getSubscriptionDestination(),
                listenerType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public void unsubscribe(SubscriptionId sid) {
        // once the subscription id is removed from the list of the
        // subscriptions which are matched, the notifications which are received
        // for this subscription are ignored
        SubscriptionEntry<?> subscriptionEntry = this.subscriptions.remove(sid);

        if (subscriptionEntry == null) {
            throw new IllegalArgumentException(
                    "No subscription registered with the specified subscription id: "
                            + sid);
        }

        Subscription subscription = subscriptionEntry.subscription;

        // updates the network to stop sending notifications
        for (Subsubscription subSubscription : subscription.getSubSubscriptions()) {
            super.selectPeer()
                    .send(
                            new UnsubscribeRequest(
                                    subscription.getOriginalId(),
                                    subSubscription.getAtomicQuery(),
                                    subscription.getType() == NotificationListenerType.BINDING));
        }

        @SuppressWarnings("unchecked")
        List<NotificationId> notificationIds =
                this.eventsDeliveredCache.getKeysNoDuplicateCheck();

        // TODO we should avoid to iterate on all the keys
        // a solution is to maintain the reverse mapping subscriptionId ->
        // notificationId
        for (NotificationId nid : notificationIds) {
            if (nid.isFor(sid)) {
                this.eventsDeliveredCache.remove(nid);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public void receiveSbce1Or2(BindingNotification notification) {
        this.logNotificationReception(notification);

        SubscriptionId subscriptionId = notification.getSubscriptionId();
        @SuppressWarnings("unchecked")
        SubscriptionEntry<BindingNotificationListener> subscriptionEntry =
                (SubscriptionEntry<BindingNotificationListener>) this.subscriptions.get(subscriptionId);

        // ignore the notifications which may be received after an unsubscribe
        // operation because the unsubscribe operation is not atomic
        if (subscriptionEntry == null) {
            return;
        }

        // avoid creation of solution object when possible
        BindingSolution solution =
                this.bindingSolutions.get(notification.getId());

        if (solution == null) {
            solution =
                    new BindingSolution(
                            subscriptionEntry.subscription.getResultVars()
                                    .size(), notification.getContent());

            BindingSolution tmpSolution = null;
            if ((tmpSolution =
                    this.bindingSolutions.putIfAbsent(
                            notification.getId(), solution)) != null) {
                // another thread has already put the solution
                solution = tmpSolution;
                solution.merge(notification.getContent());
            }
        } else {
            solution.merge(notification.getContent());
        }

        // checks that all the sub solutions have been received and that a
        // notification has not been yet delivered for the eventId associated to
        // this notification
        if (solution.isReady()) {
            if (this.markAsDelivered(notification.getId(), subscriptionId)) {
                this.deliver(subscriptionEntry, solution.getChunks());
            }

            this.bindingSolutions.remove(notification.getId());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public void receiveSbce3(BindingNotification notification) {
        this.logNotificationReception(notification);

        SubscriptionId subscriptionId = notification.getSubscriptionId();

        @SuppressWarnings("unchecked")
        SubscriptionEntry<BindingNotificationListener> subscriptionEntry =
                (SubscriptionEntry<BindingNotificationListener>) this.subscriptions.get(subscriptionId);

        // ignore the notifications that may be received for a subscription
        // which has been unregistered but also notifications that have been
        // already received for a same event
        if (subscriptionEntry != null
                && this.markAsDelivered(notification.getId(), subscriptionId)) {
            this.deliver(subscriptionEntry, notification.getContent());
        }
    }

    private void deliver(SubscriptionEntry<BindingNotificationListener> entry,
                         Binding solution) {
        entry.listener.onNotification(entry.subscription.getId(), solution);

        // do not output integration message and do not send monitoring
        // information for binding listener
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    @SuppressWarnings("unchecked")
    public void receiveSbce2(QuadruplesNotification notification) {
        this.logNotificationReception(notification);

        SubscriptionId subscriptionId = notification.getSubscriptionId();

        if (this.eventsDeliveredCache.getQuiet(notification.getId()) != null) {
            this.handleReceiveDuplicateSolution(notification);
            return;
        }

        if (log.isTraceEnabled()) {
            log.trace("Received quadruples notification subscriptionId="
                    + subscriptionId
                    + ", contentSize="
                    + notification.getContent().size()
                    + ", from="
                    + notification.getSource()
                    + "\n"
                    + QuadruplesFormatter.toString(
                            notification.getContent(), true));
        }

        // avoid creation of solution object when possible
        QuadruplesSolution solution =
                this.quadruplesSolutions.get(notification.getId());

        if (solution == null) {
            solution = new QuadruplesSolution(notification.getContent());

            QuadruplesSolution tmpSolution = null;
            if ((tmpSolution =
                    this.quadruplesSolutions.putIfAbsent(
                            notification.getId(), solution)) != null) {
                // an another thread has already put the solution
                solution = tmpSolution;
                solution.merge(notification.getContent());
            }
        } else {
            solution.merge(notification.getContent());
        }

        if (solution.isReady()) {
            // received some quadruple duplicates for a CE which has already be
            // delivered. In such a case we have to ignore these duplicates and
            // send a RemoveEphemeralSubscription to avoid a memory leak
            if (!this.markAsDelivered(notification.getId(), subscriptionId)) {
                // this.handleReceiveDuplicateSolution(notification);
            } else {
                CompoundEvent compoundEvent =
                        new CompoundEvent(solution.getChunks());

                SubscriptionEntry<?> subscriptionEntry =
                        this.subscriptions.get(subscriptionId);

                // ignore the notifications that may be received for a
                // subscription which has been unregistered
                if (subscriptionEntry != null) {
                    this.deliver(
                            (SubscriptionEntry<CompoundEventNotificationListener>) subscriptionEntry,
                            compoundEvent.getGraph().getURI(), compoundEvent);
                }

                // this.sendRemoveEphemeralSubscription(
                // compoundEvent.getGraph(), subscriptionId);
                this.quadruplesSolutions.remove(notification.getId());
            }
        }
    }

    private void handleReceiveDuplicateSolution(QuadruplesNotification notification) {
        log.info(
                "Received some quadruple duplicates for a CE that has already been delivered. They will be ignored:\n{}",
                notification);

        this.sendRemoveEphemeralSubscription(notification.getContent()
                .get(0)
                .getGraph(), notification.getSubscriptionId());

        this.quadruplesSolutions.remove(notification.getId());
    }

    private void sendRemoveEphemeralSubscription(Node graph,
                                                 SubscriptionId subscriptionId) {
        super.selectPeer().sendv(
                new RemoveEphemeralSubscriptionRequest(graph, subscriptionId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    @SuppressWarnings("unchecked")
    public void receiveSbce3(QuadruplesNotification notification) {
        this.logNotificationReception(notification);

        SubscriptionEntry<CompoundEventNotificationListener> subscriptionEntry =
                (SubscriptionEntry<CompoundEventNotificationListener>) this.subscriptions.get(notification.getSubscriptionId());

        // ignore the notifications that may be received for a
        // subscription which has been unregistered but also
        // notifications that have been already received for a same
        // event
        if (subscriptionEntry != null
                && this.markAsDelivered(
                        notification.getId(), notification.getSubscriptionId())) {

            CompoundEvent compoundEvent =
                    new CompoundEvent(notification.getContent());

            this.deliver(
                    subscriptionEntry, compoundEvent.getGraph().getURI(),
                    compoundEvent);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public void receiveSbce1Or2(SignalNotification notification) {
        this.receive(notification);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public void receiveSbce3(SignalNotification notification) {
        this.receive(notification);
    }

    private void receive(SignalNotification notification) {
        this.logNotificationReception(notification);

        SubscriptionId subscriptionId = notification.getSubscriptionId();

        @SuppressWarnings("unchecked")
        SubscriptionEntry<SignalNotificationListener> subscriptionEntry =
                (SubscriptionEntry<SignalNotificationListener>) this.subscriptions.get(subscriptionId);

        // ignore the notifications that may be received for a subscription
        // which has been unregistered but also notifications that have been
        // already received for a same event
        if (subscriptionEntry != null
                && this.markAsDelivered(
                        notification.getId(), notification.getSubscriptionId())) {
            this.deliver(subscriptionEntry, notification.getMetaEventId());
        }
    }

    private void deliver(SubscriptionEntry<SignalNotificationListener> entry,
                         String eventId) {
        entry.listener.onNotification(entry.subscription.getId(), eventId);

        // do not output integration message and do not send monitoring
        // information for signal listener
    }

    /**
     * This method is invoked remotely by a peer when algorithm SBCE1 is used
     * and when a solution matching a subscription is found for a subscriber
     * that has registered a subscription along with a
     * {@link CompoundEventNotificationListener}.
     */
    @Override
    @MemberOf("parallel")
    @SuppressWarnings("unchecked")
    public void receiveSbce1(PollingSignalNotification notification) {
        this.logNotificationReception(notification);

        SubscriptionId subscriptionId = notification.getSubscriptionId();

        SubscriptionEntry<CompoundEventNotificationListener> subscriptionEntry =
                (SubscriptionEntry<CompoundEventNotificationListener>) this.subscriptions.get(subscriptionId);

        // checks that the subscription has not been removed been while we are
        // receiving and delivering the notification
        if (subscriptionEntry != null) {
            CompoundEvent compoundEvent =
                    this.reconstructCompoundEvent(
                            notification.getId(),
                            subscriptionId,
                            NodeFactory.createURI(notification.getMetaEventId()));

            if (compoundEvent != null) {
                this.deliver(
                        subscriptionEntry, notification.getMetaEventId(),
                        compoundEvent);
            }
        }
    };

    private void deliver(SubscriptionEntry<CompoundEventNotificationListener> entry,
                         String graph, CompoundEvent compoundEvent) {
        SubscriptionId subscriptionId = entry.subscription.getId();
        CompoundEventNotificationListener listener = entry.listener;

        listener.onNotification(subscriptionId, compoundEvent);

        this.sendInputOutputMonitoringReport(graph, listener.getSubscriberUrl());

        this.logIntegrationInformation(graph);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public final CompoundEvent reconstructCompoundEvent(NotificationId notificationId,
                                                        SubscriptionId subscriptionId,
                                                        Node eventId) {
        // the reconstruction operation for an event (quadruple) which has
        // already started is cancelled. This is to avoid duplicates in the case
        // where someone subscribe without any constraint but with a compound
        // event notification listener. Then, each quadruple or event is a
        // solution. Thus, the subscribe proxy receives a notification for each
        // quadruple even if all these quadruples are part of the same compound
        // event.
        if (!this.markAsDelivered(notificationId, subscriptionId)) {
            return null;
        }

        int expectedNbQuadruples = -1;

        List<Quadruple> quadsReceived = new ArrayList<Quadruple>();
        Set<HashCode> quadHashesReceived = new HashSet<HashCode>();

        QuadruplePattern reconstructPattern =
                new QuadruplePattern(eventId, Node.ANY, Node.ANY, Node.ANY);

        int nbReconstructRequestSent = 0;
        long reconstructionStartTime = 0;

        if (log.isTraceEnabled()) {
            reconstructionStartTime = System.currentTimeMillis();
        }

        // perform polling while all the quadruples have not been retrieved
        while (quadsReceived.size() != expectedNbQuadruples) {
            if (log.isInfoEnabled()) {
                log.info(
                        "Reconstructing compound event for subscription {} and graph value {} ({}/{})",
                        new Object[] {
                                subscriptionId, eventId, quadsReceived.size(),
                                expectedNbQuadruples});
            }

            List<Quadruple> quads =
                    ((QuadruplePatternResponse) PAFuture.getFutureValue(super.selectPeer()
                            .send(
                                    new ReconstructCompoundEventRequest(
                                            reconstructPattern,
                                            quadHashesReceived)))).getResult();

            for (Quadruple q : quads) {
                if (PublishSubscribeUtils.isMetaQuadruple(q)) {
                    String objectValue = q.getObject().getLiteralLexicalForm();

                    if ('0' < P2PStructuredProperties.CAN_LOWER_BOUND.getValue()) {
                        objectValue =
                                UnicodeUtils.translate(
                                        objectValue,
                                        -(P2PStructuredProperties.CAN_LOWER_BOUND.getValue() - '0'));
                    }

                    expectedNbQuadruples = Integer.parseInt(objectValue);
                } else {
                    quadsReceived.add(q);
                }

                quadHashesReceived.add(q.hashValue());
            }

            // this condition is required to avoid a sleep on the last
            // reconstruction once all the quadruples have been retrieved
            if (quadsReceived.size() != expectedNbQuadruples) {
                try {
                    Thread.sleep(EventCloudProperties.RECONSTRUCTION_RETRY_THRESHOLD.getValue());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }

                // try {
                // Thread.sleep(ExponentialBackoff.compteValue(
                // nbReconstructRequestSent,
                // 1,
                // EventCloudProperties.RECONSTRUCTION_RETRY_THRESHOLD.getValue(),
                // 3000));
                // } catch (InterruptedException e) {
                // Thread.currentThread().interrupt();
                // }

                nbReconstructRequestSent++;
                //
                // if (quads.size() != 0) {
                // nbReconstructRequestSent = 0;
                // }
            }
        }

        if (log.isTraceEnabled()) {
            log.trace("Reconstruction for eventId " + eventId
                    + " has required " + (nbReconstructRequestSent + 1)
                    + " requests and "
                    + (System.currentTimeMillis() - reconstructionStartTime)
                    + " ms");
        }

        return new CompoundEvent(quadsReceived);
    }

    private void sendInputOutputMonitoringReport(String graph,
                                                 String destination) {
        if (super.monitoringManager != null) {
            String source = Quadruple.getPublicationSource(graph);
            if (source == null) {
                source = "http://0.0.0.0";
            }

            if (destination == null) {
                destination = this.componentUri;
            }

            long eventPublicationTimestamp =
                    Quadruple.getPublicationTime(graph);

            super.monitoringManager.sendInputOutputMonitoringReport(
                    source, destination, eventPublicationTimestamp);
        }
    }

    private void logIntegrationInformation(String graph) {
        // log information for integration test purposes
        if (log.isTraceEnabled()) {
            String msg = "EventCloud Exit";

            if (graph != null) {
                msg += " ";
                msg +=
                        Quadruple.removeMetaInformation(NodeFactory.createURI(graph));
            }

            msg += " ";
            msg += super.eventCloudCache.getId().getStreamUrl();

            log.trace(msg);
        }
    }

    private void logNotificationReception(Notification<?> notification) {
        if (log.isDebugEnabled()) {
            log.debug(
                    "New notification received {} on {} for subscription id {}",
                    new Object[] {
                            notification.getId(), this.componentUri,
                            notification.getSubscriptionId()});
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public Subscription find(SubscriptionId id) {
        return this.subscriptions.get(id).subscription;
    }

    /**
     * Returns the URI at which the component is bind.
     * 
     * @return the URI at which the component is bind.
     */
    @MemberOf("parallel")
    public String getComponentUri() {
        return this.componentUri;
    }

    private static final class SubscriptionEntry<T extends NotificationListener<?>> {

        private final Subscription subscription;

        private final T listener;

        public SubscriptionEntry(Subscription subscription, T listener) {
            this.subscription = subscription;
            this.listener = listener;
        }

    }

    private boolean markAsDelivered(NotificationId notificationId,
                                    SubscriptionId subscriptionId) {
        Element existing =
                this.eventsDeliveredCache.putIfAbsent(new Element(
                        notificationId, subscriptionId));

        return existing == null;
    }

}
