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
package fr.inria.eventcloud.load_balancing.criteria;

import com.google.common.collect.Range;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.load_balancing.balancer.PeerAllocatorBalancer;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Criterion related to the number of quadruples managed.
 * 
 * @author lpellegr
 */
public class QuadrupleCountCriterion extends Criterion {

    private static final long serialVersionUID = 160L;

    public QuadrupleCountCriterion() {
        super(
                "nbQuadruples",
                new PeerAllocatorBalancer(),
                Range.closed(0.0, (double) Long.MAX_VALUE),
                5,
                EventCloudProperties.LOAD_BALANCING_EMERGENCY_THRESHOLD_QUADRUPLE_CRITERION.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLoad(SemanticCanOverlay overlay) {
        return overlay.getMiscDatastore().getStatsRecorder().getNbQuadruples();
    }

}
