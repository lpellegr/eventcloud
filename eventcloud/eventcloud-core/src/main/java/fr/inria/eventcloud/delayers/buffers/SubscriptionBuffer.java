/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.delayers.buffers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.pubsub.Subscription;

/**
 * 
 * 
 * @author lpellegr
 */
public final class SubscriptionBuffer extends Buffer<Subscription> {

    private final List<Subscription> subscriptions;

    public SubscriptionBuffer(SemanticCanOverlay overlay, int initialCapacity) {
        super(overlay);
        this.subscriptions = new ArrayList<Subscription>(initialCapacity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(Subscription value) {
        this.subscriptions.add(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        this.subscriptions.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return this.subscriptions.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Subscription> iterator() {
        return this.subscriptions.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persist() {
        TransactionalDatasetGraph txnGraph =
                super.overlay.getSubscriptionsDatastore().begin(
                        AccessMode.WRITE);

        try {
            for (Subscription s : this.subscriptions) {
                super.overlay.getSubscriptionsCache().put(s.getId(), s);
                txnGraph.add(s.toQuadruples());
            }

            txnGraph.commit();
        } catch (Exception e) {
            e.printStackTrace();
            txnGraph.abort();
        } finally {
            txnGraph.end();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.subscriptions.size();
    }

}
