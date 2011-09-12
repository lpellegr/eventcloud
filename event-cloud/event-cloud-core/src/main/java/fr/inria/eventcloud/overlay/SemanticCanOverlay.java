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

import java.util.concurrent.ConcurrentMap;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;

import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.datastore.SemanticDatastore;
import fr.inria.eventcloud.datastore.SynchronizedJenaDatasetGraph;
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

    private final ConcurrentMap<SubscriptionId, Subscription> subscriptionsCache;

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
    public SemanticCanOverlay(SemanticDatastore peerDatastore,
            SemanticDatastore colanderDatastore) {
        super(new SemanticRequestResponseManager(colanderDatastore),
                peerDatastore);
        this.subscriptionsCache =
                new MapMaker().concurrencyLevel(4).softValues().makeMap();
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
    public Subscription findSubscription(SubscriptionId id) {
        Subscription subscription = this.subscriptionsCache.get(id);

        if (subscription == null) {
            log.debug(
                    "SemanticRequestResponseManager.find({}) subscription not in cache, retrieving it from the datastore",
                    id);
            subscription =
                    Subscription.parseFrom(
                            (SynchronizedJenaDatasetGraph) super.datastore, id);

            this.subscriptionsCache.putIfAbsent(
                    subscription.getId(), subscription);
        }

        return subscription;
    }

    /**
     * Stores the specified {@code subscription} into the cache and the local
     * persistent datastore.
     * 
     * @param subscription
     *            the subscription to store.
     */
    public void storeSubscription(Subscription subscription) {
        synchronized (this.subscriptionsCache) {
            this.subscriptionsCache.put(subscription.getId(), subscription);
            ((SemanticDatastore) super.datastore).add(subscription.toQuadruples());
        }
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
    public void deleteSubscription(SubscriptionId originalSubscriptionId) {
        synchronized (this.subscriptionsCache) {
            this.subscriptionsCache.remove(originalSubscriptionId);

            // TODO: a write lock has to be acquired for all the deletes
            for (SubscriptionId id : PublishSubscribeUtils.findSubscriptionIds(
                    (SemanticDatastore) super.datastore, originalSubscriptionId)) {
                PublishSubscribeUtils.deleteSubscription(
                        (SemanticDatastore) super.datastore, id);

            }
        }
    }

}
