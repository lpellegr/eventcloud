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

import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests some methods from the {@link SemanticHelper} class.
 * 
 * @author lpellegr
 */
public final class SemanticElementTest {

    @Test
    public void testParseElement() {
        Assert.assertEquals(
                "members",
                SemanticElement.parseElement("http://www.inria.fr/sophia/members"));

        Assert.assertEquals(
                "members",
                SemanticElement.parseElement("http://www.inria.fr/sophia#members"));

        Assert.assertEquals(
                "members",
                SemanticElement.parseElement("http://www.inria.fr/sophia/members/"));

        Assert.assertEquals(
                "members",
                SemanticElement.parseElement("http://www.inria.fr/sophia/members#"));

        Assert.assertEquals(
                "inria.fr", SemanticElement.parseElement("http://www.inria.fr"));

        Assert.assertEquals(
                "inria.fr",
                SemanticElement.parseElement("http://www.inria.fr/"));

        Assert.assertEquals("bn177", SemanticElement.parseElement("_:bn177"));

        Assert.assertEquals(
                "literal", SemanticElement.parseElement("\"literal\""));

        Assert.assertEquals(
                "\u014b\u0001\u0061",
                SemanticElement.parseElement("\u014b\u0001\u0061"));

        // http://ŋa not a legal URI no transformation is expected
        Assert.assertEquals(
                "\u0068\u0074\u0074\u0070\u003a\u002f\u002f\u014b\u0001\u0061",
                SemanticElement.parseElement("\u0068\u0074\u0074\u0070\u003a\u002f\u002f\u014b\u0001\u0061"));
    }

}
