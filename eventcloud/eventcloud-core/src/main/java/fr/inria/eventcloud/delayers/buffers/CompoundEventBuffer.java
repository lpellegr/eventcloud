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
package fr.inria.eventcloud.delayers.buffers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * 
 * 
 * @author lpellegr
 */
public final class CompoundEventBuffer extends Buffer<ExtendedCompoundEvent> {

    private final Map<ExtendedCompoundEvent, ExtendedCompoundEvent> extendedCompoundEvents;

    public CompoundEventBuffer(SemanticCanOverlay overlay, int initialCapacity) {
        super(overlay);
        this.extendedCompoundEvents =
                new HashMap<ExtendedCompoundEvent, ExtendedCompoundEvent>(
                        initialCapacity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(ExtendedCompoundEvent value) {
        ExtendedCompoundEvent previousValue =
                this.extendedCompoundEvents.put(value, value);

        if (previousValue != null) {
            value.addQuadrupleIndexesUsedForIndexing(previousValue.quadrupleIndexesUsedForIndexing);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        this.extendedCompoundEvents.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return this.extendedCompoundEvents.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<ExtendedCompoundEvent> iterator() {
        return this.extendedCompoundEvents.values().iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persist() {
        TransactionalDatasetGraph txnGraph =
                super.overlay.getMiscDatastore().begin(AccessMode.WRITE);

        try {
            // the quadruple is stored by using its meta graph value
            for (ExtendedCompoundEvent extendedCompoundEvent : this.extendedCompoundEvents.values()) {
                for (Quadruple q : extendedCompoundEvent.getQuadruplesUsedForIndexing()) {
                    txnGraph.add(
                            q.createMetaGraphNode(), q.getSubject(),
                            q.getPredicate(), q.getObject());
                }
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
        return this.extendedCompoundEvents.size();
    }

}
