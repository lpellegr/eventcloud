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
package fr.inria.eventcloud.load_balancing.balancer;

import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.PeerNotActivatedException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.deployment.EventCloudComponentsManager;
import fr.inria.eventcloud.load_balancing.LoadEvaluation;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Balancer that simply balances load by allocating a new peer and forcing it to
 * join the overloaded one.
 * 
 * @author lpellegr
 */
public class PeerAllocatorBalancer implements LoadBalancer {

    private static final long serialVersionUID = 160L;

    private static final Logger log =
            LoggerFactory.getLogger(PeerAllocatorBalancer.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void balanceOverload(LoadEvaluation loadEstimate,
                                SemanticCanOverlay overlay) {
        boolean isTraceEnabled = log.isTraceEnabled();

        long startTime = 0;
        long allocationTime = 0;
        long joinTime = 0;

        if (isTraceEnabled) {
            startTime = System.currentTimeMillis();
        }

        Peer newPeer = this.allocatePeer(overlay);

        if (isTraceEnabled) {
            allocationTime = System.currentTimeMillis() - startTime;
        }

        if (newPeer != null) {
            if (isTraceEnabled) {
                startTime = System.currentTimeMillis();
            }

            try {
                // makes the a new peer join the current one that is overloaded
                newPeer.join(overlay.getStub());
            } catch (NetworkAlreadyJoinedException e) {
                e.printStackTrace();
            } catch (PeerNotActivatedException e) {
                e.printStackTrace();
            }

            if (isTraceEnabled) {
                joinTime = System.currentTimeMillis() - startTime;
                log.trace(
                        "Peer allocated in {} ms and load balancer with a join in {} ms",
                        allocationTime, joinTime);
            }
        } else {
            // all preallocated peers have been borrowed, stop balancing
            log.trace("Peer components pool empty, no balancing performed");
        }

    }

    protected Peer allocatePeer(SemanticCanOverlay overlay) {
        EventCloudComponentsManager componentsManager =
                overlay.getLoadBalancingManager()
                        .getLoadBalancingService()
                        .getConfiguration()
                        .getEventCloudComponentsManager();

        if (componentsManager.isPeerComponentPoolEmpty()) {
            return null;
        } else {
            return componentsManager.getPeer(
                    overlay.getDeploymentConfiguration(),
                    overlay.getOverlayProvider());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void balanceUnderload(LoadEvaluation loadEstimate,
                                 SemanticCanOverlay overlay) {

        // to be defined if required
    }

}
