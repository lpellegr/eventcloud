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

import com.google.common.collect.Iterators;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;

/**
 * Buffer that keeps separated meta-quadruples and non meta-quadruples.
 * 
 * @author lpellegr
 */
public final class QuadrupleBuffer extends Buffer<Quadruple> {

    private final List<Quadruple> nonMetaQuadruples;

    private final List<Quadruple> metaQuadruples;

    public QuadrupleBuffer(SemanticCanOverlay overlay, int initialCapacity) {
        super(overlay);
        this.nonMetaQuadruples = new ArrayList<Quadruple>(initialCapacity);

        int estimatedSize =
                initialCapacity
                        / EventCloudProperties.AVERAGE_NB_QUADRUPLES_PER_COMPOUND_EVENT.getValue();

        if (estimatedSize == 0) {
            estimatedSize = 1;
        }

        this.metaQuadruples = new ArrayList<Quadruple>(estimatedSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(Quadruple q) {
        if (PublishSubscribeUtils.isMetaQuadruple(q)) {
            this.metaQuadruples.add(q);
        } else {
            this.nonMetaQuadruples.add(q);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        this.metaQuadruples.clear();
        this.nonMetaQuadruples.clear();
    }

    /**
     * Returns meta-quadruples.
     * 
     * @return the metaQuadruples.
     */
    public List<Quadruple> getMetaQuadruples() {
        return this.metaQuadruples;
    }

    /**
     * Returns non meta-quadruples.
     * 
     * @return the nonMetaQuadruples.
     */
    public List<Quadruple> getNonMetaQuadruples() {
        return this.nonMetaQuadruples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return this.metaQuadruples.isEmpty()
                && this.nonMetaQuadruples.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Quadruple> iterator() {
        return Iterators.concat(
                this.metaQuadruples.iterator(),
                this.nonMetaQuadruples.iterator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persist() {
        TransactionalDatasetGraph txnGraph =
                super.overlay.getMiscDatastore().begin(AccessMode.WRITE);

        try {
            // quadruples are stored by using their meta graph value
            for (Quadruple q : this.metaQuadruples) {
                txnGraph.add(
                        q.createMetaGraphNode(), q.getSubject(),
                        q.getPredicate(), q.getObject());
            }

            for (Quadruple q : this.nonMetaQuadruples) {
                txnGraph.add(
                        q.createMetaGraphNode(), q.getSubject(),
                        q.getPredicate(), q.getObject());
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
        return this.nonMetaQuadruples.size() + this.metaQuadruples.size();
    }

}
