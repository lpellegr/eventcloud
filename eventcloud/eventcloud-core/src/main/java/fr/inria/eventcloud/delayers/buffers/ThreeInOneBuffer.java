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

import java.util.Iterator;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.pubsub.Subscription;

/**
 * 
 * 
 * @author lpellegr
 */
public class ThreeInOneBuffer extends Buffer<Object> {

    private final QuadrupleBuffer quadrupleBuffer;

    private final SubscriptionBuffer subscriptionBuffer;

    private final CompoundEventBuffer compoundEventBuffer;

    public ThreeInOneBuffer(SemanticCanOverlay overlay, int initialCapacity) {
        super(overlay);
        this.quadrupleBuffer = new QuadrupleBuffer(overlay, initialCapacity);
        this.subscriptionBuffer =
                new SubscriptionBuffer(overlay, initialCapacity);

        if (EventCloudProperties.isSbce3PubSubAlgorithmUsed()) {
            this.compoundEventBuffer =
                    new CompoundEventBuffer(overlay, initialCapacity);
        } else {
            this.compoundEventBuffer = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(Object obj) {
        if (obj instanceof Quadruple) {
            this.add((Quadruple) obj);
        } else if (obj instanceof Subscription) {
            this.add((Subscription) obj);
        } else if (obj instanceof ExtendedCompoundEvent) {
            this.add((ExtendedCompoundEvent) obj);
        } else {
            throw new IllegalArgumentException("Unknown parameter type: "
                    + obj.getClass());
        }
    }

    public void add(Quadruple q) {
        this.quadrupleBuffer.add(q);
    }

    public void add(Subscription s) {
        this.subscriptionBuffer.add(s);
    }

    public void add(ExtendedCompoundEvent ce) {
        this.compoundEventBuffer.add(ce);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        this.quadrupleBuffer.clear();
        this.subscriptionBuffer.clear();

        if (this.compoundEventBuffer != null) {
            this.compoundEventBuffer.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        boolean result =
                this.quadrupleBuffer.isEmpty()
                        && this.subscriptionBuffer.isEmpty();

        if (this.compoundEventBuffer != null) {
            result &= this.compoundEventBuffer.isEmpty();
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Object> iterator() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persist() {
        if (!this.quadrupleBuffer.isEmpty()) {
            this.quadrupleBuffer.persist();
        }

        if (!this.subscriptionBuffer.isEmpty()) {
            this.subscriptionBuffer.persist();
        }

        if (this.compoundEventBuffer != null
                && !this.compoundEventBuffer.isEmpty()) {
            this.compoundEventBuffer.persist();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        int result =
                this.quadrupleBuffer.size() + this.subscriptionBuffer.size();

        if (this.compoundEventBuffer != null) {
            result += this.compoundEventBuffer.size();
        }

        return result;
    }

    public QuadrupleBuffer getQuadrupleBuffer() {
        return this.quadrupleBuffer;
    }

    public SubscriptionBuffer getSubscriptionBuffer() {
        return this.subscriptionBuffer;
    }

    public CompoundEventBuffer getCompoundEventBuffer() {
        return this.compoundEventBuffer;
    }

}
