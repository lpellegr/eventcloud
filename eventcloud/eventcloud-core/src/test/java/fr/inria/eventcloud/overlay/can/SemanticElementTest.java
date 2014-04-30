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
package fr.inria.eventcloud.overlay.can;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.rdf.model.AnonId;

import fr.inria.eventcloud.overlay.can.SemanticCoordinate.LoadBalancingDopingFunction;

/**
 * Tests some methods from the {@link SemanticCoordinate} class.
 * 
 * @author lpellegr
 */
public final class SemanticElementTest {

    private static LoadBalancingDopingFunction old;

    @BeforeClass
    public static void setUp() {
        old = SemanticCoordinate.DOPING_FUNCTION;
        SemanticCoordinate.DOPING_FUNCTION =
                SemanticCoordinate.createPrefixesRemovalDopingFunction();
    }

    @Test
    public void testParseElement1() {
        Assert.assertEquals(
                "members",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI("http://www.inria.fr/sophia/members")));
    }

    @Test
    public void testParseElement2() {
        Assert.assertEquals(
                "members",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI("http://www.inria.fr/sophia#members")));
    }

    @Test
    public void testParseElement3() {
        Assert.assertEquals(
                "members",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI("http://www.inria.fr/sophia/members/")));
    }

    @Test
    public void testParseElement4() {
        Assert.assertEquals(
                "members",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI("http://www.inria.fr/sophia/members#")));
    }

    @Test
    public void testParseElement5() {
        Assert.assertEquals(
                "inria.fr",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI("http://www.inria.fr")));
    }

    @Test
    public void testParseElement6() {
        Assert.assertEquals(
                "inria.fr",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI("http://www.inria.fr/")));
    }

    @Test
    public void testParseElement7() {
        Assert.assertEquals(
                "bn177",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createAnon(new AnonId(
                        "bn177"))));
    }

    @Test
    public void testParseElement8() {
        Assert.assertEquals(
                "literal",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createLiteral("literal")));
    }

    @Test
    public void testParseElement9() {
        Assert.assertEquals(
                "\u014b\u0001\u0061",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createLiteral("\u014b\u0001\u0061")));
    }

    @Test
    public void testParseElement10() {
        // http://ŋa not a legal URI no transformation is expected
        Assert.assertEquals(
                "\u0068\u0074\u0074\u0070\u003a\u002f\u002f\u014b\u0001\u0061",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI("\u0068\u0074\u0074\u0070\u003a\u002f\u002f\u014b\u0001\u0061")));
    }

    @Test
    public void testParseElement11() {
        Assert.assertEquals(
                SemanticCoordinate.EMPTY_STRING_ROUTING_CHARACTER,
                SemanticCoordinate.applyDopingFunction(NodeFactory.createLiteral("")));
    }

    @Test
    public void testParseElement12() {
        Assert.assertEquals(
                "resource",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI("resource")));
    }

    @Test
    public void testParseElement13() {
        Assert.assertEquals(
                "fragment",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI("./resource#fragment")));
    }

    @Test
    public void testParseElement14() {
        Assert.assertEquals(
                "fragment",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI("#fragment")));
    }

    @Test
    public void testParseElement15() {
        Assert.assertEquals(
                "p1",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI("urn:p1")));
    }

    @Test
    public void testParseElement16() {
        Assert.assertEquals(
                "096139210x",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI("urn:isbn:096139210x")));
    }

    @Test
    public void testParseElement17() {
        Assert.assertEquals(
                "comp.lang.java",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI("news:comp.lang.java")));
    }

    @Test
    public void testParseElement18() {
        Assert.assertEquals(
                "user@company.com",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI("mailto:user@company.com")));
    }

    @Test
    public void testParseElement19() {
        Assert.assertEquals(
                "nbquads",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI("urn:ec:event:nbquads")));
    }

    @Test
    public void testParseElement20() {
        Assert.assertEquals(
                "ec",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI("urn:ec")));
    }

    @Test
    public void testParseElement21() {
        Assert.assertEquals(
                "hello",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI("urn:ec#hello")));
    }

    @Test
    public void testParseElement22() {
        Assert.assertEquals(
                "urn",
                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI("urn")));
    }

    @AfterClass
    public static void tearDown() {
        SemanticCoordinate.DOPING_FUNCTION = old;
    }

}
