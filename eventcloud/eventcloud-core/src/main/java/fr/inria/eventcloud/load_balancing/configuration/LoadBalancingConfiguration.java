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

import java.io.Serializable;

import fr.inria.eventcloud.deployment.EventCloudComponentsManager;
import fr.inria.eventcloud.load_balancing.LoadBalancingService;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * 
 * 
 * @author lpellegr
 */
public abstract class LoadBalancingConfiguration implements Serializable {

    private static final long serialVersionUID = 160L;

    private final EventCloudComponentsManager eventCloudComponentsManager;

    public LoadBalancingConfiguration(
            EventCloudComponentsManager eventCloudComponentsManager) {
        super();
        this.eventCloudComponentsManager = eventCloudComponentsManager;
    }

    public EventCloudComponentsManager getEventCloudComponentsManager() {
        return this.eventCloudComponentsManager;
    }

    public abstract LoadBalancingService createLoadBalancingService(SemanticCanOverlay overlay);

}