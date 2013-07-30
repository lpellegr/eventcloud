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

/**
 * A category is a bucket to collect several timing information for a same
 * execution scenario but from different runs. Once the information are
 * collected it is possible to get some statistical indicators for these values.
 * 
 * @author lpellegr
 */
public interface Category {

    int getNbEntries();

    double getMean();

    double getMedian();

    double getStddev();

    double getVariance();

    double getTime(int index);

    void reportTime(long time);

}
