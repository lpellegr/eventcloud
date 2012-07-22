/**
 * Copyright (c) 2011-2012 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.MakeDeepCopy;

import fr.inria.eventcloud.api.generators.QuadrupleGenerator;

/**
 * Test cases associated to the {@link CompoundEvent} class.
 * 
 * @author lpellegr
 */
public class CompoundEventTest {

    @Test(expected = IllegalArgumentException.class)
    public void testInstanciationWithEmptyCollection() {
        new CompoundEvent(new ArrayList<Quadruple>());
    }

    @Test
    public void testInstanciation() {
        List<Quadruple> quads = new ArrayList<Quadruple>();
        for (int i = 0; i < 10; i++) {
            quads.add(QuadrupleGenerator.random());
        }

        CompoundEvent e1 = new CompoundEvent(quads);
        Assert.assertEquals(11, e1.getQuadruples().size());

        CompoundEvent e2 = null;
        try {
            e2 = (CompoundEvent) MakeDeepCopy.makeDeepCopy(e1);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(e1, e2);
    }

}
