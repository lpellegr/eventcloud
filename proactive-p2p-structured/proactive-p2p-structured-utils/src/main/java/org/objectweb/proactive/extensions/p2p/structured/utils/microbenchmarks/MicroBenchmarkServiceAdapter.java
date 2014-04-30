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
 * Simple adapter for {@link MicroBenchmarkService}.
 * 
 * @author lpellegr
 */
public abstract class MicroBenchmarkServiceAdapter implements
        MicroBenchmarkService {

    /**
     * {@inheritDoc}
     */
    @Override
    public void setup() throws Exception {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void run(StatsRecorder recorder) throws Exception;

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() throws Exception {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void teardown() throws Exception {
    }

}
