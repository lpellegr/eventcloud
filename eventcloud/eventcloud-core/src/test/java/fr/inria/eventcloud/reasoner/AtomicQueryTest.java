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
package fr.inria.eventcloud.reasoner;

import org.junit.Assert;
import org.junit.Test;

import fr.inria.eventcloud.api.generators.NodeGenerator;
import fr.inria.eventcloud.exceptions.DecompositionException;

/**
 * Test cases associated to {@link AtomicQuery}.
 * 
 * @author lpellegr
 */
public class AtomicQueryTest {

    @Test
    public void testIsFilterEvaluationRequired() throws DecompositionException {
        AtomicQuery atomicQuery =
                new AtomicQuery(
                        NodeGenerator.randomUri(), NodeGenerator.randomUri(),
                        NodeGenerator.randomUri(), NodeGenerator.randomUri());

        Assert.assertFalse(atomicQuery.isFilterEvaluationRequired());
    }

}
