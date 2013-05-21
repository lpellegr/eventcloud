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

import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.load_balancing.criteria.Criterion;
import fr.inria.eventcloud.load_balancing.gossip.GossipStrategy;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Entity in charge of managing load-balancing features.
 * 
 * @author lpellegr
 */
public class LoadBalancingManager {

    private static final Logger log =
            LoggerFactory.getLogger(LoadBalancingManager.class);

    private static final int NB_MAX_ENTRIES =
            EventCloudProperties.LOAD_BALANCING_HISTORY_TIME_WINDOW.getValue()
                    / EventCloudProperties.LOAD_BALANCING_PROBING_TIMEOUT.getValue();

    private final Criterion[] criteria;

    // sliding window that keeps only the last NB_MAX_ENTRIES reports
    private final Queue<LoadReport> loadReportsReceived;

    private final LoadBalancingService scheduledService;

    private final GossipStrategy<LoadReport> gossiper;

    private final SemanticCanOverlay overlay;

    private enum LoadState {
        UNDERLOADED, NORMAL, OVERLOADED
    };

    @SuppressWarnings("unchecked")
    public LoadBalancingManager(final SemanticCanOverlay overlay,
            Criterion[] criteria) {
        this.criteria = criteria;

        try {
            this.gossiper =
                    (GossipStrategy<LoadReport>) EventCloudProperties.LOAD_BALANCING_GOSSIP_STRATEGY.getValue()
                            .newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        this.loadReportsReceived = EvictingQueue.create(NB_MAX_ENTRIES);
        this.overlay = overlay;
        this.scheduledService = new LoadBalancingService();
    }

    public LoadState getLoadState() {
        LoadReport currentLoadReport = new LoadReport(this.criteria);

        double currentLoad =
                currentLoadReport.computeWeightedSum(this.criteria);

        for (int i = 0; i < this.criteria.length; i++) {
            if (currentLoadReport.getValues()[i] >= this.criteria[i].getEmergencyThreshold()) {
                log.info(
                        "Peer {} detected as overloaded due to criteria {} equals or higher than emergency threshold",
                        this.overlay.getId(), this.criteria[i].getName());

                return LoadState.OVERLOADED;
            }
        }

        double overlayLoad = summarize(this.loadReportsReceived, this.criteria);

        double threshold =
                EventCloudProperties.LOAD_BALANCING_IMBALANCE_RATIO.getDefaultValue()
                        * overlayLoad;

        if (currentLoad > threshold) {
            log.info(
                    "Peer {} detected as overloaded because current load is {} times greater than average network load",
                    this.overlay.getId(),
                    EventCloudProperties.LOAD_BALANCING_IMBALANCE_RATIO.getDefaultValue());

            return LoadState.OVERLOADED;
        } else if (currentLoad < threshold) {
            log.info(
                    "Peer {} detected as underloaded because current load is {} times lower than average network load",
                    this.overlay.getId(),
                    EventCloudProperties.LOAD_BALANCING_IMBALANCE_RATIO.getDefaultValue());

            return LoadState.UNDERLOADED;
        } else {
            return LoadState.NORMAL;
        }
    }

    public synchronized void save(LoadReport loadReport) {
        this.loadReportsReceived.add(loadReport);

        // TODO: improve synchronization
        // TODO: check for load state and trigger load balancing operation if
        // required
    }

    public void start() {
        this.scheduledService.start();
    }

    public void stop() {
        this.scheduledService.stop();
    }

    public static double summarize(Queue<LoadReport> reports,
                                   Criterion[] criteria) {
        double result = 0;

        for (LoadReport report : reports) {
            result += report.computeWeightedSum(criteria);
        }

        result /= criteria.length;

        return result;
    }

    public class LoadBalancingService extends AbstractScheduledService {

        private LoadReport lastReport;

        @Override
        protected void runOneIteration() throws Exception {
            long startTime = 0;

            if (log.isTraceEnabled()) {
                startTime = System.currentTimeMillis();
            }

            LoadReport report =
                    new LoadReport(LoadBalancingManager.this.criteria);

            if (this.lastReport == null
                    || report.computeWeightedSum(LoadBalancingManager.this.criteria) > EventCloudProperties.LOAD_BALANCING_GOSSIP_RATIO.getDefaultValue()
                            * this.lastReport.computeWeightedSum(LoadBalancingManager.this.criteria)) {
                log.debug(
                        "Gossiping current load ({}) from peer {}", report,
                        LoadBalancingManager.this.overlay.getId());

                LoadBalancingManager.this.gossiper.push(
                        LoadBalancingManager.this.overlay, report);

                this.lastReport = report;
            }

            if (log.isTraceEnabled()) {
                log.trace("Load balancing service iteration done in "
                        + (System.currentTimeMillis() - startTime) + " ms");
            }
        }

        @Override
        protected String serviceName() {
            return "Load balancing service";
        }

        @Override
        protected Scheduler scheduler() {
            return AbstractScheduledService.Scheduler.newFixedDelaySchedule(
                    EventCloudProperties.LOAD_BALANCING_PROBING_TIMEOUT.getValue(),
                    EventCloudProperties.LOAD_BALANCING_PROBING_TIMEOUT.getValue(),
                    TimeUnit.MILLISECONDS);
        }

    }

}
