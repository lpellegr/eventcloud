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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.objectweb.proactive.extensions.p2p.structured.utils.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.math.DoubleMath;
import com.google.common.primitives.Doubles;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.load_balancing.configuration.UniformLoadBalancingConfiguration;
import fr.inria.eventcloud.load_balancing.criteria.Criterion;
import fr.inria.eventcloud.load_balancing.gossip.GossipStrategy;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.SemanticPeer;

/**
 * 
 * 
 * @author lpellegr
 */
public class UniformLoadBalancingService extends LoadBalancingService {

    private static final Logger log =
            LoggerFactory.getLogger(LoadBalancingManager.class);

    private static final int NB_MAX_ENTRIES =
            EventCloudProperties.LOAD_BALANCING_HISTORY_TIME_WINDOW.getValue()
                    / EventCloudProperties.LOAD_BALANCING_PROBING_TIMEOUT.getValue();

    // criteria taken into account to fix load imbalance
    private final Criterion[] criteria;

    private final GossipStrategy<LoadReport> gossiper;

    private final SemanticCanOverlay overlay;

    // indicates whether a load-balancing operation is in progress ot not
    private AtomicBoolean handlingImbalance;

    // load reports received ordered by peers and then by creation time
    // (peer url -> load report))
    private final ConcurrentMap<String, LoadReport> loadReportsReceived;

    private LoadReport lastReportGossiped;

    @SuppressWarnings("unchecked")
    public UniformLoadBalancingService(final SemanticCanOverlay overlay,
            UniformLoadBalancingConfiguration loadBalancingConfiguration) {
        super(overlay, loadBalancingConfiguration);
        this.criteria = loadBalancingConfiguration.getCriteria();

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
        // System.out.println("DynamicLoadBalancingManager.getState() ID=" +
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

    public LoadReport getCurrentLoad() {
        return new LoadReport(this.overlay.getPeerURL(), this.criteria);
    }

    @Override
    protected void startUp() throws Exception {
        super.startUp();

        LoadReport loadReport =
                UniformLoadBalancingService.this.getCurrentLoad();

        UniformLoadBalancingService.this.gossiper.push(
                UniformLoadBalancingService.this.overlay, loadReport);

        this.lastReportGossiped = loadReport;
    }

    @Override
    protected void runOneIteration() throws Exception {
        try {
            long startTime = 0;

            if (log.isTraceEnabled()) {
                startTime = System.currentTimeMillis();
            }

            if (!UniformLoadBalancingService.this.handlingImbalance.get()) {
                LoadReport localLoadReport =
                        UniformLoadBalancingService.this.getCurrentLoad();

                double overlayLoad =
                        UniformLoadBalancingService.this.getAverageOverlayLoad();

                // push the current load report to others in an
                // epidemic-style if it is large enough compared to the
                // previous sent
                this.notifyNeighborsAboutLoadChange(localLoadReport);

                LoadState loadState =
                        UniformLoadBalancingService.this.getState(
                                localLoadReport, overlayLoad);

                switch (loadState) {
                    case UNDERLOADED:
                        log.info(
                                "Peer {} detected as underloaded and load balancing operation triggered",
                                UniformLoadBalancingService.this.overlay.getId());

                        // DynamicLoadBalancingManager.this.handlingImbalance.set(true);
                        // DynamicLoadBalancingManager.this.overlay.getStub()
                        // .leave();
                        // DynamicLoadBalancingManager.this.handlingImbalance.set(false);
                        break;
                    case OVERLOADED:
                        log.info(
                                "Peer {} detected as overloaded and load balancing operation triggered",
                                UniformLoadBalancingService.this.overlay.getId());
                        UniformLoadBalancingService.this.handlingImbalance.set(true);

                        String bestSuitedPeerURL =
                                this.findBestSuitedPeerForHandlingOverload();

                        if (bestSuitedPeerURL != null) {
                            SemanticPeer bestSuitedPeer =
                                    UniformLoadBalancingService.this.overlay.findPeerStub(bestSuitedPeerURL);

                            log.debug(
                                    "Found peer {} to force to rejoin on peer {}",
                                    bestSuitedPeer,
                                    UniformLoadBalancingService.this.overlay.getId());

                            // bestSuitedPeer.leave();
                            // bestSuitedPeer.join(DynamicLoadBalancingManager.this.overlay.getStub());

                            bestSuitedPeer.reassign(UniformLoadBalancingService.this.overlay.getStub());
                        } else {
                            log.debug("Allocating a new peer in the cloud");

                            // TODO: allocate a new machine in the cloud and
                            // deploy a new peer on it before to force this
                            // peer to join the current one
                            // SemanticPeer peer =
                            // SemanticFactory.newSemanticPeer(DynamicLoadBalancingManager.this.overlay.getOverlayProvider());
                            // peer.join(DynamicLoadBalancingManager.this.overlay.getStub());
                        }

                        UniformLoadBalancingService.this.handlingImbalance.set(false);
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

    @SuppressWarnings("unused")
    private void notifyNeighborsAboutLoadChange(LoadReport loadReport) {
        double gossipRatio =
                EventCloudProperties.LOAD_BALANCING_GOSSIP_RATIO.getDefaultValue();

        double lastLocalLoad = 0;
        double localLoad =
                loadReport.computeWeightedSum(UniformLoadBalancingService.this.criteria);

        if (this.lastReportGossiped != null) {
            lastLocalLoad =
                    this.lastReportGossiped.computeWeightedSum(UniformLoadBalancingService.this.criteria);
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
                UniformLoadBalancingService.this.overlay.getId());
        UniformLoadBalancingService.this.gossiper.push(
                UniformLoadBalancingService.this.overlay, loadReport);

        // add the report in our histogram to have a consistent view
        UniformLoadBalancingService.this.save(loadReport);

        this.lastReportGossiped = loadReport;
        // }
    }

    private String findBestSuitedPeerForHandlingOverload() {
        LoadReport currentLoadReport =
                UniformLoadBalancingService.this.getCurrentLoad();

        double averageOverlayLoad =
                UniformLoadBalancingService.this.getAverageOverlayLoad();
        double currentLoad =
                currentLoadReport.computeWeightedSum(UniformLoadBalancingService.this.criteria);

        log.debug(
                "Current load={}, average overlay load={}", currentLoad,
                averageOverlayLoad);

        List<LoadReport> sortedLoadReports =
                new ArrayList<LoadReport>(
                        UniformLoadBalancingService.this.loadReportsReceived.values());

        // sorts the load reports by load
        Collections.sort(sortedLoadReports, new Comparator<LoadReport>() {
            @Override
            public int compare(LoadReport r1, LoadReport r2) {
                return Doubles.compare(
                        r1.computeWeightedSum(UniformLoadBalancingService.this.criteria),
                        r2.computeWeightedSum(UniformLoadBalancingService.this.criteria));
            }
        });

        // removes the entry about the current peer, to avoid to balance the
        // load with itself
        Iterator<LoadReport> it = sortedLoadReports.iterator();
        while (it.hasNext()) {
            LoadReport loadReport = it.next();

            if (loadReport.getPeerURL().equals(
                    UniformLoadBalancingService.this.overlay.getPeerURL())) {
                it.remove();
                break;
            }
        }

        // looks at the lower value
        // if it is not satisfying the conditions there is no solution since
        // all load reports have greater load value
        double possibleSolutionLoad = -1;
        if (sortedLoadReports.size() > 0) {
            LoadReport possibleSolution = sortedLoadReports.get(0);

            // among the available entries, we have to find one that will
            // not be overloaded once half of the current peer load is
            // transfered to it
            if (this.satisfyEmergencyThresholds(
                    currentLoadReport, possibleSolution)) {
                possibleSolutionLoad =
                        possibleSolution.computeWeightedSum(UniformLoadBalancingService.this.criteria);
            }
        }

        if (possibleSolutionLoad < 0) {
            log.debug("No solution found!");
            return null;
        }

        // finds other load reports that have the same load
        int index = 1;
        for (int i = 1; i < sortedLoadReports.size(); i++) {
            double load =
                    sortedLoadReports.get(i).computeWeightedSum(
                            UniformLoadBalancingService.this.criteria);

            if (DoubleMath.fuzzyEquals(load, possibleSolutionLoad, 1e-3)) {
                index++;
            } else {
                break;
            }
        }

        // picks a peer at random
        return sortedLoadReports.get(RandomUtils.nextInt(index)).getPeerURL();
    }

    private boolean satisfyEmergencyThresholds(LoadReport current,
                                               LoadReport solution) {
        for (int i = 0; i < UniformLoadBalancingService.this.criteria.length; i++) {
            if (solution.getValues()[i] + (current.getValues()[i] / 2) >= UniformLoadBalancingService.this.criteria[i].getEmergencyThreshold()) {
                return false;
            }
        }

        return true;
    }

}
