/**
 * Copyright (c) 2011 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.api;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.util.converter.MakeDeepCopy;

import fr.inria.eventcloud.utils.generator.QuadrupleGenerator;

/**
 * Test cases associated to the {@link Event} class.
 * 
 * @author lpellegr
 */
public class EventTest {

    @Test(expected = IllegalArgumentException.class)
    public void testInstanciationWithEmptyCollection() {
        new Event(new Collection<Quadruple>());
    }

    @Test
    public void testInstanciation() {
        Collection<Quadruple> quads = new Collection<Quadruple>();
        for (int i = 0; i < 10; i++) {
            quads.add(QuadrupleGenerator.create());
        }

        Event e1 = new Event(quads);
        Assert.assertEquals(10, e1.getQuadruples().size());

        Event e2 = null;
        try {
            e2 = (Event) MakeDeepCopy.WithObjectStream.makeDeepCopy(e1);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(e1, e2);
    }

}
