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
package fr.inria.eventcloud.api;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.MakeDeepCopy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import fr.inria.eventcloud.api.generators.CompoundEventGenerator;
import fr.inria.eventcloud.api.generators.NodeGenerator;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;

/**
 * Test cases associated to the {@link CompoundEvent} class.
 * 
 * @author lpellegr
 */
public class CompoundEventTest {

    @Test
    public void testCompoundEventValidity1() {
        CompoundEvent compoundEvent = CompoundEventGenerator.random(10);

        Assert.assertTrue(CompoundEvent.isValid(compoundEvent));
    }

    @Test
    public void testCompoundEventValidity2() {
        Builder<Quadruple> quadruples = new ImmutableList.Builder<Quadruple>();
        for (int i = 0; i < 10; i++) {
            quadruples.add(QuadrupleGenerator.random());
        }

        Assert.assertFalse(CompoundEvent.isValid(new CompoundEvent(
                quadruples.build())));
    }

    @Test
    public void testCreateMetaQuadruple() {
        CompoundEvent compoundEvent = CompoundEventGenerator.random(10);

        int metaQuadrupleValue =
                (Integer) CompoundEvent.createMetaQuadruple(compoundEvent)
                        .getObject()
                        .getLiteralValue();

        Assert.assertEquals(10, metaQuadrupleValue);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testImmutability1() {
        CompoundEvent compoundEvent = CompoundEventGenerator.random(10);
        compoundEvent.getTriples().add(
                new Triple(
                        NodeGenerator.randomUri(), NodeGenerator.randomUri(),
                        NodeGenerator.randomUri()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInstanciationWithEmptyCollection() {
        new CompoundEvent(new ArrayList<Quadruple>());
    }

    @Test
    public void testInstanciation() {
        Node graphValue = Node.createURI("urn:g");

        Builder<Quadruple> builder = new ImmutableList.Builder<Quadruple>();
        for (int i = 0; i < 10; i++) {
            builder.add(QuadrupleGenerator.random(graphValue));
        }

        ImmutableList<Quadruple> quadruples = builder.build();

        CompoundEvent e1 = new CompoundEvent(quadruples);
        Assert.assertEquals(quadruples.size(), e1.size());

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
