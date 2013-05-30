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
package fr.inria.eventcloud.load_balancing.criteria;

import com.google.common.collect.Range;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.stats.StatsRecorder;

/**
 * Number of quadruples stored load-balancing criterion.
 * 
 * @author lpellegr
 */
public class NbQuadrupleStoredCriterion extends Criterion {

    private final StatsRecorder statsRecorder;

    public NbQuadrupleStoredCriterion(StatsRecorder statsRecorder) {
        super(
                "nb quadruples stored",
                Range.closed(0.0, (double) Long.MAX_VALUE),
                5,
                EventCloudProperties.LOAD_BALANCING_CRITERION_NB_QUADS_STORED_EMERGENCY_THRESHOLD.getValue(),
                1);

        this.statsRecorder = statsRecorder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLoad() {
        return this.statsRecorder.getNbQuadruples();
    }

}
