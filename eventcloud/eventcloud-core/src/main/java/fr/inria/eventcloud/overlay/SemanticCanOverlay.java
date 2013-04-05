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
package fr.inria.eventcloud.overlay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.JoinIntroduceOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.JoinIntroduceResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.utils.HomogenousPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soceda.socialfilter.relationshipstrengthengine.RelationshipStrengthEngineManager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.QuadrupleIterator;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.delayers.IndexSubscriptionRequestDelayer;
import fr.inria.eventcloud.delayers.PublishCompoundEventRequestDelayer;
import fr.inria.eventcloud.delayers.PublishQuadrupleRequestDelayer;
import fr.inria.eventcloud.messages.request.can.PublishCompoundEventRequest;
import fr.inria.eventcloud.messages.request.can.PublishQuadrupleRequest;
import fr.inria.eventcloud.overlay.can.SemanticElement;
import fr.inria.eventcloud.overlay.can.SemanticZone;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.SubscriberConnectionFailure;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.reasoner.SparqlColander;

/**
 * This class is a specialized version of {@link CanOverlay} for semantic data.
 * 
 * @author lpellegr
 */
public class SemanticCanOverlay extends CanOverlay<SemanticElement> {

    private static final Logger log =
            LoggerFactory.getLogger(SemanticCanOverlay.class);

    private RelationshipStrengthEngineManager socialFilter;

    private final LoadingCache<String, SemanticPeer> peerStubsCache;

    private final LoadingCache<SubscriptionId, Subscription> subscriptionsCache;

    private final ConcurrentMap<SubscriptionId, SubscriberConnectionFailure> subscriberConnectionFailures;

    private final TransactionalTdbDatastore miscDatastore;

    private final TransactionalTdbDatastore subscriptionsDatastore;

    private ScheduledExecutorService ephemeralSubscriptionsGarbageColletor;

    private final IndexSubscriptionRequestDelayer indexSubscriptionRequestDelayer;

    private final PublishQuadrupleRequestDelayer publishQuadrupleRequestDelayer;

    private final PublishCompoundEventRequestDelayer publishCompoundEventRequestDelayer;

    /**
     * Constructs a new overlay with the specified datastore instances.
     * 
     * @param subscriptionsDatastore
     *            the datastore instance that is used to store subscriptions.
     * 
     * @param miscDatastore
     *            the datastore instance that is used to store miscellaneous
     *            data (publications, historical data, etc.).
     * 
     * @param colanderDatastore
     *            the datastore instance that is used to filter intermediate
     *            results for SPARQL requests from a {@link SparqlColander}.
     */
    public SemanticCanOverlay(
            final TransactionalTdbDatastore subscriptionsDatastore,
            TransactionalTdbDatastore miscDatastore,
            TransactionalTdbDatastore colanderDatastore) {
        super(new SemanticRequestResponseManager(colanderDatastore));

        this.miscDatastore = miscDatastore;
        this.subscriptionsDatastore = subscriptionsDatastore;

        this.miscDatastore.open();
        this.subscriptionsDatastore.open();

        CacheBuilder<Object, Object> cacheBuilder =
                CacheBuilder.newBuilder()
                        .softValues()
                        .maximumSize(
                                EventCloudProperties.PEER_STUBS_CACHE_MAXIMUM_SIZE.getValue());

        if (EventCloudProperties.RECORD_STATS_PEER_STUBS_CACHE.getValue()) {
            cacheBuilder.recordStats();
        }

        this.peerStubsCache =
                cacheBuilder.build(new CacheLoader<String, SemanticPeer>() {
                    @Override
                    public SemanticPeer load(String peerUrl) throws Exception {
                        return PAActiveObject.lookupActive(
                                SemanticPeer.class, peerUrl);
                    }
                });

        cacheBuilder =
                CacheBuilder.newBuilder()
                        .softValues()
                        .maximumSize(
                                EventCloudProperties.SUBSCRIPTIONS_CACHE_MAXIMUM_SIZE.getValue());

        if (EventCloudProperties.RECORD_STATS_SUBSCRIPTIONS_CACHE.getValue()) {
            cacheBuilder.recordStats();
        }

        this.subscriptionsCache =
                cacheBuilder.build(new CacheLoader<SubscriptionId, Subscription>() {
                    @Override
                    public Subscription load(SubscriptionId key)
                            throws SubscriptionNotFoundException {
                        Subscription subscription =
                                Subscription.parseFrom(
                                        subscriptionsDatastore, key);

                        if (subscription == null) {
                            throw new SubscriptionNotFoundException();
                        }

                        return subscription;
                    }
                });

        this.subscriberConnectionFailures =
                new MapMaker().softValues().makeMap();

        if (EventCloudProperties.isSbce2PubSubAlgorithmUsed()
                || EventCloudProperties.isSbce3PubSubAlgorithmUsed()) {
            this.ephemeralSubscriptionsGarbageColletor =
                    Executors.newSingleThreadScheduledExecutor();
            this.ephemeralSubscriptionsGarbageColletor.scheduleWithFixedDelay(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                SemanticCanOverlay.this.removeOutdatedEphemeralSubscriptions();
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                    },
                    EventCloudProperties.EPHEMERAL_SUBSCRIPTIONS_GC_TIMEOUT.getValue(),
                    EventCloudProperties.EPHEMERAL_SUBSCRIPTIONS_GC_TIMEOUT.getValue(),
                    TimeUnit.MINUTES);
        }

        this.indexSubscriptionRequestDelayer =
                new IndexSubscriptionRequestDelayer(this);
        this.publishQuadrupleRequestDelayer =
                new PublishQuadrupleRequestDelayer(this);

        if (EventCloudProperties.isSbce3PubSubAlgorithmUsed()) {
            this.publishCompoundEventRequestDelayer =
                    new PublishCompoundEventRequestDelayer(this);
        } else {
            this.publishCompoundEventRequestDelayer = null;
        }
    }

    private void removeOutdatedEphemeralSubscriptions() {
        TransactionalDatasetGraph txnGraph =
                this.subscriptionsDatastore.begin(AccessMode.READ_ONLY);

        String findSubscriptionsSparql =
                "SELECT ?g ?sId WHERE { GRAPH ?g { ?sId <"
                        + PublishSubscribeConstants.EPHEMERAL_SUBSCRIPTION_INDEXATION_DATETIME_PROPERTY
                        + "> ?itime } FILTER( "
                        + System.currentTimeMillis()
                        + " - ?itime > "
                        + EventCloudProperties.EPHEMERAL_SUBSCRIPTION_EXPIRATION_TIME.getValue()
                        + " ) }";

        QueryExecution qexec =
                QueryExecutionFactory.create(
                        findSubscriptionsSparql,
                        txnGraph.getUnderlyingDataset());

        List<Node[]> solutions = null;
        try {
            ResultSet r = qexec.execSelect();

            solutions = new ArrayList<Node[]>();

            while (r.hasNext()) {
                QuerySolution s = r.next();

                Node graph = s.get("g").asNode();
                Node sId = s.get("sId").asNode();
                solutions.add(new Node[] {graph, sId});
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }

        txnGraph = this.subscriptionsDatastore.begin(AccessMode.WRITE);

        try {
            for (Node[] solution : solutions) {
                Node graph = solution[0];
                Node sId = solution[1];

                txnGraph.delete(
                        graph,
                        sId,
                        PublishSubscribeConstants.EPHEMERAL_SUBSCRIPTION_SUBSCRIBER_NODE,
                        Node.ANY);
                txnGraph.delete(
                        graph,
                        sId,
                        PublishSubscribeConstants.EPHEMERAL_SUBSCRIPTION_INDEXATION_DATETIME_NODE,
                        Node.ANY);
            }

            txnGraph.commit();
        } catch (Exception e) {
            e.printStackTrace();
            txnGraph.abort();
        } finally {
            txnGraph.end();
        }

        if (log.isDebugEnabled()) {
            StringBuilder msg = new StringBuilder();
            msg.append("New ephemeral garbage collection performed, ");
            msg.append(solutions.size());
            msg.append(" quadruple(s) removed");

            log.debug(msg.toString());
        }
    }

    /**
     * Indicates whether this overlay is connected to a social filter or not.
     * 
     * @return true if this overlay is connected to a social filter, false
     *         otherwise.
     */
    public boolean hasSocialFilter() {
        return this.socialFilter != null;
    }

    public LoadingCache<SubscriptionId, Subscription> getSubscriptionsCache() {
        return this.subscriptionsCache;
    }

    /**
     * Returns the {@link IndexSubscriptionRequestDelayer} delayer instance.
     * 
     * @return the {@link IndexSubscriptionRequestDelayer} delayer instance.
     */
    public IndexSubscriptionRequestDelayer getIndexSubscriptionRequestDelayer() {
        return this.indexSubscriptionRequestDelayer;
    }

    /**
     * Returns the {@link PublishQuadrupleRequest} delayer instance.
     * 
     * @return the {@link PublishQuadrupleRequest} delayer instance.
     */
    public PublishQuadrupleRequestDelayer getPublishQuadrupleRequestDelayer() {
        return this.publishQuadrupleRequestDelayer;
    }

    /**
     * Returns the {@link PublishCompoundEventRequest} delayer instance.
     * 
     * @return the {@link PublishCompoundEventRequest} delayer instance.
     */
    public PublishCompoundEventRequestDelayer getPublishCompoundEventRequestDelayer() {
        return this.publishCompoundEventRequestDelayer;
    }

    /**
     * Returns the social filter if any.
     * 
     * @return the social filter if any, null otherwise.
     */
    public RelationshipStrengthEngineManager getSocialFilter() {
        return this.socialFilter;
    }

    /**
     * Sets the social filter.
     * 
     * @param socialFilter
     *            the social filter.
     */
    public void setSocialFilter(RelationshipStrengthEngineManager socialFilter) {
        this.socialFilter = socialFilter;
    }

    /**
     * Finds the subscription associated to the specified subscription
     * {@code id} from the cache. When no subscription is found in the cache, a
     * lookup is performed in the datastore. If no subscription is found for the
     * specified {@code id}, a {@code null} value is returned.
     * 
     * @param id
     *            the subscription identifier used to lookup the subscription.
     * 
     * @return the subscription found or {@code null}.
     */
    public final Subscription findSubscription(SubscriptionId id) {
        try {
            return this.subscriptionsCache.get(id);
        } catch (ExecutionException e) {
            // subscription not found in the cache nor in the datastore
            return null;
        }
    }

    public final Subscription findSubscription(final TransactionalDatasetGraph dataset,
                                               final SubscriptionId id) {
        try {
            return this.subscriptionsCache.get(
                    id, new Callable<Subscription>() {
                        @Override
                        public Subscription call() throws Exception {
                            return Subscription.parseFrom(dataset, id);
                        };
                    });
        } catch (ExecutionException e) {
            throw new IllegalStateException("Subscription " + id
                    + " not found in cache and datastore");
        }
    }

    /**
     * Finds the peer stub associated to the specified {@code peerUrl} from the
     * cache. When no stub is found in the cache, the stub is created on the
     * fly. If an error occurs during the creation of the stub, a {@code null}
     * value is returned.
     * 
     * @param peerUrl
     *            the peer URL used to lookup the stub.
     * 
     * @return the subscription found or {@code null}.
     */
    public final SemanticPeer findPeerStub(String peerUrl) {
        try {
            return this.peerStubsCache.get(peerUrl);
        } catch (ExecutionException e) {
            throw new IllegalStateException(
                    "Stub associated to URL "
                            + peerUrl
                            + " not found in cache and the construction of the remote reference failed");
        }
    }

    /**
     * Stores the specified {@code subscription} in cache and the local
     * persistent datastore.
     * 
     * @param subscription
     *            the subscription to store.
     */
    public void storeSubscription(Subscription subscription) {
        this.subscriptionsCache.put(subscription.getId(), subscription);

        TransactionalDatasetGraph txnGraph =
                this.subscriptionsDatastore.begin(AccessMode.WRITE);

        try {
            txnGraph.add(subscription.toQuadruples());
            txnGraph.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }
    }

    /**
     * Deletes from the local datastore all the quadruples associated to the
     * subscriptions which are related to the subscription identified by
     * {@code originalSubscriptionId}.
     * 
     * @param originalSubscriptionId
     *            the original subscription id (the subscription identifier
     *            associated to the first subscription which has not been
     *            rewritten) to use.
     * 
     * @param useBindingNotificationListener
     *            Indicated whether the original subscription id uses a
     *            {@link BindingNotificationListener} or not.
     */
    public void deleteSubscriptions(SubscriptionId originalSubscriptionId,
                                    boolean useBindingNotificationListener) {
        Node oidNode =
                PublishSubscribeUtils.createSubscriptionIdUri(originalSubscriptionId);

        synchronized (this.subscriptionsCache) {
            TransactionalDatasetGraph txnGraph =
                    this.subscriptionsDatastore.begin(AccessMode.WRITE);
            try {
                // deletes potential intermediate results if the original
                // subscription uses a binding notification listener
                if (useBindingNotificationListener) {
                    txnGraph.delete(new QuadruplePattern(
                            Node.ANY,
                            oidNode,
                            PublishSubscribeConstants.QUADRUPLE_MATCHES_SUBSCRIPTION_NODE,
                            Node.ANY));
                }

                txnGraph.delete(new QuadruplePattern(
                        oidNode, Node.ANY, Node.ANY, Node.ANY));

                txnGraph.commit();
            } finally {
                txnGraph.end();
            }

            this.subscriptionsCache.invalidate(originalSubscriptionId);
        }
    }

    public TransactionalTdbDatastore getMiscDatastore() {
        return this.miscDatastore;
    }

    public TransactionalTdbDatastore getSubscriptionsDatastore() {
        return this.subscriptionsDatastore;
    }

    public ConcurrentMap<SubscriptionId, SubscriberConnectionFailure> getSubscriberConnectionFailures() {
        return this.subscriberConnectionFailures;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String dump() {
        StringBuilder result = new StringBuilder(super.dump());

        if (EventCloudProperties.RECORD_STATS_SUBSCRIPTIONS_CACHE.getValue()) {
            result.append("Subscriptions cache:\n  ");
            result.append(this.subscriptionsCache.stats());
            result.append('\n');
        }

        if (EventCloudProperties.RECORD_STATS_PEER_STUBS_CACHE.getValue()) {
            result.append("Peer stubs cache:\n  ");
            result.append(this.peerStubsCache.stats());
            result.append('\n');
        }

        if (EventCloudProperties.RECORD_STATS_SUBSCRIBE_PROXIES_CACHE.getValue()) {
            result.append("Subscribe proxies cache:\n  ");
            result.append(Subscription.SUBSCRIBE_PROXIES_CACHE.stats());
            result.append('\n');
        }

        if (EventCloudProperties.RECORD_STATS_MISC_DATASTORE.getValue()) {
            result.append("Misc datastore stats recorded with ");
            result.append(this.miscDatastore.getStatsRecorder().getClass());
            result.append('\n');
            result.append("Misc datastore size: ");
            result.append(this.miscDatastore.getStatsRecorder().getNbQuads());
            result.append('\n');
        }

        return result.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Zone<SemanticElement> newZone() {
        return new SemanticZone();
    }

    /*
     * DataHandler interface implementation
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void assignDataReceived(Serializable dataReceived) {
        SemanticData semanticDataReceived = ((SemanticData) dataReceived);

        store(this.miscDatastore, semanticDataReceived.getMiscData());
        store(
                this.subscriptionsDatastore,
                semanticDataReceived.getSubscriptions());
    }

    private static void store(TransactionalTdbDatastore datastore,
                              Collection<Quadruple> quadruples) {
        if (quadruples == null || quadruples.size() == 0) {
            return;
        }

        TransactionalDatasetGraph txnGraph = datastore.begin(AccessMode.WRITE);

        try {
            txnGraph.add(quadruples);
            txnGraph.commit();
        } finally {
            txnGraph.end();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SemanticData retrieveAllData() {
        return new SemanticData(
                retrieveAll(this.miscDatastore),
                retrieveAll(this.subscriptionsDatastore));
    }

    private static List<Quadruple> retrieveAll(TransactionalTdbDatastore datastore) {
        TransactionalDatasetGraph txnGraph =
                datastore.begin(AccessMode.READ_ONLY);

        try {
            return Lists.newArrayList(txnGraph.find(QuadruplePattern.ANY));
        } finally {
            txnGraph.end();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void join(Peer landmarkPeer) {
        super.join(landmarkPeer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JoinIntroduceResponseOperation<SemanticElement> handleJoinIntroduceMessage(JoinIntroduceOperation<SemanticElement> msg) {
        JoinIntroduceResponseOperation<SemanticElement> response =
                super.handleJoinIntroduceMessage(msg);

        if (this.miscDatastore.getStatsRecorder() != null) {
            // We have to sync in order to ensure that there is no background
            // threads updating the stats fields otherwise when someone will
            // call the method to know what is the point where the split should
            // be done it may returns a value that is not between the bounds
            // managed by the peer
            this.miscDatastore.getStatsRecorder().sync();
        }

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public SemanticData retrieveDataIn(Object interval) {
        return this.retrieveDataIn((Zone<SemanticElement>) interval, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public SemanticData removeDataIn(Object interval) {
        return this.retrieveDataIn((Zone<SemanticElement>) interval, true);
    }

    private SemanticData retrieveDataIn(Zone<SemanticElement> zone,
                                        boolean remove) {
        List<Quadruple> miscData = this.retrieveMiscDataIn(zone, remove);
        List<Quadruple> subscriptions =
                this.retrieveSubscriptionsIn(zone, remove);

        return new SemanticData(miscData, subscriptions);
    }

    private List<Quadruple> retrieveMiscDataIn(Zone<SemanticElement> zone,
                                               boolean remove) {
        SemanticElement graph, subject, predicate, object;

        List<Quadruple> result = new ArrayList<Quadruple>();

        TransactionalDatasetGraph txnGraph =
                this.miscDatastore.begin(AccessMode.READ_ONLY);

        try {
            QuadrupleIterator it = txnGraph.find(QuadruplePattern.ANY);

            while (it.hasNext()) {
                Quadruple quad = it.next();

                graph = new SemanticElement(quad.getGraph());
                subject = new SemanticElement(quad.getSubject());
                predicate = new SemanticElement(quad.getPredicate());
                object = new SemanticElement(quad.getObject());

                if (graph.compareTo(zone.getLowerBound((byte) 0)) >= 0
                        && graph.compareTo(zone.getUpperBound((byte) 0)) < 0
                        && subject.compareTo(zone.getLowerBound((byte) 1)) >= 0
                        && subject.compareTo(zone.getUpperBound((byte) 1)) < 0
                        && predicate.compareTo(zone.getLowerBound((byte) 2)) >= 0
                        && predicate.compareTo(zone.getUpperBound((byte) 2)) < 0
                        && object.compareTo(zone.getLowerBound((byte) 3)) >= 0
                        && object.compareTo(zone.getUpperBound((byte) 3)) < 0) {
                    result.add(quad);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            txnGraph.end();
        }

        if (remove) {
            delete(this.miscDatastore, result);
        }

        return result;
    }

    private List<Quadruple> retrieveSubscriptionsIn(Zone<SemanticElement> zone,
                                                    boolean remove) {
        TransactionalDatasetGraph txnGraph =
                this.subscriptionsDatastore.begin(AccessMode.READ_ONLY);

        String q = createFindSubscriptionsQuery();

        QueryExecution qexec =
                QueryExecutionFactory.create(
                        QueryFactory.create(q), txnGraph.getUnderlyingDataset());

        List<Node> subscriptionIdsToCopy = new ArrayList<Node>();
        List<Node> subscriptionIdsToDelete = new ArrayList<Node>();

        try {
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                Binding binding = results.nextBinding();
                Node oid = binding.get(Var.alloc("oid"));
                Node g = binding.get(Var.alloc("g"));
                Node s = binding.get(Var.alloc("s"));
                Node p = binding.get(Var.alloc("p"));
                Node o = binding.get(Var.alloc("o"));

                // nodes after prefix removal
                String gwp = SemanticElement.removePrefix(g);
                String swp = SemanticElement.removePrefix(s);
                String pwp = SemanticElement.removePrefix(p);
                String owp = SemanticElement.removePrefix(o);

                boolean gIsVar = isVariable(g);
                boolean sIsVar = isVariable(s);
                boolean pIsVar = isVariable(p);
                boolean oIsVar = isVariable(o);

                // tests whether the nodes associated to the sub subscription
                // used to index the subscription is managed by the zone of the
                // peer that joins. If yes, the subscription has to be copied to
                // the new peer
                if (isSubscriptionManagedByZone(
                        zone, gwp, swp, pwp, owp, gIsVar, sIsVar, pIsVar,
                        oIsVar)) {
                    subscriptionIdsToCopy.add(oid);
                }

                // tests whether the nodes associated to the sub subscription
                // used to index the subscription is managed by the zone of the
                // current peer (i.e. the landmark peer that is joined). If yes,
                // the subscription has to be removed
                if (remove
                        && !isSubscriptionManagedByZone(
                                super.zone, gwp, swp, pwp, owp, gIsVar, sIsVar,
                                pIsVar, oIsVar)) {
                    subscriptionIdsToDelete.add(oid);
                }
            }
        } finally {
            qexec.close();
            txnGraph.end();
        }

        txnGraph = this.subscriptionsDatastore.begin(AccessMode.READ_ONLY);

        List<Quadruple> result = new ArrayList<Quadruple>();

        try {
            QuadrupleIterator it = null;

            // transfers intermediate chunks for each subscription matched and
            // created with a binding notification listener
            for (Node graph : subscriptionIdsToCopy) {
                it = txnGraph.find(graph, Node.ANY, Node.ANY, Node.ANY);

                while (it.hasNext()) {
                    result.add(it.next());
                }

                it =
                        txnGraph.find(
                                Node.ANY,
                                graph,
                                PublishSubscribeConstants.QUADRUPLE_MATCHES_SUBSCRIPTION_NODE,
                                Node.ANY);
                while (it.hasNext()) {
                    result.add(it.next());
                }
            }

            // copy ephemeral subscriptions
            // TODO: remove those that should no longer be on the peer
            it =
                    txnGraph.find(
                            Node.ANY,
                            Node.ANY,
                            PublishSubscribeConstants.EPHEMERAL_SUBSCRIPTION_SUBSCRIBER_NODE,
                            Node.ANY);

            while (it.hasNext()) {
                result.add(it.next());
            }

            it =
                    txnGraph.find(
                            Node.ANY,
                            Node.ANY,
                            PublishSubscribeConstants.EPHEMERAL_SUBSCRIPTION_INDEXATION_DATETIME_NODE,
                            Node.ANY);

            while (it.hasNext()) {
                result.add(it.next());
            }
        } finally {
            txnGraph.end();
        }

        if (remove) {
            txnGraph = this.subscriptionsDatastore.begin(AccessMode.WRITE);

            try {
                for (Node graph : subscriptionIdsToDelete) {
                    txnGraph.delete(graph, Node.ANY, Node.ANY, Node.ANY);
                    txnGraph.delete(
                            Node.ANY,
                            graph,
                            PublishSubscribeConstants.QUADRUPLE_MATCHES_SUBSCRIPTION_NODE,
                            Node.ANY);
                }
            } finally {
                txnGraph.end();
            }
        }

        return result;
    }

    private static boolean isVariable(Node n) {
        return n.isLiteral()
                && n.getLiteralDatatypeURI().equals(
                        PublishSubscribeConstants.SUBSCRIPTION_VARIABLE_VALUE);
    }

    private static boolean isSubscriptionManagedByZone(Zone<SemanticElement> zone,
                                                       String gwp, String swp,
                                                       String pwp, String owp,
                                                       boolean gIsVar,
                                                       boolean sIsVar,
                                                       boolean pIsVar,
                                                       boolean oIsVar) {

        String graphLowerBound = zone.getLowerBound((byte) 0).getValue();
        String graphUpperBound = zone.getUpperBound((byte) 0).getValue();
        String subjectLowerBound = zone.getLowerBound((byte) 1).getValue();
        String subjectUpperBound = zone.getUpperBound((byte) 1).getValue();
        String predicateLowerBound = zone.getLowerBound((byte) 2).getValue();
        String predicateUpperBound = zone.getUpperBound((byte) 2).getValue();
        String objectLowerBound = zone.getLowerBound((byte) 3).getValue();
        String objectUpperBound = zone.getUpperBound((byte) 3).getValue();

        return (gIsVar || ((gwp.compareTo(graphLowerBound) >= 0) && (gwp.compareTo(graphUpperBound) < 0)))
                && (sIsVar || ((swp.compareTo(subjectLowerBound) >= 0) && (swp.compareTo(subjectUpperBound) < 0)))
                && (pIsVar || ((pwp.compareTo(predicateLowerBound) >= 0) && (pwp.compareTo(predicateUpperBound) < 0)))
                && (oIsVar || ((owp.compareTo(objectLowerBound) >= 0) && (owp.compareTo(objectUpperBound) < 0)));
    }

    private static void delete(TransactionalTdbDatastore datastore,
                               Collection<Quadruple> quadruples) {
        TransactionalDatasetGraph txnGraph = datastore.begin(AccessMode.WRITE);
        try {
            for (Quadruple q : quadruples) {
                txnGraph.delete(q);
            }
            txnGraph.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }
    }

    /**
     * Creates an returns a SPARQL query that finds the RDF terms of the sub
     * subcription used to index each subscription.
     * 
     * @return a SPARQL query that finds the RDF terms of the sub subcription
     *         used to index each subscription.
     */
    private static String createFindSubscriptionsQuery() {
        char[] vars = {'g', 's', 'p', 'o'};

        StringBuilder result = new StringBuilder();
        result.append("PREFIX ec: <");
        result.append(EventCloudProperties.FILTER_FUNCTIONS_NS.getValue());
        result.append(">\n");
        result.append("SELECT ?oid ?g ?s ?p ?o WHERE {\n  GRAPH ?oid {\n");
        result.append("    ?oid <");
        result.append(PublishSubscribeConstants.SUBSCRIPTION_INDEXED_WITH_PROPERTY);
        result.append("> ?iref .\n");
        result.append("    ?ssid <");
        result.append(PublishSubscribeConstants.SUBSUBSCRIPTION_ID_PROPERTY);
        result.append("> ?iref .\n");

        for (int i = 0; i < vars.length; i++) {
            result.append("    ?ssid <");
            result.append(PublishSubscribeConstants.SUBSUBSCRIPTION_NS);
            result.append(vars[i]);
            result.append("> ?");
            result.append(vars[i]);
            result.append(" .\n");
        }

        result.append("  }\n\n}");

        return result.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HomogenousPair<? extends Zone<SemanticElement>> splitZones(byte dimension) {
        if (!EventCloudProperties.STATIC_LOAD_BALANCING.getValue()
                || (EventCloudProperties.STATIC_LOAD_BALANCING.getValue() && this.miscDatastore.getStatsRecorder() == null)) {
            return super.splitZones(dimension);
        } else {
            SemanticElement estimatedMiddle =
                    this.miscDatastore.getStatsRecorder()
                            .computeSplitEstimation(dimension);

            // no estimation can performed because no quadruple has
            // been recorded, thus we apply the default split method
            if (estimatedMiddle == null) {
                return super.splitZones(dimension);
            }

            try {
                Coordinate<SemanticElement> lowerBoundCopy =
                        super.zone.getLowerBound().clone();
                Coordinate<SemanticElement> upperBoundCopy =
                        super.zone.getUpperBound().clone();

                lowerBoundCopy.setElement(dimension, estimatedMiddle);
                upperBoundCopy.setElement(dimension, estimatedMiddle);

                return HomogenousPair.createHomogenous(
                        new SemanticZone(
                                super.zone.getLowerBound(), upperBoundCopy),
                        new SemanticZone(
                                lowerBoundCopy, super.zone.getUpperBound()));
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        super.close();

        if (EventCloudProperties.isSbce2PubSubAlgorithmUsed()
                || EventCloudProperties.isSbce3PubSubAlgorithmUsed()) {
            this.ephemeralSubscriptionsGarbageColletor.shutdown();

            try {
                this.ephemeralSubscriptionsGarbageColletor.awaitTermination(
                        2, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.indexSubscriptionRequestDelayer.close();
        this.publishQuadrupleRequestDelayer.close();

        if (EventCloudProperties.isSbce3PubSubAlgorithmUsed()) {
            this.publishCompoundEventRequestDelayer.close();
        }

        this.miscDatastore.close();
        this.subscriptionsDatastore.close();
    }

    private static final class SubscriptionNotFoundException extends Exception {

        private static final long serialVersionUID = 140L;

        public SubscriptionNotFoundException() {
            super();
        }

    }

}
