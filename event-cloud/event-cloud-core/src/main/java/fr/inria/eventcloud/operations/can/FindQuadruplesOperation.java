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
package fr.inria.eventcloud.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.datastore.PersistentJenaTdbDatastore;

/**
 * Operation used to query the datastore managed by a peer with a call to
 * {@link PersistentTdbDatastore#find(QuadruplePattern)}.
 * 
 * @author lpellegr
 */
public final class FindQuadruplesOperation implements SynchronousOperation {

    private static final long serialVersionUID = 1L;

    private final QuadruplePattern quadruplePattern;

    public FindQuadruplesOperation(QuadruplePattern quadruplePattern) {
        this.quadruplePattern = quadruplePattern;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperation handle(StructuredOverlay overlay) {
        return new FindQuadruplesResponseOperation(
                ((PersistentJenaTdbDatastore) overlay.getDatastore()).find(this.quadruplePattern));
    }

}
