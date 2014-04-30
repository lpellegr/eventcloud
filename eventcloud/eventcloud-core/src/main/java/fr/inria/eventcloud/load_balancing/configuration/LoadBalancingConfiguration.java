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
package fr.inria.eventcloud.load_balancing.configuration;

import java.io.Serializable;

import com.google.common.base.Preconditions;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.deployment.EventCloudComponentsManager;
import fr.inria.eventcloud.load_balancing.LoadBalancingStrategy;
import fr.inria.eventcloud.load_balancing.criteria.Criterion;
import fr.inria.eventcloud.load_balancing.services.AbsoluteLoadBalancingService;
import fr.inria.eventcloud.load_balancing.services.LoadBalancingService;
import fr.inria.eventcloud.load_balancing.services.RelativeLoadBalancingService;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Defines values for load balancing parameters.
 * 
 * @author lpellegr
 */
public class LoadBalancingConfiguration implements Serializable {

    private static final long serialVersionUID = 160L;

    private final Criterion[] criteria;

    private final EventCloudComponentsManager componentsManager;

    private final LoadBalancingStrategy strategy;

    private double k1;

    private double k2;

    public LoadBalancingConfiguration(Criterion[] criteria,
            EventCloudComponentsManager eventCloudComponentsManager,
            LoadBalancingStrategy strategy) {
        Preconditions.checkArgument(
                criteria.length > 0,
                "No criteria defined to perform load balancing");

        this.criteria = criteria;
        this.componentsManager = eventCloudComponentsManager;

        for (int i = 0; i < criteria.length; i++) {
            criteria[i].index = i;
        }

        this.k1 = EventCloudProperties.LOAD_BALANCING_PARAMETER_K1.getValue();
        this.k2 = EventCloudProperties.LOAD_BALANCING_PARAMETER_K2.getValue();
        this.strategy = strategy;
    }

    public EventCloudComponentsManager getEventCloudComponentsManager() {
        return this.componentsManager;
    }

    public LoadBalancingService createLoadBalancingService(SemanticCanOverlay overlay) {
        switch (this.strategy) {
            case ABSOLUTE:
                return new AbsoluteLoadBalancingService(overlay, this);
            case RELATIVE:
                return new RelativeLoadBalancingService(overlay, this);
        }

        throw new IllegalStateException("Unknown load balancing strategy: "
                + this.strategy);
    }

    public Criterion[] getCriteria() {
        return this.criteria;
    }

    public double getK1() {
        return this.k1;
    }

    public double getK2() {
        return this.k2;
    }

    public void setK1(double k1) {
        this.k1 = k1;
    }

    public void setK2(double k2) {
        this.k2 = k2;
    }

}
