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
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.util.concurrent.AbstractScheduledService;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.factories.SemanticFactory;
import fr.inria.eventcloud.load_balancing.criteria.Criterion;
import fr.inria.eventcloud.load_balancing.gossip.GossipStrategy;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.SemanticPeer;

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

    private final LoadBalancingService scheduledService;

    private final GossipStrategy<LoadReport> gossiper;

    private final SemanticCanOverlay overlay;

    private AtomicBoolean handlingImbalance;

    private AtomicInteger nbLoadReportsReceived;

    private final ConcurrentMap<String, List<LoadReport>> loadReportsReceived;

    private final ConcurrentSkipListMap<Long, String> loadReportsReceivedOrdered;

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
        this.nbLoadReportsReceived = new AtomicInteger();

        this.loadReportsReceived =
                new ConcurrentHashMap<String, List<LoadReport>>(NB_MAX_ENTRIES);
        this.loadReportsReceivedOrdered =
                new ConcurrentSkipListMap<Long, String>();

        this.overlay = overlay;
        this.scheduledService = new LoadBalancingService();
    }

    public double getLocalLoad() {
        return this.getCurrentLoad().computeWeightedSum(this.criteria);
    }

    public double getSystemLoad() {
        return summarize(this.loadReportsReceived, this.criteria);
    }

    public boolean exceedsAverageSystemLoadHighThreshold(double value) {
        double overlayLoad = this.getSystemLoad();

        double threshold =
                EventCloudProperties.LOAD_BALANCING_IMBALANCE_RATIO.getDefaultValue()
                        * overlayLoad;

        return value > threshold;
    }

    public boolean exceedsAverageSystemLoadLowThreshold(double value) {
        double overlayLoad = summarize(this.loadReportsReceived, this.criteria);

        double threshold =
                EventCloudProperties.LOAD_BALANCING_IMBALANCE_RATIO.getDefaultValue()
                        / overlayLoad;

        return value > threshold;
    }

    public LoadState getLoadState() {
        LoadReport currentLoadReport = this.getCurrentLoad();

        double currentLoad =
                currentLoadReport.computeWeightedSum(this.criteria);

        boolean warmupEnded = true;

        for (int i = 0; i < this.criteria.length; i++) {
            if (currentLoadReport.getValues()[i] <= this.criteria[i].getWarmupThreshold()) {
                warmupEnded &= false;
                continue;
            }

            if (currentLoadReport.getValues()[i] >= this.criteria[i].getEmergencyThreshold()) {
                return LoadState.OVERLOADED;
            }
        }

        if (warmupEnded) {
            if (this.exceedsAverageSystemLoadHighThreshold(currentLoad)) {
                return LoadState.OVERLOADED;
            } else if (this.exceedsAverageSystemLoadLowThreshold(currentLoad)) {
                return LoadState.UNDERLOADED;
            }
        }

        return LoadState.NORMAL;
    }

    public void save(LoadReport loadReport) {
        if (this.handlingImbalance.get()) {
            return;
        }

        // TODO replace list by concurrent map
        List<LoadReport> l =
                Collections.synchronizedList(Lists.newArrayList(loadReport));

        if ((l =
                this.loadReportsReceived.putIfAbsent(loadReport.getPeerURL(), l)) != null) {
            l.add(loadReport);
        }

        this.loadReportsReceivedOrdered.put(
                loadReport.getCreationTime(), loadReport.getPeerURL());

        if (this.nbLoadReportsReceived.incrementAndGet() > NB_MAX_ENTRIES) {
            Entry<Long, String> entry =
                    this.loadReportsReceivedOrdered.pollFirstEntry();

            Iterator<LoadReport> it =
                    this.loadReportsReceived.get(entry.getValue()).iterator();
            while (it.hasNext()) {
                LoadReport r = it.next();

                if (r.getCreationTime() == entry.getKey()) {
                    it.remove();
                    break;
                }
            }
        }

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

    public static double summarize(ConcurrentMap<String, List<LoadReport>> reports,
                                   Criterion[] criteria) {
        double result = 0;

        for (List<LoadReport> reportList : reports.values()) {
            for (LoadReport report : reportList) {
                result += report.computeWeightedSum(criteria);
            }
        }

        result /= reports.size();

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

            LoadReport currentLoad = LoadBalancingManager.this.getCurrentLoad();

            // push the current load report to others in an epidemic-style if it
            // large enough compared to the previous sent
            if (!LoadBalancingManager.this.handlingImbalance.get()) {

                double currentLocalLoad = 0;
                double lastLocalLoad = 0;
                double imbalanceRatio = 0;

                if (this.lastReport != null) {
                    currentLocalLoad =
                            currentLoad.computeWeightedSum(LoadBalancingManager.this.criteria);
                    lastLocalLoad =
                            this.lastReport.computeWeightedSum(LoadBalancingManager.this.criteria);
                    imbalanceRatio =
                            EventCloudProperties.LOAD_BALANCING_GOSSIP_RATIO.getDefaultValue();
                }

                if (this.lastReport == null
                        || currentLocalLoad > imbalanceRatio * lastLocalLoad
                        || currentLocalLoad < lastLocalLoad / imbalanceRatio) {
                    log.debug(
                            "Gossiping current load ({}) from peer {}",
                            currentLoad,
                            LoadBalancingManager.this.overlay.getId());

                    LoadBalancingManager.this.gossiper.push(
                            LoadBalancingManager.this.overlay, currentLoad);

                    this.lastReport = currentLoad;

                    switch (LoadBalancingManager.this.getLoadState()) {
                        case UNDERLOADED:
                            log.info(
                                    "Peer {} detected as underloaded and load balancing operation triggered",
                                    LoadBalancingManager.this.overlay.getId());

                            LoadBalancingManager.this.handlingImbalance.set(true);
                            LoadBalancingManager.this.overlay.getStub().leave();
                            LoadBalancingManager.this.handlingImbalance.set(false);
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

                                bestSuitedPeer.leave();
                                bestSuitedPeer.join(LoadBalancingManager.this.overlay.getStub());
                            } else {
                                log.debug("Allocating a new peer in the cloud");

                                // TODO: allocate a new machine in the cloud and
                                // deploy a new peer on it before to force this
                                // peer to join the current one
                                SemanticPeer peer =
                                        SemanticFactory.newSemanticPeer(LoadBalancingManager.this.overlay.getOverlayProvider());
                                peer.join(LoadBalancingManager.this.overlay.getStub());
                            }

                            LoadBalancingManager.this.handlingImbalance.set(false);
                            break;
                        default:
                            break;
                    }
                }
            }

            if (log.isTraceEnabled()) {
                log.trace("Load balancing service iteration done in "
                        + (System.currentTimeMillis() - startTime) + " ms");
            }
        }

        private String findBestSuitedPeerForHandlingOverload() {
            List<LoadReport> reports =
                    new ArrayList<LoadReport>(
                            LoadBalancingManager.this.loadReportsReceived.size());

            for (Entry<String, List<LoadReport>> entry : LoadBalancingManager.this.loadReportsReceived.entrySet()) {
                reports.add(entry.getValue().get(entry.getValue().size() - 1));
            }

            // sort entries from the least loaded to the most loaded
            Collections.sort(reports, new Comparator<LoadReport>() {
                @Override
                public int compare(LoadReport r1, LoadReport r2) {
                    return Doubles.compare(
                            r1.computeWeightedSum(LoadBalancingManager.this.criteria),
                            r2.computeWeightedSum(LoadBalancingManager.this.criteria));
                }
            });

            LoadReport currentLoad = LoadBalancingManager.this.getCurrentLoad();

            log.debug("Current load={}", currentLoad);

            // among the available entries, we have to find one that will not be
            // overloaded once half of the current peer load is transfered to it
            for (LoadReport possibleSolution : reports) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Possible solution={}, average system load={}",
                            possibleSolution,
                            LoadBalancingManager.this.getSystemLoad());
                }

                // compare to average system load
                if (!LoadBalancingManager.this.exceedsAverageSystemLoadHighThreshold(possibleSolution.computeWeightedSum(LoadBalancingManager.this.criteria)
                        + (currentLoad.computeWeightedSum(LoadBalancingManager.this.criteria) / 2))
                        // compare to emergency thresholds
                        && this.satisfyEmergencyThresholds(
                                currentLoad, possibleSolution)) {
                    return possibleSolution.getPeerURL();
                }
            }

            // no solution found
            return null;
        }

        private boolean satisfyEmergencyThresholds(LoadReport current,
                                                   LoadReport solution) {
            for (int i = 0; i < LoadBalancingManager.this.criteria.length; i++) {
                if (current.getValues()[i] + (solution.getValues()[i] / 2) > LoadBalancingManager.this.criteria[i].getEmergencyThreshold()) {
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
