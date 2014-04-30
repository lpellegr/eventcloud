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
package fr.inria.eventcloud.api;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.MakeDeepCopy;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

import fr.inria.eventcloud.api.generators.NodeGenerator;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;

/**
 * Tests cases associated to the {@link Quadruple} class.
 * 
 * @author lpellegr
 */
public class QuadrupleTest {

    @Test(expected = IllegalArgumentException.class)
    public void testInstantiationWithBlankNode() {
        new Quadruple(
                NodeFactory.createAnon(), NodeGenerator.randomUri(),
                NodeGenerator.randomUri(), NodeGenerator.random());
        new Quadruple(
                NodeGenerator.randomUri(), NodeFactory.createAnon(),
                NodeGenerator.randomUri(), NodeGenerator.random());
        new Quadruple(
                NodeGenerator.randomUri(), NodeGenerator.randomUri(),
                NodeFactory.createAnon(), NodeGenerator.random());
        new Quadruple(
                NodeGenerator.randomUri(), NodeGenerator.randomUri(),
                NodeGenerator.randomUri(), NodeFactory.createAnon());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInstantiationWithVars() {
        new Quadruple(
                Node.ANY, NodeGenerator.randomUri(), NodeGenerator.randomUri(),
                NodeGenerator.random());
        new Quadruple(
                NodeGenerator.randomUri(), Node.ANY, NodeGenerator.randomUri(),
                NodeGenerator.random());
        new Quadruple(
                NodeGenerator.randomUri(), NodeGenerator.randomUri(), Node.ANY,
                NodeGenerator.random());
        new Quadruple(
                NodeGenerator.randomUri(), NodeGenerator.randomUri(),
                NodeGenerator.randomUri(), Node.ANY);

        new Quadruple(
                NodeFactory.createVariable("test"), NodeGenerator.randomUri(),
                NodeGenerator.randomUri(), NodeGenerator.random());
        new Quadruple(
                NodeGenerator.randomUri(), NodeFactory.createVariable("test"),
                NodeGenerator.randomUri(), NodeGenerator.random());
        new Quadruple(
                NodeGenerator.randomUri(), NodeGenerator.randomUri(),
                NodeFactory.createVariable("test"), NodeGenerator.random());
        new Quadruple(
                NodeGenerator.randomUri(), NodeGenerator.randomUri(),
                NodeGenerator.randomUri(), NodeFactory.createVariable("test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInstantiationWithNullValues() {
        new Quadruple(
                null, NodeGenerator.randomUri(), NodeGenerator.randomUri(),
                NodeGenerator.random());
        new Quadruple(
                NodeGenerator.randomUri(), null, NodeGenerator.randomUri(),
                NodeGenerator.random());
        new Quadruple(
                NodeGenerator.randomUri(), NodeGenerator.randomUri(), null,
                NodeGenerator.random());
        new Quadruple(
                NodeGenerator.randomUri(), NodeGenerator.randomUri(),
                NodeGenerator.random(), null);

        new Quadruple(
                null, null, NodeGenerator.randomUri(), NodeGenerator.random());
        new Quadruple(
                null, NodeGenerator.randomUri(), null, NodeGenerator.random());
        new Quadruple(
                null, NodeGenerator.randomUri(), NodeGenerator.randomUri(),
                null);
        new Quadruple(
                NodeGenerator.randomUri(), null, null, NodeGenerator.random());
        new Quadruple(
                NodeGenerator.randomUri(), null, NodeGenerator.randomUri(),
                null);
        new Quadruple(
                NodeGenerator.randomUri(), NodeGenerator.randomUri(), null,
                null);

        new Quadruple(null, null, null, NodeGenerator.random());
        new Quadruple(null, NodeGenerator.randomUri(), null, null);
        new Quadruple(null, null, NodeGenerator.randomUri(), null);

        new Quadruple(null, null, null, null);
    }

    @Test
    public void testAddMetaInformation() {
        Quadruple q1 = QuadrupleGenerator.randomWithLiteral();

        long publicationTime = System.currentTimeMillis();
        q1.setPublicationSource("publisher1");
        q1.setPublicationTime(publicationTime);

        Assert.assertEquals(publicationTime, q1.getPublicationTime());
        Assert.assertEquals("publisher1", q1.getPublicationSource());
    }

    @Test
    public void testEquality() {
        Quadruple q1 = QuadrupleGenerator.randomWithLiteral();
        Quadruple q2 =
                new Quadruple(
                        q1.getGraph(), q1.getSubject(), q1.getPredicate(),
                        q1.getObject());

        Assert.assertEquals(q1, q2);

        q1.setPublicationTime(System.currentTimeMillis());
        q2.setPublicationTime(System.currentTimeMillis() + 1);

        Assert.assertFalse(q1.equals(q2));
        Assert.assertFalse(q1.hashCode() == q2.hashCode());

        q1.setPublicationSource("p1");
        q2.setPublicationSource("p2");

        Assert.assertFalse(q1.equals(q2));
        Assert.assertFalse(q1.hashCode() == q2.hashCode());

        q2.setPublicationSource("p1");

        Assert.assertFalse(q1.equals(q2));
        Assert.assertFalse(q1.hashCode() == q2.hashCode());

        q2.setPublicationTime(q1.getPublicationTime());

        Assert.assertEquals(q1, q2);
        Assert.assertTrue(q1.hashCode() == q2.hashCode());
    }

    @Test
    public void testGetPublicationTime() {
        Quadruple q1 = QuadrupleGenerator.random();

        Assert.assertEquals(
                -1, Quadruple.getPublicationTime(q1.createMetaGraphNode()));

        long publicationTime = System.currentTimeMillis();
        q1.setPublicationTime(publicationTime);

        Assert.assertEquals(
                publicationTime,
                Quadruple.getPublicationTime(q1.createMetaGraphNode()));
    }

    @Test
    public void testGetPublicationSource() {
        Quadruple q1 = QuadrupleGenerator.random();

        Assert.assertNull(Quadruple.getPublicationSource(q1.createMetaGraphNode()));

        q1.setPublicationTime();
        q1.setPublicationSource("publisher1");

        Assert.assertEquals(
                "publisher1",
                Quadruple.getPublicationSource(q1.createMetaGraphNode()));
    }

    @Test
    public void testIsMetaGraphNode() {
        Quadruple q1 = QuadrupleGenerator.random();

        Assert.assertFalse(Quadruple.isMetaGraphNode(q1.getGraph()));
        Assert.assertFalse(Quadruple.isMetaGraphNode(q1.getSubject()));
        Assert.assertFalse(Quadruple.isMetaGraphNode(q1.getPredicate()));
        Assert.assertFalse(Quadruple.isMetaGraphNode(q1.getObject()));

        Assert.assertFalse(Quadruple.isMetaGraphNode(q1.createMetaGraphNode()));

        q1.setPublicationTime();
        Assert.assertTrue(Quadruple.isMetaGraphNode(q1.createMetaGraphNode()));
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        Quadruple q1 = QuadrupleGenerator.randomWithLiteral();
        q1.setPublicationSource("publisher1");
        q1.setPublicationTime(System.currentTimeMillis());

        Quadruple q2 = (Quadruple) MakeDeepCopy.makeDeepCopy(q1);

        Assert.assertEquals("Quadruples are not equals after deep copy", q1, q2);

        Quadruple q3 = (Quadruple) MakeDeepCopy.makeDeepCopy(q2);

        Assert.assertEquals(q2, q3);
        Assert.assertEquals(q1, q3);

        Quadruple q4 =
                new Quadruple(
                        q3.createMetaGraphNode(), q3.getSubject(),
                        q3.getPredicate(), q3.getObject(), false, true);

        Assert.assertEquals(q3, q4);
    }

    @Test
    public void testSerializationObjectLiteralLanguageTag() throws IOException,
            ClassNotFoundException {
        testQuadrupleSerialization(new Quadruple(
                NodeGenerator.randomUri(), NodeGenerator.randomUri(),
                NodeGenerator.randomUri(), NodeFactory.createLiteral(
                        "hello", "en", null)));
    }

    @Test
    public void testSerializationObjectLiteralDatatype() throws IOException,
            ClassNotFoundException {
        testQuadrupleSerialization(new Quadruple(
                NodeGenerator.randomUri(), NodeGenerator.randomUri(),
                NodeGenerator.randomUri(), NodeFactory.createLiteral(
                        "true", null, XSDDatatype.XSDboolean)));
    }

    @Test
    public void testSerializationObjectLiteralLanguageTagDatatype() {
    }

    private static void testQuadrupleSerialization(Quadruple q)
            throws ClassNotFoundException, IOException {
        Quadruple deepCopy = (Quadruple) MakeDeepCopy.makeDeepCopy(q);

        Assert.assertEquals(
                "Quadruples are not equals after deep copy", q, deepCopy);
    }

}
