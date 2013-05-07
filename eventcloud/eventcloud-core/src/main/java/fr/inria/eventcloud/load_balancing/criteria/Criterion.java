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

/**
 * A load-balancing criterion.
 * 
 * @author lpellegr
 */
public abstract class Criterion {

    private final String name;

    private final Range<Double> domain;

    public Criterion(String name, Range<Double> domain) {
        this.name = name;
        this.domain = domain;
    }

    public String getName() {
        return this.name;
    }

    public Range<Double> getDomain() {
        return this.domain;
    }

    public double normalize(double value) {
        return value / this.domain.upperEndpoint();
    }

    public abstract double getLoad();

}
