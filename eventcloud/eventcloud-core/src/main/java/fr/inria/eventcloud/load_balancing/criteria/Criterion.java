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

import java.io.Serializable;

import com.google.common.collect.Range;

import fr.inria.eventcloud.load_balancing.LoadEvaluation;
import fr.inria.eventcloud.load_balancing.balancer.LoadBalancer;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Class defining common fields and methods to imbalance criteria.
 * 
 * @author lpellegr
 */
public abstract class Criterion implements Serializable {

    private static final long serialVersionUID = 160L;

    public int index;

    private final String name;

    private final Range<Double> domain;

    private final double warmupThreshold;

    private final double emergencyThreshold;

    private final LoadBalancer loadBalancer;

    public Criterion(String name, LoadBalancer loadBalancer,
            Range<Double> domain) {
        this(name, loadBalancer, domain, domain.upperEndpoint(),
                domain.lowerEndpoint());
    }

    public Criterion(String name, LoadBalancer loadBalancer,
            Range<Double> domain, double warmupThreshold,
            double emergencyThreshold) {
        this.name = name;
        this.domain = domain;

        this.warmupThreshold = warmupThreshold;
        this.emergencyThreshold = emergencyThreshold;

        this.loadBalancer = loadBalancer;
    }

    public abstract double getLoad(SemanticCanOverlay overlay);

    public void balanceOverload(LoadEvaluation loadEstimate,
                                SemanticCanOverlay overlay) {
        this.loadBalancer.balanceOverload(loadEstimate, overlay);
    }

    public void balanceUnderload(LoadEvaluation loadEstimate,
                                 SemanticCanOverlay overlay) {
        this.loadBalancer.balanceUnderload(loadEstimate, overlay);
    }

    public String getName() {
        return this.name;
    }

    public Range<Double> getDomain() {
        return this.domain;
    }

    public double getEmergencyThreshold() {
        return this.emergencyThreshold;
    }

    public double getWarmupThreshold() {
        return this.warmupThreshold;
    }

    public double normalize(double value) {
        return value / this.domain.upperEndpoint();
    }

}
