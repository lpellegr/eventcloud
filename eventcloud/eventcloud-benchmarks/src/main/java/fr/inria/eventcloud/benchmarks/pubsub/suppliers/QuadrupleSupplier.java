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
package fr.inria.eventcloud.benchmarks.pubsub.suppliers;

import com.google.common.base.Supplier;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;

/**
 * This class provides a randomly generated {@link Quadruple} each time a call
 * to {@link QuadrupleSupplier#get()} is performed.
 * 
 * @author lpellegr
 */
public class QuadrupleSupplier implements Supplier<Quadruple> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Quadruple get() {
        return QuadrupleGenerator.randomWithoutLiteral();
    }

}
