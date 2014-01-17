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
package fr.inria.eventcloud.benchmarks.load_balancing;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;

import fr.inria.eventcloud.datastore.stats.CentroidStatsRecorder;
import fr.inria.eventcloud.datastore.stats.MeanStatsRecorder;
import fr.inria.eventcloud.datastore.stats.NullStatsRecorder;
import fr.inria.eventcloud.datastore.stats.StatsRecorder;

/**
 * Simple stats recorder class converter for {@link JCommander}.
 * 
 * @author lpellegr
 */
public class StatsRecorderClassConverter implements
        IStringConverter<Class<? extends StatsRecorder>> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends StatsRecorder> convert(String value) {

        if (value.equalsIgnoreCase("centroid")) {
            return CentroidStatsRecorder.class;
        } else if (value.equalsIgnoreCase("mean")) {
            return MeanStatsRecorder.class;
        } else if (value.equalsIgnoreCase("null")) {
            return NullStatsRecorder.class;
        }

        throw new IllegalArgumentException(
                "Unknown stats recorder class requested: " + value);
    }

}
