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

import org.joda.time.DateTime;

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
        result.append("Load information retrieved at ");
        result.append(new DateTime(this.creationTime));
        result.append(":\n");
        // result.append("  directory usage=");
        // result.append(this.values[0]);
        // result.append("\n");
        // result.append("  nb quadruples stored=");
        // result.append(this.values[1]);
        // result.append("\n");
        // result.append("  system load=");
        // result.append(this.values[2]);

        return result.toString();
    }

}
