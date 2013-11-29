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
package fr.inria.eventcloud.load_balancing.configuration;

import fr.inria.eventcloud.deployment.EventCloudComponentsManager;
import fr.inria.eventcloud.load_balancing.LoadBalancingService;
import fr.inria.eventcloud.load_balancing.NeighborsThresholdLoadBalancingService;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Configuration for {@link NeighborsThresholdLoadBalancingService}.
 * 
 * @author lpellegr
 */
public class NeighborsThresholdLoadBalancingConfiguration extends
        LocalThresholdLoadBalancingConfiguration {

    private static final long serialVersionUID = 160L;

    private final double neighborsThresholdRatio;

    public NeighborsThresholdLoadBalancingConfiguration(
            EventCloudComponentsManager eventCloudComponentsManager,
            int maximumNumberOfQuadruplesPerPeer, double neighborsThresholdRatio) {
        super(eventCloudComponentsManager, maximumNumberOfQuadruplesPerPeer);

        this.neighborsThresholdRatio = neighborsThresholdRatio;
    }

    public double getNeighborsThresholdRatio() {
        return this.neighborsThresholdRatio;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LoadBalancingService createLoadBalancingService(SemanticCanOverlay overlay) {
        return new NeighborsThresholdLoadBalancingService(overlay, this);
    }

}
