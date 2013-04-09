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
package fr.inria.eventcloud.benchmarks.pubsub.measurements;

/**
 * Simple measurement to keep entry and exit times.
 * 
 * @author lpellegr
 */
public class SimpleMeasurement implements Measurement {

    private static final long serialVersionUID = 140L;

    private long entryTime;

    private long exitTime;

    /**
     * {@inheritDoc}
     */
    @Override
    public long getElapsedTime() {
        return this.exitTime - this.entryTime;
    }

    public void setEntryTime() {
        this.entryTime = System.currentTimeMillis();
    }

    public void setExitTime() {
        this.exitTime = System.currentTimeMillis();
    }

    public void setExitTime(long timestamp) {
        this.exitTime = timestamp;
    }

}
