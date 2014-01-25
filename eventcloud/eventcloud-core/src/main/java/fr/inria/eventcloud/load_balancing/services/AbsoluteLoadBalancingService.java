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
package fr.inria.eventcloud.load_balancing.services;

import fr.inria.eventcloud.load_balancing.configuration.LoadBalancingConfiguration;
import fr.inria.eventcloud.load_balancing.criteria.Criterion;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Load balancing service taking decision about imbalances by using local
 * knowledge only (i.e. predefined threshold values).
 * 
 * @author lpellegr
 */
public class AbsoluteLoadBalancingService extends LoadBalancingService {

    public AbsoluteLoadBalancingService(final SemanticCanOverlay overlay,
            LoadBalancingConfiguration configuration) {
        super(overlay, configuration);

        // TODO: it should be possible to set k1 and k2 per criterion.
        configuration.setK1(1);
        configuration.setK2(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLoadEstimate(Criterion c) {
        return super.configuration.getCriteria()[c.index].getEmergencyThreshold();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String serviceName() {
        return "Absolute load balancing service";
    }

}
