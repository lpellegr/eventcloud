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
package fr.inria.eventcloud.benchmarks.pubsub.measurements;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.configuration.EventCloudProperties;

/**
 * Cumulated measurements to keep entry or exit times for a bunch of events.
 * 
 * @author lpellegr
 */
public class CumulatedMeasurement implements Serializable {

    private static final long serialVersionUID = 160L;

    private ConcurrentMap<String, Long> times;

    public CumulatedMeasurement(int nbEventsExpected) {
        this.times =
                new ConcurrentHashMap<String, Long>(
                        nbEventsExpected + 1,
                        1,
                        EventCloudProperties.MAO_LIMIT_SUBSCRIBE_PROXIES.getValue());
    }

    public void reportReception(Node eventId) {
        this.times.put(eventId.getURI(), System.currentTimeMillis());
    }

    public long getElapsedTime(Map<String, Long> pointToPointEntryMeasurements) {
        long sum = 0;

        for (Entry<String, Long> entry : this.times.entrySet()) {
            Long entryValue = pointToPointEntryMeasurements.get(entry.getKey());
            Long exitValue = entry.getValue();

            if (entryValue == null) {
                throw new IllegalStateException(
                        "Entry time not found for eventId "
                                + entry.getKey()
                                + "\nPoint-to-point entry map contains "
                                + pointToPointEntryMeasurements.size()
                                + " entrie(s)\n"
                                + "Point-to-point exit map contains "
                                + this.times.size()
                                + " entrie(s)\nPoint-to-point entry collection dump:\n"
                                + this.toString(pointToPointEntryMeasurements));
            }

            sum += exitValue - entryValue;
        }

        return sum;
    }

    private String toString(Map<String, Long> pointToPointMeasurements) {
        StringBuilder buf = new StringBuilder();

        for (Entry<String, Long> entry : pointToPointMeasurements.entrySet()) {
            buf.append("  ");
            buf.append(entry.getKey());
            buf.append(": ");
            buf.append(entry.getValue());
            buf.append("\n");
        }

        return buf.toString();
    }

}
