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
package fr.inria.eventcloud.operations.can;

import java.util.List;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.datastore.stats.StatsRecorder;

/**
 * Syntactic sugars for sending common operations.
 * 
 * @author lpellegr
 */
public final class Operations {

    public static List<Quadruple> findQuadruplesOperation(Peer peer,
                                                          QuadruplePattern quadruplePattern) {
        return findQuadruplesOperation(peer, quadruplePattern, false);
    }

    public static List<Quadruple> findQuadruplesOperation(Peer peer,
                                                          QuadruplePattern quadruplePattern,
                                                          boolean useSubscriptionsDatastore) {
        return ((FindQuadruplesResponseOperation) PAFuture.getFutureValue(peer.receive(new FindQuadruplesOperation(
                quadruplePattern, useSubscriptionsDatastore)))).getQuadruples();
    }

    public static StatsRecorder getStatsRecorder(Peer p) {
        return ((GetStatsRecordeResponseOperation) PAFuture.getFutureValue(p.receive(new GetStatsRecorderOperation()))).getStatsRecorder();
    }

}
