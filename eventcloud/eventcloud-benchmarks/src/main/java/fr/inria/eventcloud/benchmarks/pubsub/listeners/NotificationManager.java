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
package fr.inria.eventcloud.benchmarks.pubsub.listeners;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.benchmarks.pubsub.BenchmarkStatsCollector;
import fr.inria.eventcloud.benchmarks.pubsub.measurements.CumulatedMeasurement;
import fr.inria.eventcloud.benchmarks.pubsub.measurements.SimpleMeasurement;

/**
 * Base for custom notification listeners that may report stats to a
 * {@link BenchmarkStatsCollector} instance.
 * 
 * @author lpellegr
 */
public abstract class NotificationManager<T> implements Serializable {

    private static final long serialVersionUID = 160L;

    private final BenchmarkStatsCollector collector;

    private final CumulatedMeasurement pointToPointExitMeasurements;

    private final SimpleMeasurement outputMeasurement;

    private final int nbEventsExpected;

    private final AtomicInteger nbEventsReceived;

    private final int subscribeProxyDeliveryWaitTime;

    public NotificationManager(BenchmarkStatsCollector collector,
            int nbEventsExpected, int subscribeProxyDeliveryWaitTime) {
        super();

        this.collector = collector;
        this.pointToPointExitMeasurements =
                new CumulatedMeasurement(nbEventsExpected);
        this.outputMeasurement = new SimpleMeasurement();
        this.nbEventsExpected = nbEventsExpected;
        this.nbEventsReceived = new AtomicInteger();

        this.subscribeProxyDeliveryWaitTime = subscribeProxyDeliveryWaitTime;
    }

    // WARNING: this method may be invoked in parallel
    public void handleNewEvent(SubscriptionId subscriptionId, T solution) {
        this.pointToPointExitMeasurements.reportReception(this.getEventId(solution));

        int nbEventsReceived = this.nbEventsReceived.incrementAndGet();

        if (nbEventsReceived == 1) {
            this.outputMeasurement.setEntryTime();
        }

        if (nbEventsReceived == this.nbEventsExpected) {
            this.outputMeasurement.setExitTime();

            // notifies the collector in two steps to avoid to take the time
            // to transfer measurements into the end-to-end delay
            this.collector.reportEndToEndTermination(
                    subscriptionId, this.outputMeasurement.getExitTime());

            // to avoid concurrent modification exception given that we may
            // report event reception while we are trying to serialize the
            // collection
            synchronized (this.pointToPointExitMeasurements) {
                // reports other measurements
                this.collector.reportMeasurements(
                        subscriptionId, this.outputMeasurement,
                        this.pointToPointExitMeasurements);
            }
        }

        if (this.subscribeProxyDeliveryWaitTime > 0) {
            try {
                Thread.sleep(this.subscribeProxyDeliveryWaitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public abstract Node getEventId(T solution);

}
