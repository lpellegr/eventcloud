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

import java.io.Serializable;

import fr.inria.eventcloud.load_balancing.criteria.Criterion;

/**
 * Provides load information. Mainly used to broadcast load to peers.
 * 
 * @author lpellegr
 */
public class LoadReport implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long creationTime;

    private final double[] values;

    protected LoadReport(Criterion[] criteria) {
        this.values = new double[criteria.length];

        for (int i = 0; i < criteria.length; i++) {
            this.values[i] = criteria[i].getLoad();
        }

        this.creationTime = System.currentTimeMillis();
    }

    public double computeWeightedSum(Criterion[] criteria) {
        if (criteria.length != this.values.length) {
            throw new IllegalArgumentException(
                    "Criteria length different from the number of load report values");
        }

        double result = 0;

        for (int i = 0; i < criteria.length; i++) {
            result += this.values[i] * criteria[i].getWeight();
        }

        return result;
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public double[] getValues() {
        return this.values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.creationTime);
        result.append(" -> ");

        for (int i = 0; i < this.values.length; i++) {
            result.append(this.values[i]);

            if (i < this.values.length - 1) {
                result.append(' ');
            }
        }

        return result.toString();
    }

}
