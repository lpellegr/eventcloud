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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.util;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.extensions.p2p.structured.operations.Operation;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.operations.can.FindQuadruplesOperation;
import fr.inria.eventcloud.operations.can.FindQuadruplesResponseOperation;
import fr.inria.eventcloud.overlay.SemanticPeer;

/**
 * This class provides some convenient methods to execute an {@link Operation}
 * on a {@link SemanticPeer} by hiding ProActive mechanisms.
 * <p>
 * All methods are susceptible to thrown a {@link ProActiveRuntimeException} if
 * a communication problem occurs while the operation is dispatched to the
 * remote node.
 * 
 * @author lpellegr
 */
public final class SemanticPeerOperations {

    public static Collection<Quadruple> findQuadruples(SemanticPeer peer,
                                                       QuadruplePattern quadruplePattern) {
        return ((FindQuadruplesResponseOperation) PAFuture.getFutureValue(peer.receiveImmediateService(new FindQuadruplesOperation(
                quadruplePattern)))).getQuadruples();
    }

}