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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.overlay.can;

import junit.framework.Assert;

import org.junit.Test;

import fr.inria.eventcloud.overlay.can.SemanticElement;

/**
 * Tests some methods from the {@link SemanticHelper} class.
 * 
 * @author lpellegr
 */
public final class SemanticElementTest {

    @Test
    public void testParseTripleElement() {
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
                "test.com", SemanticElement.parseElement("http://test.com"));

        Assert.assertEquals(
                "4t84t203", SemanticElement.parseElement("http://4t84t203"));

    }

}
