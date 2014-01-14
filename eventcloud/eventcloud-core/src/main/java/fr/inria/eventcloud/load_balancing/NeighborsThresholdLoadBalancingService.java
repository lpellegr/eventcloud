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
package fr.inria.eventcloud.load_balancing;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.operations.GenericResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Status;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.load_balancing.configuration.LocalThresholdLoadBalancingConfiguration;
import fr.inria.eventcloud.load_balancing.configuration.NeighborsThresholdLoadBalancingConfiguration;
import fr.inria.eventcloud.operations.can.RetrieveEstimatedNumberOfQuadruplesOperation;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticCoordinate;

/**
 * Load balancing service that forces a new peer from a preallocated pool of
 * peers to join the current one if the ratio between its load and the one of
 * its neighbors is greater than a given threshold.
 * 
 * @author lpellegr
 */
public class NeighborsThresholdLoadBalancingService extends
        LocalThresholdLoadBalancingService {

    private static final Logger log =
            LoggerFactory.getLogger(NeighborsThresholdLoadBalancingService.class);

    public NeighborsThresholdLoadBalancingService(
            final SemanticCanOverlay overlay,
            LocalThresholdLoadBalancingConfiguration loadBalancingConfiguration) {
        super(overlay, loadBalancingConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runOneIteration() throws Exception {
        long nbQuadruples =
                super.overlay.getMiscDatastore()
                        .getStatsRecorder()
                        .getNbQuadruples();

        // runs iteration if the zone of the peer is not being updated
        if (super.overlay.getStatus() != Status.ACTIVATED) {
            return;
        }

        log.trace(
                "Running one iteration on {}, nbMiscData={}",
                super.overlay.getId(), nbQuadruples);

        NeighborsThresholdLoadBalancingConfiguration configuration =
                ((NeighborsThresholdLoadBalancingConfiguration) super.loadBalancingConfiguration);

        // compare the number of quadruples on the current peer with those on
        // the neighbors
        for (byte dimension = 0; dimension < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            for (byte direction = 0; direction < 2; direction++) {
                for (NeighborEntry<SemanticCoordinate> entry : this.overlay.getNeighborTable()
                        .get(dimension, direction)
                        .values()) {
                    @SuppressWarnings("unchecked")
                    long nbQuadruplesOnNeighbor =
                            ((GenericResponseOperation<Long>) PAFuture.getFutureValue(entry.getStub()
                                    .receive(
                                            new RetrieveEstimatedNumberOfQuadruplesOperation()))).getValue();

                    double ratio = nbQuadruples / nbQuadruplesOnNeighbor;

                    if (ratio > configuration.getNeighborsThresholdRatio()) {
                        log.debug(
                                "Threshold reached when comparing with neighbor {}: {}/{} > {}, new peer is joining",
                                entry.getId(), nbQuadruples,
                                nbQuadruplesOnNeighbor,
                                configuration.getNeighborsThresholdRatio());

                        super.makeNewPeerJoin();
                        return;
                    }
                }
            }
        }

        super.makeDecision(nbQuadruples);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String serviceName() {
        return "Neighbors threshold load-balancing service";
    }

}
