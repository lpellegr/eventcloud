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
package org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks;

/**
 * Defines the action to execute with a {@link MicroBenchmark}.
 * 
 * @author lpellegr
 */
public interface MicroBenchmarkService {

    /**
     * Invoked before the first run. The purpose of this method is to setup the
     * infrastructure used to execute the different runs.
     */
    void setup() throws Exception;

    /**
     * Benchmark run to evaluate.
     */
    void run(StatsRecorder recorder) throws Exception;

    /**
     * Invoked after each run to clean up data structures.
     */
    void clear() throws Exception;

    /**
     * Invoked after the last run.
     */
    void teardown() throws Exception;

}
