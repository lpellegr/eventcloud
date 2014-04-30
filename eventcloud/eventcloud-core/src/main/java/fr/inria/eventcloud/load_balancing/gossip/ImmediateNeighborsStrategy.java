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
package fr.inria.eventcloud.load_balancing.gossip;

import java.util.concurrent.ConcurrentMap;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;

import fr.inria.eventcloud.load_balancing.LoadReport;
import fr.inria.eventcloud.operations.can.RegisterLoadReportOperation;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticCoordinate;

/**
 * Gossip strategy that forwards load reports to immediate neighbors.
 * 
 * @author lpellegr
 */
public class ImmediateNeighborsStrategy implements GossipStrategy<LoadReport> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void push(SemanticCanOverlay overlay, LoadReport rumour) {
        for (byte dimension = 0; dimension < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            for (byte direction = 0; direction < 2; direction++) {
                ConcurrentMap<OverlayId, NeighborEntry<SemanticCoordinate>> neighbors =
                        overlay.getNeighborTable().get(dimension, direction);

                for (NeighborEntry<SemanticCoordinate> entry : neighbors.values()) {
                    entry.getStub().receive(
                            new RegisterLoadReportOperation(rumour));
                }
            }
        }
    }

}
