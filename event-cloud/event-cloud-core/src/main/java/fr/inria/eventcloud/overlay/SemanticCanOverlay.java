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
package fr.inria.eventcloud.overlay;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.AccessMode;
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

    public final ExecutorService datastoreThreadPool;

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

        this.datastoreThreadPool = Executors.newFixedThreadPool(10);

        this.peerStubsCache =
                CacheBuilder.newBuilder()
                        .softValues()
                        .maximumSize(
                                EventCloudProperties.PEER_STUBS_CACHE_MAXIMUM_SIZE.getValue())
                        .build(new CacheLoader<String, SemanticPeer>() {
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
                            public Subscription load(SubscriptionId key) {
                                return Subscription.parseFrom(
                                        (TransactionalTdbDatastore) datastore,
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
                    "Error while creating stub from the cache for url: {}",
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
        txnGraph.add(subscription.toQuadruples());
        txnGraph.commit();
        txnGraph.close();
    }

    /**
     * Deletes all the subscription quadruples associated to the specified
     * {@code originalSubscriptionId} from the local persistent datastore.
     * 
     * @param originalSubscriptionId
     *            the original subscription id (the subscription identifier
     *            associated to the first subscription which has not been
     *            rewritten) to use.
     */
    public synchronized void deleteSubscription(SubscriptionId originalSubscriptionId) {
        this.subscriptionsCache.invalidate(originalSubscriptionId);

        TransactionalTdbDatastore datastore =
                (TransactionalTdbDatastore) super.datastore;
        List<SubscriptionId> subscriptionIds =
                PublishSubscribeUtils.findSubscriptionIds(
                        datastore, originalSubscriptionId);

        for (SubscriptionId id : subscriptionIds) {
            PublishSubscribeUtils.deleteSubscription(datastore, id);
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
