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

import com.google.common.util.concurrent.AbstractScheduledService;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.stats.StatsRecorder;
import fr.inria.eventcloud.load_balancing.criteria.Criterion;
import fr.inria.eventcloud.load_balancing.criteria.NbQuadrupleStoredCriterion;

/**
 * Entity in charge of managing load-balancing features.
 * 
 * @author lpellegr
 */
public class LoadBalancingManager {

    private static final int NB_MAX_ENTRIES =
            EventCloudProperties.LOAD_BALANCING_HISTORY_TIME_WINDOW.getValue()
                    / EventCloudProperties.LOAD_BALANCING_PROBING_TIMEOUT.getValue();

    private final Criterion[] criteria;

    // sliding window that keeps only the last NB_MAX_ENTRIES reports
    private final Queue<LoadReport> loadReportsReceived;

    private AbstractScheduledService scheduledService;

    public LoadBalancingManager(StatsRecorder miscDatastoreStatsRecorder) {
        this.criteria = new Criterion[] {
        // new DiskUsageCriterion(
        // EventCloudProperties.REPOSITORIES_PATH.getValue(),
        // EventCloudProperties.LOAD_BALANCING_DISK_CAPACITY.getValue()),
        new NbQuadrupleStoredCriterion(miscDatastoreStatsRecorder),
        // new SystemLoadCriterion()
                };

        this.loadReportsReceived = EvictingQueue.create(NB_MAX_ENTRIES);

        this.scheduledService = new AbstractScheduledService() {
            @Override
            protected void runOneIteration() throws Exception {
                // LoadReport report =
                // new LoadReport(LoadBalancingManager.this.criteria);

                // TODO: define how to report load
            }

            /**
             * {@inheritDoc}
             */
            @Override
            protected String serviceName() {
                return "Load-balancing probing service";
            }

            @Override
            protected Scheduler scheduler() {
                return AbstractScheduledService.Scheduler.newFixedDelaySchedule(
                        EventCloudProperties.LOAD_BALANCING_PROBING_TIMEOUT.getValue(),
                        EventCloudProperties.LOAD_BALANCING_PROBING_TIMEOUT.getValue(),
                        TimeUnit.MILLISECONDS);
            }
        };
    }

    // public DiskUsageCriterion getDiskUsageCriterion() {
    // return (DiskUsageCriterion) this.criteria[0];
    // }

    public NbQuadrupleStoredCriterion getNbQuadrupleStoredCriterion() {
        return (NbQuadrupleStoredCriterion) this.criteria[1];
    }

    // public SystemLoadCriterion getSystemLoadCriterion() {
    // return (SystemLoadCriterion) this.criteria[2];
    // }

    public void save(LoadReport systemLoadReport) {
        this.loadReportsReceived.add(systemLoadReport);

    }

    public void start() {
        this.scheduledService.start();
    }

    public void stop() {
        this.scheduledService.stop();
    }

}
