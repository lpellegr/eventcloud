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
package fr.inria.eventcloud.pubsub.solutions;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;

/**
 * Defines a solution that collects intermediate chunks (represented as
 * quadruples) that belong to a same {@link CompoundEvent}.
 * 
 * @author lpellegr
 */
public class QuadruplesSolution extends Solution<Collection<Quadruple>> {

    private int nbQuadruplesExpected;

    private int nbQuadruplesReceived;

    /**
     * 
     * @param chunk
     */
    public QuadruplesSolution(List<Quadruple> chunk) {
        // a set is used to remove potential duplicates
        super(new HashSet<Quadruple>(chunk.size(), 0.9f));
        this.add(chunk);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void merge(Collection<Quadruple> chunk) {
        this.add(chunk);
    }

    private void add(Collection<Quadruple> chunk) {
        for (Quadruple quadruple : chunk) {
            if (this.nbQuadruplesExpected == 0
                    && quadruple.getPredicate().equals(
                            PublishSubscribeConstants.EVENT_NB_QUADRUPLES_NODE)) {
                this.nbQuadruplesExpected =
                        (Integer) quadruple.getObject().getLiteralValue();
            } else {
                if (super.chunks.add(quadruple)) {
                    this.nbQuadruplesReceived++;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReady() {
        return this.nbQuadruplesReceived == this.nbQuadruplesExpected;
    }

}
