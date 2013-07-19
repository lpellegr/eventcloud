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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.math.DoubleMath;
import com.google.common.primitives.Doubles;
import com.google.common.util.concurrent.AbstractScheduledService;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.load_balancing.criteria.Criterion;
import fr.inria.eventcloud.load_balancing.gossip.GossipStrategy;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.SemanticPeer;

/**
 * Entity in charge of managing load-balancing features. All the job is made is
 * the internal {@link LoadBalancingService#runOneIteration()} method.
 * 
 * @author lpellegr
 */
public class LoadBalancingManager {

    private static final Logger log =
            LoggerFactory.getLogger(LoadBalancingManager.class);

    private static final int NB_MAX_ENTRIES =
            EventCloudProperties.LOAD_BALANCING_HISTORY_TIME_WINDOW.getValue()
                    / EventCloudProperties.LOAD_BALANCING_PROBING_TIMEOUT.getValue();

    // criteria taken into account to fix load imbalance
    private final Criterion[] criteria;

    private final LoadBalancingService scheduledService;

    private final GossipStrategy<LoadReport> gossiper;

    private final SemanticCanOverlay overlay;

    // indicates whether a load-balancing operation is in progress ot not
    private AtomicBoolean handlingImbalance;

    // load reports received ordered by peers and then by creation time
    // (peer url -> load report))
    private final ConcurrentMap<String, LoadReport> loadReportsReceived;

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

        this.handlingImbalance = new AtomicBoolean();

        this.loadReportsReceived =
                new ConcurrentHashMap<String, LoadReport>(NB_MAX_ENTRIES);

        this.overlay = overlay;
        this.scheduledService = new LoadBalancingService();
    }

    /**
     * Returns the local load factor at the time the method is called.
     * 
     * @return the local load factor at the time the method is called.
     */
    public double getLocalLoad() {
        return this.getCurrentLoad().computeWeightedSum(this.criteria);
    }

    /**
     * Returns the current view of the overlay load. This is an approximation
     * since the peers may report their load to a subset of the overlay only.
     * <p>
     * The average overlay load is computed by using only the last load report
     * received by a given peer.
     * 
     * @return the current view of the average overlay load. This is an
     *         approximation since the peers may report their load to a subset
     *         of the overlay only.
     */
    public double getAverageOverlayLoad() {
        int count = 0;
        double result = 0;

        for (LoadReport loadReport : this.loadReportsReceived.values()) {
            result += loadReport.computeWeightedSum(this.criteria);
            count++;
        }

        if (count > 0) {
            return result / count;
        }

        return count;
    }

    public boolean exceedsAverageSystemLoadHighThreshold(double value,
                                                         double averageSystemLoad) {
        double threshold =
                EventCloudProperties.LOAD_BALANCING_IMBALANCE_RATIO.getDefaultValue()
                        * averageSystemLoad;

        return value > threshold;
    }

    public boolean exceedsAverageSystemLoadLowThreshold(double value,
                                                        double averageSystemLoad) {
        double threshold =
                EventCloudProperties.LOAD_BALANCING_IMBALANCE_RATIO.getDefaultValue()
                        / averageSystemLoad;

        return value > threshold;
    }

    private boolean emergencyThresholdsViolatedBy(LoadReport loadReport) {
        for (int i = 0; i < this.criteria.length; i++) {
            if (loadReport.getValues()[i] > this.criteria[i].getEmergencyThreshold()) {
                return true;
            }
        }

        return false;
    }

    private boolean warmupThresholdsViolatedBy(LoadReport loadReport) {
        for (int i = 0; i < this.criteria.length; i++) {
            if (loadReport.getValues()[i] > this.criteria[i].getWarmupThreshold()) {
                return true;
            }
        }

        return false;
    }

    private boolean overloadConditionSatisfiedBy(LoadReport loadReport,
                                                 double averageOverlayLoad) {
        return loadReport.computeWeightedSum(this.criteria) > averageOverlayLoad
                * EventCloudProperties.LOAD_BALANCING_IMBALANCE_RATIO.getValue();
    }

    private boolean underloadConditionSatisfiedBy(LoadReport loadReport,
                                                  double averageOverlayLoad) {
        return loadReport.computeWeightedSum(this.criteria) < averageOverlayLoad
                / EventCloudProperties.LOAD_BALANCING_IMBALANCE_RATIO.getValue();
    }

    // TODO: remove once debugging terminated
    // private int step;

    public LoadState getState(LoadReport loadReport, double averageOverlayLoad) {
        // System.out.println("LoadBalancingManager.getState() ID=" +
        // overlay.name + ", PEERID=" + overlay.getId());
        // if (overlay.name.equals("toto1") && step > 5 && step % 4 == 0) {
        // step++;
        // return LoadState.OVERLOADED;
        // } else {
        // step++;
        // return LoadState.NORMAL;
        // }

        if (!this.warmupThresholdsViolatedBy(loadReport)) {
            return LoadState.NORMAL;
        }

        if (this.emergencyThresholdsViolatedBy(loadReport)
                || this.overloadConditionSatisfiedBy(
                        loadReport, averageOverlayLoad)) {
            return LoadState.OVERLOADED;
        }

        if (this.underloadConditionSatisfiedBy(loadReport, averageOverlayLoad)) {
            return LoadState.UNDERLOADED;
        }

        return LoadState.NORMAL;
    }

    public LoadState getState() {
        return this.getState(
                this.getCurrentLoad(), this.getAverageOverlayLoad());
    }

    public void clear() {
        this.loadReportsReceived.clear();
    }

    public void save(LoadReport loadReport) {
        if (this.handlingImbalance.get()) {
            return;
        }

        this.loadReportsReceived.put(loadReport.getPeerURL(), loadReport);

    }

    public void start() {
        this.scheduledService.start();
    }

    public void stop() {
        this.scheduledService.stop();
    }

    public LoadReport getCurrentLoad() {
        return new LoadReport(this.overlay.getPeerURL(), this.criteria);
    }

    public class LoadBalancingService extends AbstractScheduledService {

        private LoadReport lastReportGossiped;

        @Override
        protected void startUp() throws Exception {
            super.startUp();

            LoadReport loadReport = LoadBalancingManager.this.getCurrentLoad();

            LoadBalancingManager.this.gossiper.push(
                    LoadBalancingManager.this.overlay, loadReport);

            this.lastReportGossiped = loadReport;
        }

        @Override
        protected void runOneIteration() throws Exception {
            try {
                long startTime = 0;

                if (log.isTraceEnabled()) {
                    startTime = System.currentTimeMillis();
                }

                if (!LoadBalancingManager.this.handlingImbalance.get()) {
                    LoadReport localLoadReport =
                            LoadBalancingManager.this.getCurrentLoad();

                    double overlayLoad =
                            LoadBalancingManager.this.getAverageOverlayLoad();

                    // push the current load report to others in an
                    // epidemic-style if it is large enough compared to the
                    // previous sent
                    this.notifyNeighborsAboutLoadChange(localLoadReport);

                    LoadState loadState =
                            LoadBalancingManager.this.getState(
                                    localLoadReport, overlayLoad);

                    switch (loadState) {
                        case UNDERLOADED:
                            log.info(
                                    "Peer {} detected as underloaded and load balancing operation triggered",
                                    LoadBalancingManager.this.overlay.getId());

                            // LoadBalancingManager.this.handlingImbalance.set(true);
                            // LoadBalancingManager.this.overlay.getStub()
                            // .leave();
                            // LoadBalancingManager.this.handlingImbalance.set(false);
                            break;
                        case OVERLOADED:
                            log.info(
                                    "Peer {} detected as overloaded and load balancing operation triggered",
                                    LoadBalancingManager.this.overlay.getId());
                            LoadBalancingManager.this.handlingImbalance.set(true);

                            String bestSuitedPeerURL =
                                    this.findBestSuitedPeerForHandlingOverload();

                            if (bestSuitedPeerURL != null) {
                                SemanticPeer bestSuitedPeer =
                                        LoadBalancingManager.this.overlay.findPeerStub(bestSuitedPeerURL);

                                log.debug(
                                        "Found peer {} to force to rejoin on peer {}",
                                        bestSuitedPeer,
                                        LoadBalancingManager.this.overlay.getId());

                                // bestSuitedPeer.leave();
                                // bestSuitedPeer.join(LoadBalancingManager.this.overlay.getStub());

                                bestSuitedPeer.reassign(LoadBalancingManager.this.overlay.getStub());
                            } else {
                                log.debug("Allocating a new peer in the cloud");

                                // TODO: allocate a new machine in the cloud and
                                // deploy a new peer on it before to force this
                                // peer to join the current one
                                // SemanticPeer peer =
                                // SemanticFactory.newSemanticPeer(LoadBalancingManager.this.overlay.getOverlayProvider());
                                // peer.join(LoadBalancingManager.this.overlay.getStub());
                            }

                            LoadBalancingManager.this.handlingImbalance.set(false);
                            break;
                        default:
                            break;
                    }
                }

                if (log.isTraceEnabled()) {
                    log.trace("Load balancing service iteration done in "
                            + (System.currentTimeMillis() - startTime) + " ms");
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        private void notifyNeighborsAboutLoadChange(LoadReport loadReport) {
            double gossipRatio =
                    EventCloudProperties.LOAD_BALANCING_GOSSIP_RATIO.getDefaultValue();

            double lastLocalLoad = 0;
            double localLoad =
                    loadReport.computeWeightedSum(LoadBalancingManager.this.criteria);

            if (this.lastReportGossiped != null) {
                lastLocalLoad =
                        this.lastReportGossiped.computeWeightedSum(LoadBalancingManager.this.criteria);
            }

            boolean lastLocalLoadIsZero =
                    DoubleMath.fuzzyEquals(lastLocalLoad, 0, 1e-1);

            // TODO: condition should be tested once other bugs are fixed
            // if ((lastLocalLoadIsZero && localLoad > lastLocalLoad)
            // || (!lastLocalLoadIsZero && (localLoad > lastLocalLoad
            // * gossipRatio))
            // || (!lastLocalLoadIsZero && (localLoad < lastLocalLoad
            // / gossipRatio))) {
            log.debug(
                    "Gossiping load ({}) from peer {}", localLoad,
                    LoadBalancingManager.this.overlay.getId());
            LoadBalancingManager.this.gossiper.push(
                    LoadBalancingManager.this.overlay, loadReport);

            // add the report in our histogram to have a consistent view
            LoadBalancingManager.this.save(loadReport);

            this.lastReportGossiped = loadReport;
            // }
        }

        private String findBestSuitedPeerForHandlingOverload() {
            LoadReport currentLoadReport =
                    LoadBalancingManager.this.getCurrentLoad();

            double averageOverlayLoad =
                    LoadBalancingManager.this.getAverageOverlayLoad();
            double currentLoad =
                    currentLoadReport.computeWeightedSum(LoadBalancingManager.this.criteria);

            log.debug(
                    "Current load={}, average overlay load={}", currentLoad,
                    averageOverlayLoad);

            List<LoadReport> sortedLoadReports =
                    new ArrayList<LoadReport>(
                            LoadBalancingManager.this.loadReportsReceived.values());

            Collections.sort(sortedLoadReports, new Comparator<LoadReport>() {
                @Override
                public int compare(LoadReport r1, LoadReport r2) {
                    return Doubles.compare(
                            r1.computeWeightedSum(LoadBalancingManager.this.criteria),
                            r2.computeWeightedSum(LoadBalancingManager.this.criteria));
                }
            });

            // among the available entries, we have to find one that will not be
            // overloaded once half of the current peer load is transfered to it
            for (LoadReport possibleSolution : sortedLoadReports) {
                double possibleSolutionLoad =
                        possibleSolution.computeWeightedSum(LoadBalancingManager.this.criteria);

                log.debug(
                        "Possible solution load={}, average overlay load={}, url={}",
                        possibleSolutionLoad, averageOverlayLoad,
                        possibleSolution.getPeerURL());

                // compare to average overlay load
                if (!possibleSolution.getPeerURL().equals(
                        LoadBalancingManager.this.overlay.getPeerURL())
                        &&
                        // possibleSolution < ()
                        // compare to emergency thresholds
                        // &&
                        this.satisfyEmergencyThresholds(
                                currentLoadReport, possibleSolution)) {
                    return possibleSolution.getPeerURL();
                }
            }

            log.debug("No solution found!");
            // no solution found
            return null;
        }

        private boolean satisfyEmergencyThresholds(LoadReport current,
                                                   LoadReport solution) {
            for (int i = 0; i < LoadBalancingManager.this.criteria.length; i++) {
                if (solution.getValues()[i] + (current.getValues()[i] / 2) >= LoadBalancingManager.this.criteria[i].getEmergencyThreshold()) {
                    return false;
                }
            }

            return true;
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
