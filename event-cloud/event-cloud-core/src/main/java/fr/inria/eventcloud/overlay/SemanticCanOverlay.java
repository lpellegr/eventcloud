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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.openjena.riot.out.NodeFmtLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;

import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.QuadrupleIterator;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.reasoner.SparqlColander;

/**
 * This class is a specialized version of {@link CanOverlay} for semantic data.
 * 
 * @author lpellegr
 */
public class SemanticCanOverlay extends CanOverlay {

    private static final Logger log =
            LoggerFactory.getLogger(SemanticCanOverlay.class);

    private final LoadingCache<String, SemanticPeer> peerStubsCache;

    private final LoadingCache<SubscriptionId, Subscription> subscriptionsCache;

    /**
     * Constructs a new overlay with the specified {@code dataHandler} and
     * {@code requestResponseManager}.
     * 
     * @param peerDatastore
     *            the datastore instance to set for storing semantic data.
     * 
     * @param colanderDatastore
     *            the datastore instance to set for filtering sparql answers
     *            from a {@link SparqlColander}.
     */
    public SemanticCanOverlay(TransactionalTdbDatastore peerDatastore,
            TransactionalTdbDatastore colanderDatastore) {
        super(new SemanticRequestResponseManager(colanderDatastore),
                peerDatastore);

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
                                        (TransactionalTdbDatastore) SemanticCanOverlay.this.datastore,
                                        key);
                            }
                        });
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
                ((TransactionalTdbDatastore) super.datastore).begin(AccessMode.WRITE);

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
            TransactionalTdbDatastore datastore =
                    (TransactionalTdbDatastore) super.datastore;

            TransactionalDatasetGraph txnGraph =
                    datastore.begin(AccessMode.WRITE);
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

}
