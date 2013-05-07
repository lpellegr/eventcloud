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

import java.util.Iterator;
import java.util.Queue;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import fr.inria.eventcloud.load_balancing.criteria.Criterion;

/**
 * 
 * 
 * @author lpellegr
 */
public class LoadReportSummarizer {

    public static double summarize(Queue<LoadReport> reports,
                                   Criterion[] criteria) {
        DescriptiveStatistics[] stats =
                new DescriptiveStatistics[criteria.length];
        for (int i = 0; i < criteria.length; i++) {
            stats[i] = new DescriptiveStatistics();
        }

        Iterator<LoadReport> it = reports.iterator();
        while (it.hasNext()) {
            LoadReport report = it.next();

            for (int i = 0; i < criteria.length; i++) {
                stats[i].addValue(criteria[i].normalize(report.getValues()[i]));
            }
        }

        double sum = 0;

        for (int i = 0; i < stats.length; i++) {
            sum += stats[i].getMean();
        }

        return sum;
    }

}
