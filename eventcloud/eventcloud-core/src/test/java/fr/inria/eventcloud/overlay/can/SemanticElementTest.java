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
package fr.inria.eventcloud.overlay.can;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;

/**
 * Tests some methods from the {@link SemanticElement} class.
 * 
 * @author lpellegr
 */
public final class SemanticElementTest {

    @Test
    public void testParseElement1() {
        Assert.assertEquals(
                "members",
                SemanticElement.removePrefix(Node.createURI("http://www.inria.fr/sophia/members")));
    }

    @Test
    public void testParseElement2() {
        Assert.assertEquals(
                "members",
                SemanticElement.removePrefix(Node.createURI("http://www.inria.fr/sophia#members")));
    }

    @Test
    public void testParseElement3() {
        Assert.assertEquals(
                "members",
                SemanticElement.removePrefix(Node.createURI("http://www.inria.fr/sophia/members/")));
    }

    @Test
    public void testParseElement4() {
        Assert.assertEquals(
                "members",
                SemanticElement.removePrefix(Node.createURI("http://www.inria.fr/sophia/members#")));
    }

    @Test
    public void testParseElement5() {
        Assert.assertEquals(
                "inria.fr",
                SemanticElement.removePrefix(Node.createURI("http://www.inria.fr")));
    }

    @Test
    public void testParseElement6() {
        Assert.assertEquals(
                "inria.fr",
                SemanticElement.removePrefix(Node.createURI("http://www.inria.fr/")));
    }

    @Test
    public void testParseElement7() {
        Assert.assertEquals(
                "bn177",
                SemanticElement.removePrefix(Node.createAnon(new AnonId("bn177"))));
    }

    @Test
    public void testParseElement8() {
        Assert.assertEquals(
                "literal",
                SemanticElement.removePrefix(Node.createLiteral("literal")));
    }

    @Test
    public void testParseElement9() {
        Assert.assertEquals(
                "\u014b\u0001\u0061",
                SemanticElement.removePrefix(Node.createLiteral("\u014b\u0001\u0061")));
    }

    @Test
    public void testParseElement10() {
        // http://ŋa not a legal URI no transformation is expected
        Assert.assertEquals(
                "\u0068\u0074\u0074\u0070\u003a\u002f\u002f\u014b\u0001\u0061",
                SemanticElement.removePrefix(Node.createURI("\u0068\u0074\u0074\u0070\u003a\u002f\u002f\u014b\u0001\u0061")));
    }

    @Test
    public void testParseElement11() {
        Assert.assertEquals(
                SemanticElement.EMPTY_STRING_ROUTING_CHARACTER,
                SemanticElement.removePrefix(Node.createLiteral("")));
    }

    @Test
    public void testParseElement12() {
        Assert.assertEquals(
                "resource",
                SemanticElement.removePrefix(Node.createURI("resource")));
    }

    @Test
    public void testParseElement13() {
        Assert.assertEquals(
                "fragment",
                SemanticElement.removePrefix(Node.createURI("./resource#fragment")));
    }

    @Test
    public void testParseElement14() {
        Assert.assertEquals(
                "fragment",
                SemanticElement.removePrefix(Node.createURI("#fragment")));
    }

}
