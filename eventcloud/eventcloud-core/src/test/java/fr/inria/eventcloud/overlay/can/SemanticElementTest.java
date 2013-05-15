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
package fr.inria.eventcloud.overlay.can;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.graph.NodeFactory;
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
                SemanticElement.removePrefix(NodeFactory.createURI("http://www.inria.fr/sophia/members")));
    }

    @Test
    public void testParseElement2() {
        Assert.assertEquals(
                "members",
                SemanticElement.removePrefix(NodeFactory.createURI("http://www.inria.fr/sophia#members")));
    }

    @Test
    public void testParseElement3() {
        Assert.assertEquals(
                "members",
                SemanticElement.removePrefix(NodeFactory.createURI("http://www.inria.fr/sophia/members/")));
    }

    @Test
    public void testParseElement4() {
        Assert.assertEquals(
                "members",
                SemanticElement.removePrefix(NodeFactory.createURI("http://www.inria.fr/sophia/members#")));
    }

    @Test
    public void testParseElement5() {
        Assert.assertEquals(
                "inria.fr",
                SemanticElement.removePrefix(NodeFactory.createURI("http://www.inria.fr")));
    }

    @Test
    public void testParseElement6() {
        Assert.assertEquals(
                "inria.fr",
                SemanticElement.removePrefix(NodeFactory.createURI("http://www.inria.fr/")));
    }

    @Test
    public void testParseElement7() {
        Assert.assertEquals(
                "bn177",
                SemanticElement.removePrefix(NodeFactory.createAnon(new AnonId(
                        "bn177"))));
    }

    @Test
    public void testParseElement8() {
        Assert.assertEquals(
                "literal",
                SemanticElement.removePrefix(NodeFactory.createLiteral("literal")));
    }

    @Test
    public void testParseElement9() {
        Assert.assertEquals(
                "\u014b\u0001\u0061",
                SemanticElement.removePrefix(NodeFactory.createLiteral("\u014b\u0001\u0061")));
    }

    @Test
    public void testParseElement10() {
        // http://ŋa not a legal URI no transformation is expected
        Assert.assertEquals(
                "\u0068\u0074\u0074\u0070\u003a\u002f\u002f\u014b\u0001\u0061",
                SemanticElement.removePrefix(NodeFactory.createURI("\u0068\u0074\u0074\u0070\u003a\u002f\u002f\u014b\u0001\u0061")));
    }

    @Test
    public void testParseElement11() {
        Assert.assertEquals(
                SemanticElement.EMPTY_STRING_ROUTING_CHARACTER,
                SemanticElement.removePrefix(NodeFactory.createLiteral("")));
    }

    @Test
    public void testParseElement12() {
        Assert.assertEquals(
                "resource",
                SemanticElement.removePrefix(NodeFactory.createURI("resource")));
    }

    @Test
    public void testParseElement13() {
        Assert.assertEquals(
                "fragment",
                SemanticElement.removePrefix(NodeFactory.createURI("./resource#fragment")));
    }

    @Test
    public void testParseElement14() {
        Assert.assertEquals(
                "fragment",
                SemanticElement.removePrefix(NodeFactory.createURI("#fragment")));
    }

    @Test
    public void testParseElement15() {
        Assert.assertEquals(
                "p1",
                SemanticElement.removePrefix(NodeFactory.createURI("urn:p1")));
    }

    @Test
    public void testParseElement16() {
        Assert.assertEquals(
                "096139210x",
                SemanticElement.removePrefix(NodeFactory.createURI("urn:isbn:096139210x")));
    }

    @Test
    public void testParseElement17() {
        Assert.assertEquals(
                "comp.lang.java",
                SemanticElement.removePrefix(NodeFactory.createURI("news:comp.lang.java")));
    }

    @Test
    public void testParseElement18() {
        Assert.assertEquals(
                "user@company.com",
                SemanticElement.removePrefix(NodeFactory.createURI("mailto:user@company.com")));
    }

    @Test
    public void testParseElement19() {
        Assert.assertEquals(
                "nbquads",
                SemanticElement.removePrefix(NodeFactory.createURI("urn:ec:event:nbquads")));
    }

    @Test
    public void testParseElement20() {
        Assert.assertEquals(
                "ec",
                SemanticElement.removePrefix(NodeFactory.createURI("urn:ec")));
    }

    @Test
    public void testParseElement21() {
        Assert.assertEquals(
                "hello",
                SemanticElement.removePrefix(NodeFactory.createURI("urn:ec#hello")));
    }

    @Test
    public void testParseElement22() {
        Assert.assertEquals(
                "urn",
                SemanticElement.removePrefix(NodeFactory.createURI("urn")));
    }

}
