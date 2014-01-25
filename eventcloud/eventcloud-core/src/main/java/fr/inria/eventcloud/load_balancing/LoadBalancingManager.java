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

import fr.inria.eventcloud.load_balancing.services.LoadBalancingService;

/**
 * Entity in charge of managing load balancing features. All the job is
 * delegated to the {@link LoadBalancingService} specified with the constructor.
 * 
 * @author lpellegr
 * 
 * @see LoadBalancingService
 */
public class LoadBalancingManager {

    private final LoadBalancingService loadBalancingService;

    public LoadBalancingManager(LoadBalancingService loadBalancingService) {
        this.loadBalancingService = loadBalancingService;
    }

    public void start() {
        this.loadBalancingService.startAsync();
    }

    public void stop() {
        this.loadBalancingService.stopAsync();
    }

    public boolean isRunning() {
        return this.loadBalancingService.isRunning();
    }

    public LoadBalancingService getLoadBalancingService() {
        return this.loadBalancingService;
    }

}
