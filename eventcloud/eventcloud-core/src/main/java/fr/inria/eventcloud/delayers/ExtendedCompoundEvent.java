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
package fr.inria.eventcloud.delayers;

import java.util.ArrayList;
import java.util.List;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

/**
 * Simple abstraction to maintain together a compound event and the index of the
 * quadruple used to publish the compound event.
 * 
 * @author lpellegr
 */
public class ExtendedCompoundEvent {

    public final CompoundEvent compoundEvent;

    public final List<Integer> quadrupleIndexesUsedForIndexing;

    public ExtendedCompoundEvent(CompoundEvent compoundEvent,
            int quadrupleIndexUsedForIndexing) {
        this.compoundEvent = compoundEvent;

        this.quadrupleIndexesUsedForIndexing =
                new ArrayList<Integer>(compoundEvent.size());
        this.quadrupleIndexesUsedForIndexing.add(quadrupleIndexUsedForIndexing);
    }

    public void addQuadrupleIndexesUsedForIndexing(List<Integer> quadrupleIndexesUsedForIndexing) {
        this.quadrupleIndexesUsedForIndexing.addAll(quadrupleIndexesUsedForIndexing);
    }

    public Quadruple[] getQuadruplesUsedForIndexing() {
        Quadruple[] result =
                new Quadruple[this.quadrupleIndexesUsedForIndexing.size()];

        for (int i = 0; i < result.length; i++) {
            result[i] =
                    this.compoundEvent.get(this.quadrupleIndexesUsedForIndexing.get(i));
        }

        return result;
    }

    /*
     * hashCode and equals methods use only the compoundEvent field so that
     * when the same events are indexed on the same peer they are filtered out.
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.compoundEvent.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ExtendedCompoundEvent
                && ((ExtendedCompoundEvent) obj).compoundEvent.equals(this.compoundEvent);
    }

}
