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
package fr.inria.eventcloud.load_balancing.services;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.load_balancing.LoadEvaluation;
import fr.inria.eventcloud.load_balancing.LoadReport;
import fr.inria.eventcloud.load_balancing.LoadState;
import fr.inria.eventcloud.load_balancing.configuration.LoadBalancingConfiguration;
import fr.inria.eventcloud.load_balancing.criteria.Criterion;
import fr.inria.eventcloud.load_balancing.gossip.GossipStrategy;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Load balancing service using system load estimation in order to decide
 * whether the peer running the service suffers from imbalance. The estimation
 * is computed by exchanging load information with the help of a
 * {@link GossipStrategy}.
 * 
 * @author lpellegr
 */
public class RelativeLoadBalancingService extends LoadBalancingService {

    private final GossipStrategy<LoadReport> gossiper;

    private final ConcurrentMap<String, LoadReport> loadReports;

    private ScheduledExecutorService gossipService;

    @SuppressWarnings("unchecked")
    public RelativeLoadBalancingService(SemanticCanOverlay overlay,
            LoadBalancingConfiguration loadBalancingConfiguration) {
        super(overlay, loadBalancingConfiguration);

        this.loadReports =
                new ConcurrentHashMap<String, LoadReport>(
                        2 * P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue());

        try {
            this.gossiper =
                    (GossipStrategy<LoadReport>) EventCloudProperties.LOAD_BALANCING_GOSSIP_STRATEGY.getValue()
                            .newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        this.gossipService = Executors.newScheduledThreadPool(1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startUp() throws Exception {
        this.gossipService.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        RelativeLoadBalancingService.this.gossipLoad();
                    }
                },
                EventCloudProperties.LOAD_BALANCING_GOSSIP_PERIOD.getValue(),
                EventCloudProperties.LOAD_BALANCING_GOSSIP_PERIOD.getValue(),
                TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadBalancingIteration() {
        super.balanceLoad();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void shutDown() throws Exception {
        this.gossipService.shutdownNow();
        this.gossipService.awaitTermination(
                2 * EventCloudProperties.LOAD_BALANCING_GOSSIP_PERIOD.getValue(),
                TimeUnit.MILLISECONDS);
    }

    protected void gossipLoad() {
        LoadReport report = this.createLoadReport();

        // TODO: support multiple criteria
        // does not send report is load is null
        // if (!DoubleMath.fuzzyEquals(report.getValues()[0], 0, 0.1)) {
        this.gossiper.push(this.overlay, report);
        // }
    }

    protected LoadReport createLoadReport() {
        Criterion[] criteria = this.configuration.getCriteria();

        double[] loads = new double[criteria.length];

        for (int i = 0; i < criteria.length; i++) {
            loads[i] = criteria[i].getLoad(this.overlay);
        }

        return new LoadReport(this.overlay.getPeerURL(), loads);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LoadEvaluation evaluateLoadState() {
        if (this.loadReports.isEmpty()) {
            // FIXME more than one criterion should be handled
            Criterion c = this.configuration.getCriteria()[0];
            LoadState loadState = LoadState.NORMAL;

            double measurement = c.getLoad(this.overlay);
            double estimate = c.getEmergencyThreshold();

            if (measurement >= estimate) {
                loadState = LoadState.OVERLOADED;
            }

            return new LoadEvaluation(c, loadState, measurement, estimate);
        } else {
            return super.evaluateLoadState();
        }
    }

    public void register(LoadReport loadReport) {
        this.loadReports.put(loadReport.getPeerURL(), loadReport);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLoadEstimate(Criterion c) {
        double sum = 0;
        int count = 0;

        Iterator<LoadReport> it = this.loadReports.values().iterator();

        while (it.hasNext()) {
            sum += it.next().getValues()[c.index];
            count++;
        }

        return sum / count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String serviceName() {
        return "Relative load balancing service";
    }

}
