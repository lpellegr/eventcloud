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

import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;

import fr.inria.eventcloud.load_balancing.configuration.ThresholdLoadBalancingConfiguration;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Load balancing service that forces a new peer from a preallocated pool of
 * peers to join the current one if its load is greater than a given threshold.
 * 
 * @author lpellegr
 */
public class ThresholdLoadBalancingService extends LoadBalancingService {

    private static final Logger log =
            LoggerFactory.getLogger(ThresholdLoadBalancingService.class);

    public ThresholdLoadBalancingService(final SemanticCanOverlay overlay,
            ThresholdLoadBalancingConfiguration loadBalancingConfiguration) {
        super(overlay, loadBalancingConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runOneIteration() throws Exception {
        // runs iteration if the zone of the peer is not being updated
        if (super.overlay.getStatus() != Status.ACTIVATED) {
            return;
        }

        long nbQuadruples =
                super.overlay.getMiscDatastore()
                        .getStatsRecorder()
                        .getNbQuadruples();

        log.trace(
                "Threshold load balancing service is running one iteration on {}, nbMiscData={}",
                super.overlay.getId(), nbQuadruples);

        ThresholdLoadBalancingConfiguration configuration =
                (ThresholdLoadBalancingConfiguration) super.loadBalancingConfiguration;

        if (nbQuadruples > configuration.getMaximumNumberOfQuadruplesPerPeer()) {
            boolean isTraceEnabled = log.isTraceEnabled();
            long startTime = 0;

            if (isTraceEnabled) {
                startTime = System.currentTimeMillis();
            }

            Peer newPeer =
                    super.loadBalancingConfiguration.getEventCloudComponentsManager()
                            .getPeer(
                                    super.overlay.getDeploymentConfiguration(),
                                    super.overlay.getOverlayProvider());

            String timingMsg = "";
            if (isTraceEnabled) {
                timingMsg =
                        " (it took " + (System.currentTimeMillis() - startTime)
                                + " ms to retrieve peer from pool)";
            }

            log.debug(
                    "Treshold reached {} > {}, new peer is joining" + timingMsg,
                    nbQuadruples,
                    configuration.getMaximumNumberOfQuadruplesPerPeer());

            newPeer.join(super.overlay.getStub());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String serviceName() {
        return "Threshold load-balancing service";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Scheduler scheduler() {
        return AbstractScheduledService.Scheduler.newFixedDelaySchedule(
                500, 500, TimeUnit.MILLISECONDS);
    }

}
