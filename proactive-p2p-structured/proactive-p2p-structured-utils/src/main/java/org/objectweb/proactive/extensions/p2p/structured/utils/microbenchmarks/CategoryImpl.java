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
package org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks;

import java.util.Arrays;

import org.apache.commons.math3.stat.StatUtils;

/**
 * Default implementation for {@link Category}.
 * 
 * @author lpellegr
 */
public class CategoryImpl implements Category {

    private double[] values;

    private int index;

    private int discardFirstRuns;

    public CategoryImpl(int nbEntries, int discardFirstRuns) {
        this.values = new double[nbEntries + discardFirstRuns];
        this.discardFirstRuns = discardFirstRuns;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNbEntries() {
        return this.values.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMean() {
        return StatUtils.mean(this.filter(this.values));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMedian() {
        return StatUtils.percentile(this.filter(this.values), 50);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getStddev() {
        return Math.sqrt(this.getVariance());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getVariance() {
        return StatUtils.variance(this.filter(this.values));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getValue(int index) {
        return this.values[index];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reportValue(long value) {
        this.values[this.index] = value;
        this.index++;
    }

    private double[] filter(double[] values) {
        return Arrays.copyOfRange(
                values, this.discardFirstRuns, this.values.length);
    }

}
