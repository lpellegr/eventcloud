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

import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.load_balancing.LoadEvaluation;
import fr.inria.eventcloud.load_balancing.LoadState;
import fr.inria.eventcloud.load_balancing.configuration.LoadBalancingConfiguration;
import fr.inria.eventcloud.load_balancing.criteria.Criterion;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Abstract service in charge of performing load balancing periodically.
 * 
 * @author lpellegr
 */
public abstract class LoadBalancingService extends AbstractScheduledService {

    protected static final Logger log =
            LoggerFactory.getLogger(LoadBalancingService.class);

    protected final SemanticCanOverlay overlay;

    protected final LoadBalancingConfiguration configuration;

    public LoadBalancingService(SemanticCanOverlay overlay,
            LoadBalancingConfiguration loadBalancingConfiguration) {
        this.overlay = overlay;
        this.configuration = loadBalancingConfiguration;

        // log.debug("Parameter k1 set to {}", this.configuration.getK1());
        // log.debug("Parameter k2 set to {}", this.configuration.getK2());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runOneIteration() throws Exception {
        // runs iteration if the zone of the peer is not being updated
        if (this.overlay.getStatus() != Status.ACTIVATED) {
            return;
        }

        this.loadBalancingIteration();
    }

    protected void loadBalancingIteration() {
        this.balanceLoad();
    }

    protected void balanceLoad() {
        LoadEvaluation loadEstimate = this.evaluateLoadState();

        if (loadEstimate.loadState != LoadState.NORMAL) {
            log.info(
                    "Peer {} detected as {} ({})", this.overlay.getId(),
                    loadEstimate.loadState, loadEstimate);
        }

        // check whether load balancing is required
        // and fix imbalances according to the type detected
        switch (loadEstimate.loadState) {
            case OVERLOADED:
                loadEstimate.criterion.balanceOverload(
                        loadEstimate, this.overlay);
                break;
            case NORMAL:
                log.trace(
                        "No imbalance detected on peer {}",
                        this.overlay.getId());
                break;
            case UNDERLOADED:
                loadEstimate.criterion.balanceUnderload(
                        loadEstimate, this.overlay);
                break;
        }
    }

    public LoadEvaluation evaluateLoadState() {
        double k1 = this.configuration.getK1();
        double k2 = this.configuration.getK2();

        Criterion[] criteria = this.configuration.getCriteria();

        Criterion imbalanceCriterion = null;
        LoadState loadState = LoadState.NORMAL;

        double measurement = 0;
        double estimate = 0;

        for (Criterion c : criteria) {
            measurement = c.getLoad(this.overlay);
            estimate = this.getLoadEstimate(c);

            if (measurement >= estimate * k1) {
                imbalanceCriterion = c;
                loadState = LoadState.OVERLOADED;
                break;
            }

            if (measurement < estimate * k2) {
                imbalanceCriterion = c;
                loadState = LoadState.UNDERLOADED;
                break;
            }
        }

        return new LoadEvaluation(
                imbalanceCriterion, loadState, measurement, estimate);
    }

    public abstract double getLoadEstimate(Criterion c);

    public LoadBalancingConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Scheduler scheduler() {
        return AbstractScheduledService.Scheduler.newFixedDelaySchedule(
                EventCloudProperties.LOAD_BALANCING_PERIOD.getValue(),
                EventCloudProperties.LOAD_BALANCING_PERIOD.getValue(),
                TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String serviceName() {
        return "Load balancing service";
    }

}
