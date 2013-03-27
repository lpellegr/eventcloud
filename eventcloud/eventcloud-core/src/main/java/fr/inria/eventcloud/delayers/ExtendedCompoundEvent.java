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

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

/**
 * Stupid abstraction to maintain together a compound event and the index of the
 * quadruple used to publish the compound event.
 * 
 * @author lpellegr
 */
public class ExtendedCompoundEvent {

    public final CompoundEvent compoundEvent;

    public final int indexQuadrupleUsedForIndexing;

    public ExtendedCompoundEvent(CompoundEvent compoundEvent,
            int indexQuadrupleUsedForIndexing) {
        this.compoundEvent = compoundEvent;
        this.indexQuadrupleUsedForIndexing = indexQuadrupleUsedForIndexing;
    }

    public Quadruple getIndexedQuadruple() {
        return this.compoundEvent.getQuadruples().get(
                this.indexQuadrupleUsedForIndexing);
    }

}
