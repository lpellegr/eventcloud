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

import com.google.common.util.concurrent.AbstractScheduledService;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.load_balancing.configuration.LoadBalancingConfiguration;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * 
 * 
 * @author lpellegr
 */
public abstract class LoadBalancingService extends AbstractScheduledService {

    protected final SemanticCanOverlay overlay;

    protected final LoadBalancingConfiguration loadBalancingConfiguration;

    public LoadBalancingService(SemanticCanOverlay overlay,
            LoadBalancingConfiguration loadBalancingConfiguration) {
        this.overlay = overlay;
        this.loadBalancingConfiguration = loadBalancingConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String serviceName() {
        return "Load balancing service";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Scheduler scheduler() {
        return AbstractScheduledService.Scheduler.newFixedDelaySchedule(
                EventCloudProperties.LOAD_BALANCING_PROBING_TIMEOUT.getValue(),
                EventCloudProperties.LOAD_BALANCING_PROBING_TIMEOUT.getValue(),
                TimeUnit.MILLISECONDS);
    }

}
