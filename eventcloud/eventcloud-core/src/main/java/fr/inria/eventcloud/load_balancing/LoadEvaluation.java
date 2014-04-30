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
package fr.inria.eventcloud.load_balancing;

import com.google.common.base.Objects;

import fr.inria.eventcloud.load_balancing.criteria.Criterion;

/**
 * Load evaluation report generated for each load balancing iteration.
 * 
 * @author lpellegr
 */
public class LoadEvaluation {

    public final Criterion criterion;

    public final LoadState loadState;

    public final double measure;

    public final double estimate;

    public LoadEvaluation(Criterion criterion, LoadState loadState,
            double measure, double estimate) {
        this.criterion = criterion;
        this.loadState = loadState;
        this.measure = measure;
        this.estimate = estimate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Objects.toStringHelper(this.getClass()).add(
                "criterion", this.criterion.getName()).add(
                "loadState", this.loadState).add("measure", this.measure).add(
                "estimate", this.estimate).toString();
    }

}
