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
package fr.inria.eventcloud.overlay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.openjena.riot.out.NodeFmtLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soceda.socialfilter.relationshipstrengthengine.RelationshipStrengthEngineManager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;

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

        this.peerStubsCache =
                CacheBuilder.newBuilder()
                        .softValues()
                        .maximumSize(
                                EventCloudProperties.PEER_STUBS_CACHE_MAXIMUM_SIZE.getValue())
                        .build(new CacheLoader<String, SemanticPeer>() {
                            @Override
                            public SemanticPeer load(String peerUrl)
                                    throws Exception {
                                return PAActiveObject.lookupActive(
                                        SemanticPeer.class, peerUrl);
                            }
                        });

        this.subscriptionsCache =
                CacheBuilder.newBuilder()
                        .softValues()
                        .maximumSize(
                                EventCloudProperties.SUBSCRIPTIONS_CACHE_MAXIMUM_SIZE.getValue())
                        .build(new CacheLoader<SubscriptionId, Subscription>() {
                            @Override
                            public Subscription load(SubscriptionId key) {
                                return Subscription.parseFrom(
                                        subscriptionsDatastore, key);
                            }
                        });

        this.subscriberConnectionFailures =
                new MapMaker().softValues().makeMap();

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
     * {@code id} from the cache. When no subscription is found into the cache,
     * a lookup is performed in the datastore. If no subscription is found for
     * the specified {@code id}, a {@code null} value is returned.
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
            log.error(
                    "Error while retrieving subscription {} from the cache",
                    id, e);
            return null;
        }
    }

    /**
     * Finds the peer stub associated to the specified {@code peerUrl} from the
     * cache. When no stub is found into the cache, the stub is created on the
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
            log.error(
                    "Error while retrieving stub from the cache for url: {}",
                    peerUrl, e);
            return null;
        }
    }

    /**
     * Stores the specified {@code subscription} into the cache and the local
     * persistent datastore.
     * 
     * @param subscription
     *            the subscription to store.
     */
    public void storeSubscription(Subscription subscription) {
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
        Node osidNode =
                PublishSubscribeUtils.createSubscriptionIdUri(originalSubscriptionId);

        StringBuilder deleteSubsubscriptionsQuery = new StringBuilder();
        deleteSubsubscriptionsQuery.append("DELETE WHERE { GRAPH ");
        deleteSubsubscriptionsQuery.append(NodeFmtLib.str(PublishSubscribeConstants.SUBSCRIPTION_NS_NODE));
        deleteSubsubscriptionsQuery.append(" { ?subscriptionIdUri ");
        deleteSubsubscriptionsQuery.append(NodeFmtLib.str(PublishSubscribeConstants.SUBSCRIPTION_ORIGINAL_ID_NODE));
        deleteSubsubscriptionsQuery.append(' ');
        deleteSubsubscriptionsQuery.append(NodeFmtLib.str(osidNode));
        deleteSubsubscriptionsQuery.append(" . ?subscriptionIdUri ");
        deleteSubsubscriptionsQuery.append(' ');
        deleteSubsubscriptionsQuery.append(NodeFmtLib.str(PublishSubscribeConstants.SUBSCRIPTION_HAS_SUBSUBSCRIPTION_NODE));
        deleteSubsubscriptionsQuery.append(" ?subSubscriptionIdUri . ?subSubscriptionIdUri ?p ?o }}");

        synchronized (this.subscriptionsCache) {
            TransactionalDatasetGraph txnGraph =
                    this.subscriptionsDatastore.begin(AccessMode.WRITE);
            try {
                // deletes potential intermediate results if the original
                // subscription uses a binding notification listener
                if (useBindingNotificationListener) {
                    txnGraph.delete(new QuadruplePattern(
                            Node.ANY,
                            osidNode,
                            PublishSubscribeConstants.QUADRUPLE_MATCHES_SUBSCRIPTION_NODE,
                            Node.ANY));
                }

                // retrieves the uri of subscriptions which have the specified
                // original id
                List<Node> subscriptionsIdUris = new ArrayList<Node>();

                QuadrupleIterator result =
                        txnGraph.find(new QuadruplePattern(
                                PublishSubscribeConstants.SUBSCRIPTION_NS_NODE,
                                Node.ANY,
                                PublishSubscribeConstants.SUBSCRIPTION_ORIGINAL_ID_NODE,
                                osidNode));
                while (result.hasNext()) {
                    subscriptionsIdUris.add(result.next().getSubject());
                }

                // deletes quadruples related to sub subscriptions indexed with
                // the subscription
                UpdateAction.execute(
                        UpdateFactory.create(deleteSubsubscriptionsQuery.toString()),
                        txnGraph.toDataset());

                // deletes quadruples related to the subscription

                for (Node node : subscriptionsIdUris) {
                    txnGraph.delete(new QuadruplePattern(
                            PublishSubscribeConstants.SUBSCRIPTION_NS_NODE,
                            node, Node.ANY, Node.ANY));
                }

                txnGraph.commit();
            } catch (Exception e) {
                e.printStackTrace();
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
        result.append("Subscriptions cache:\n");
        result.append(this.subscriptionsCache.stats());
        result.append("\nPeer stubs cache:\n");
        result.append(this.peerStubsCache.stats());
        result.append("\nSubscriber proxies cache:\n");
        result.append(Subscription.subscribeProxiesCache.stats());
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

        if (semanticDataReceived.getMiscData() != null) {
            TransactionalDatasetGraph txnGraph =
                    this.miscDatastore.begin(AccessMode.WRITE);

            try {
                txnGraph.add(semanticDataReceived.getMiscData());
                txnGraph.commit();
            } finally {
                txnGraph.end();
            }
        }
        // TODO: add subscriptions support
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SemanticData retrieveAllData() {
        TransactionalDatasetGraph txnGraph =
                this.miscDatastore.begin(AccessMode.READ_ONLY);

        List<Quadruple> data = null;
        try {
            data = Lists.newArrayList(txnGraph.find(QuadruplePattern.ANY));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }

        // TODO: add subscriptions support

        return new SemanticData(data, null);
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
        SemanticElement graph, subject, predicate, object;

        List<Quadruple> result = new ArrayList<Quadruple>();

        TransactionalDatasetGraph txnGraph =
                this.miscDatastore.begin(AccessMode.READ_ONLY);
        try {
            QuadrupleIterator it = txnGraph.find(QuadruplePattern.ANY);

            while (it.hasNext()) {
                Quadruple quad = it.next();

                graph = new SemanticElement(quad.getGraph().toString());
                subject = new SemanticElement(quad.getSubject().toString());
                predicate = new SemanticElement(quad.getPredicate().toString());
                object = new SemanticElement(quad.getObject().toString());

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
            txnGraph = this.miscDatastore.begin(AccessMode.WRITE);
            try {
                for (Quadruple q : result) {
                    txnGraph.delete(q);
                }
                txnGraph.commit();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                txnGraph.end();
            }
        }

        // TODO: add subscriptions support
        return new SemanticData(result, null);
    }

}
