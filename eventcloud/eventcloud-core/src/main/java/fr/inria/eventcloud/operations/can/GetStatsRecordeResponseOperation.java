/**
 * Copyright (c) 2011-2012 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;

import fr.inria.eventcloud.datastore.stats.StatsRecorder;

/**
 * Response associated to {@link GetStatsRecorderOperation}.
 * 
 * @author lpellegr
 */
public class GetStatsRecordeResponseOperation implements ResponseOperation {

    private static final long serialVersionUID = 140L;

    private final StatsRecorder statsRecorder;

    public GetStatsRecordeResponseOperation(StatsRecorder statsRecorder) {
        this.statsRecorder = statsRecorder;
    }

    /**
     * Returns a deep copy of the {@link StatsRecorder} instance contained by
     * the misc datastore.
     * 
     * @return a deep copy of the {@link StatsRecorder} instance contained by
     *         the misc datastore.
     */
    public StatsRecorder getStatsRecorder() {
        return this.statsRecorder;
    }

}
