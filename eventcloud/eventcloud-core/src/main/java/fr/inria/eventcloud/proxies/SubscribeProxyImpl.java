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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.jdbm.DB;
import org.apache.jdbm.DBMaker;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.component.body.ComponentEndActive;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxies;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.HashCode;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.listeners.BindingWrapperNotificationListener;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;
import fr.inria.eventcloud.api.listeners.NotificationListener;
import fr.inria.eventcloud.api.listeners.NotificationListenerType;
import fr.inria.eventcloud.api.listeners.SignalNotificationListener;
import fr.inria.eventcloud.api.properties.AlterableElaProperty;
import fr.inria.eventcloud.api.wrappers.BindingWrapper;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.Vars;
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
public class SubscribeProxyImpl extends Proxy implements ComponentEndActive,
        SubscribeProxy, SubscribeProxyAttributeController {

    private static final long serialVersionUID = 1L;

    private static final String DB_NAME = "eventIdsReceived";

    /**
     * ADL name of the subscribe proxy component.
     */
    public static final String SUBSCRIBE_PROXY_ADL =
            "fr.inria.eventcloud.proxies.SubscribeProxy";

    /**
     * Functional interface name of the subscribe proxy component.
     */
    public static final String SUBSCRIBE_SERVICES_ITF = "subscribe-services";

    private static final Logger log =
            LoggerFactory.getLogger(SubscribeProxy.class);

    // contains the subscriptions that have been registered from this proxy
    private ConcurrentMap<SubscriptionId, Subscription> subscriptions;

    // contains the listeners to use in order to deliver the solutions
    private ConcurrentMap<SubscriptionId, NotificationListener<?>> listeners;

    // contains the solutions that are being received
    private ConcurrentMap<NotificationId, Solution> solutions;

    private DB eventIdsReceivedDB;

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

        this.createAndRegisterEventIdsReceivedDB(body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endComponentActivity(Body body) {
        this.unsubscribeAndCloseDB();
    }

    private void createAndRegisterEventIdsReceivedDB(Body body) {
        String dbPath =
                EventCloudProperties.getDefaultTemporaryPath() + "jdbm"
                        + File.separatorChar;

        new File(dbPath).mkdirs();

        String dbFilename = dbPath + body.getID();

        this.eventIdsReceivedDB =
                DBMaker.openFile(dbFilename)
                        .deleteFilesAfterClose()
                        .disableLocking()
                        .disableTransactions()
                        .enableSoftCache()
                        .make();

        this.eventIdsReceivedDB.createHashMap(DB_NAME);
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
            this.subscriptions =
                    new ConcurrentHashMap<SubscriptionId, Subscription>();
            this.listeners =
                    new ConcurrentHashMap<SubscriptionId, NotificationListener<?>>();
            this.solutions = new ConcurrentHashMap<NotificationId, Solution>();

            // TODO: use the properties field to initialize ELA properties

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    SubscribeProxyImpl.this.unsubscribeAndCloseDB();
                }
            }));
        }
    }

    private void unsubscribeAndCloseDB() {
        if (!this.eventIdsReceivedDB.isClosed()) {
            // removes remaining subscriptions at shutdown
            Iterator<SubscriptionId> it =
                    SubscribeProxyImpl.this.subscriptions.keySet().iterator();

            while (it.hasNext()) {
                SubscriptionId id = it.next();
                SubscribeProxyImpl.this.unsubscribe(id);
                it.remove();
            }

            this.eventIdsReceivedDB.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
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
                        this.componentUri,
                        subscription.getSubscriptionDestination(),
                        listener.getType());

        if (this.listeners.put(subscription.getId(), listener) != null) {
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

        super.sendv(new IndexSubscriptionRequest(internalSubscription));

        log.info(
                "New subscription has been registered from {} with id {}",
                PAActiveObject.getBodyOnThis().getUrl(), subscription.getId());
    }

    @MemberOf("parallel")
    private Node extractEventId(Subscription subscription, Binding binding) {
        if (!subscription.getGraphNode().isVariable()) {
            throw new IllegalArgumentException(
                    "The subscription graph node is not a variable");
        }

        Node eventId;
        if ((eventId = binding.get(Vars.GRAPH)) == null) {
            throw new IllegalArgumentException(
                    "The specified binding does not contain a graph value");
        }

        return eventId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public final CompoundEvent reconstructCompoundEvent(Subscription subscription,
                                                        Binding binding) {
        return this.reconstructCompoundEvent(
                subscription.getId(),
                this.extractEventId(subscription, binding));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public final CompoundEvent reconstructCompoundEvent(SubscriptionId id,
                                                        Node eventId) {
        if (!eventId.isURI()) {
            throw new IllegalArgumentException("The event id must be an URI:"
                    + eventId);
        }

        // The reconstruction operation for an event (quadruple) which has been
        // already received is cancelled. If someone subscribe without any
        // constraint and a compound event notification listener, each quadruple
        // or event is a solution and thus the subscribe proxy receives a
        // notification for each quadruple even if all these quadruples are part
        // of the same compound event.
        if (this.getEventIdsReceived().containsKey(eventId.getURI())) {
            return null;
        }

        int expectedNumberOfQuadruples = -1;

        List<Quadruple> quadsReceived = new ArrayList<Quadruple>();
        Set<HashCode> quadHashesReceived = new HashSet<HashCode>();

        QuadruplePattern reconstructPattern =
                new QuadruplePattern(eventId, Node.ANY, Node.ANY, Node.ANY);

        // perform polling while all the quadruples have not been retrieved
        while (quadsReceived.size() != expectedNumberOfQuadruples) {
            // the reconstruct operation is stopped if an another thread has
            // already reconstructed the compound event before the current one
            if (this.getEventIdsReceived().containsKey(eventId.getURI())) {
                return null;
            }

            log.info(
                    "Reconstructing compound event for subscription {} and graph value {} ({}/{})",
                    new Object[] {
                            id, eventId, quadsReceived.size(),
                            expectedNumberOfQuadruples});

            List<Quadruple> quads =
                    ((QuadruplePatternResponse) PAFuture.getFutureValue(super.selectPeer()
                            .send(
                                    new ReconstructCompoundEventRequest(
                                            reconstructPattern,
                                            quadHashesReceived)))).getResult();

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

        if (this.getEventIdsReceived().putIfAbsent(eventId.getURI(), id) != null) {
            // an another thread has already reconstructed the same event
            return null;
        }

        // we create an event from quadruples which comes from a previous event.
        // Hence we do not need to add new meta information
        return new CompoundEvent(quadsReceived, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
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
        this.getEventIdsReceived().values().remove(id);

        // updates the network to stop sending notifications
        for (Subsubscription subSubscription : subscription.getSubSubscriptions()) {
            super.selectPeer()
                    .send(
                            new UnsubscribeRequest(
                                    subscription.getOriginalId(),
                                    subSubscription.getAtomicQuery(),
                                    subscription.getType() == NotificationListenerType.BINDING));
        }
    }

    @MemberOf("parallel")
    private void deliver(NotificationId id) {
        NotificationListener<?> listener =
                this.listeners.get(id.getSubscriptionId());

        Solution solution = this.solutions.remove(id);

        if (listener instanceof BindingNotificationListener) {
            this.deliver(id, (BindingNotificationListener) listener, solution);
        } else if (listener instanceof BindingWrapperNotificationListener) {
            this.deliver(
                    id, (BindingWrapperNotificationListener) listener, solution);
        } else if (listener instanceof CompoundEventNotificationListener) {
            this.deliver(
                    id, (CompoundEventNotificationListener) listener, solution);
        } else if (listener instanceof SignalNotificationListener) {
            this.deliver(id, (SignalNotificationListener) listener, solution);
        } else {
            log.error(
                    "Unknown notification listener for delivery: {}",
                    listener.getClass());
        }

        log.info("Notification {} has been delivered", id);
    }

    @MemberOf("parallel")
    private final void deliver(NotificationId id,
                               BindingNotificationListener listener,
                               Solution solution) {
        listener.onNotification(id.getSubscriptionId(), solution.getSolution());

        this.sendInputOutputMonitoringReportIfNecessary(
                id.getSubscriptionId(), solution, listener.getSubscriberUrl());
    }

    @MemberOf("parallel")
    private final void deliver(NotificationId id,
                               BindingWrapperNotificationListener listener,
                               Solution solution) {
        listener.onNotification(id.getSubscriptionId(), new BindingWrapper(
                solution.getSolution()));

        this.sendInputOutputMonitoringReportIfNecessary(
                id.getSubscriptionId(), solution, listener.getSubscriberUrl());
    }

    @MemberOf("parallel")
    private final void deliver(NotificationId id,
                               CompoundEventNotificationListener listener,
                               Solution solution) {
        CompoundEvent compoundEvent =
                this.reconstructCompoundEvent(
                        this.subscriptions.get(id.getSubscriptionId()),
                        solution.getSolution());

        if (P2PStructuredProperties.ENABLE_BENCHMARKS_INFORMATION.getValue()) {
            for (int i = 0; i < compoundEvent.size(); i++) {
                log.info("Reconstructed compound event containing quadruple : "
                        + compoundEvent.getQuadruples().get(i));
            }
        }

        if (compoundEvent != null
                && this.subscriptions.containsKey(id.getSubscriptionId())) {
            listener.onNotification(id.getSubscriptionId(), compoundEvent);

            this.sendInputOutputMonitoringReport(
                    id.getSubscriptionId(), solution.getSolution(),
                    listener.getSubscriberUrl());
        }
    }

    @MemberOf("parallel")
    private final void deliver(NotificationId id,
                               SignalNotificationListener listener,
                               Solution solution) {
        listener.onNotification(id.getSubscriptionId());

        this.sendInputOutputMonitoringReportIfNecessary(
                id.getSubscriptionId(), solution, listener.getSubscriberUrl());
    }

    /**
     * This method is used to send an input/output monitoring report per
     * compound event event and not per notification which is received, even if
     * a subscriber has subscribed with a SignalNotificationListener or a
     * BindingNotificationListener.
     * 
     * @param id
     *            the subscription id.
     * @param solution
     *            the solution received.
     * @param subscriberUrl
     *            the subscriber url.
     */
    @MemberOf("parallel")
    private void sendInputOutputMonitoringReportIfNecessary(SubscriptionId id,
                                                            Solution solution,
                                                            String subscriberUrl) {
        Subscription subscription;
        subscription = this.subscriptions.get(id);

        Node eventId =
                this.extractEventId(subscription, solution.getSolution());

        if (this.getEventIdsReceived().put(eventId.getURI(), id) == null) {
            this.sendInputOutputMonitoringReport(eventId, subscriberUrl);
        }
    }

    @MemberOf("parallel")
    private void sendInputOutputMonitoringReport(SubscriptionId id,
                                                 Binding binding,
                                                 String subscriberUrl) {
        if (super.monitoringManager != null) {
            Subscription subscription = this.subscriptions.get(id);

            Node eventId = this.extractEventId(subscription, binding);

            this.sendInputOutputMonitoringReport(eventId, subscriberUrl);
        }
    }

    @MemberOf("parallel")
    private void sendInputOutputMonitoringReport(Node eventId,
                                                 String subscriberUrl) {
        if (super.monitoringManager != null) {
            String destination = this.componentUri;
            if (subscriberUrl != null) {
                destination = subscriberUrl;
            }

            String source = Quadruple.getPublicationSource(eventId);
            if (source == null) {
                source = "http://0.0.0.0";
            }

            super.monitoringManager.sendInputOutputMonitoringReport(
                    source, destination, Quadruple.getPublicationTime(eventId));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public void receive(Notification notification) {
        SubscriptionId subscriptionId =
                notification.getId().getSubscriptionId();

        Subscription subscription = this.subscriptions.get(subscriptionId);

        // this condition is used to ignore the notifications which may be
        // received after an unsubscribe operation because the unsubscribe
        // operation is not atomic
        if (subscription == null) {
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

    @MemberOf("parallel")
    private ConcurrentMap<Object, Object> getEventIdsReceived() {
        return this.eventIdsReceivedDB.getHashMap(DB_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public Subscription find(SubscriptionId id) {
        return this.subscriptions.get(id);
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

    /**
     * Lookups a subscribe proxy component on the specified {@code componentUri}
     * .
     * 
     * @param componentUri
     *            the URL of the subscribe proxy component.
     * 
     * @return the reference on the {@link SubscribeApi} interface of the
     *         subscribe proxy component.
     * 
     * @throws IOException
     *             if an error occurs during the construction of the stub.
     * 
     * @deprecated This method will be removed for the next release. Please use
     *             {@link ProxyFactory#lookupSubscribeProxy(String)} instead.
     */
    @Deprecated
    @MemberOf("parallel")
    public static SubscribeProxy lookup(String componentUri) throws IOException {
        return ComponentUtils.lookupFcInterface(
                componentUri, SUBSCRIBE_SERVICES_ITF, SubscribeProxy.class);
    }

}
