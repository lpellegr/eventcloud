package fr.inria.eventcloud.util;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests some methods from the {@link SemanticHelper} class.
 * 
 * @author lpellegr
 */
public class SemanticHelperTest {

    @Test
    public void testParseTripleElement() {
        Assert.assertEquals(
                "members",
                SemanticHelper.parseTripleElement("http://www.inria.fr/sophia/members"));

        Assert.assertEquals(
                "members",
                SemanticHelper.parseTripleElement("http://www.inria.fr/sophia#members"));

        Assert.assertEquals(
                "members",
                SemanticHelper.parseTripleElement("http://www.inria.fr/sophia/members/"));

        Assert.assertEquals(
                "members",
                SemanticHelper.parseTripleElement("http://www.inria.fr/sophia/members#"));

        Assert.assertEquals(
                "inria.fr",
                SemanticHelper.parseTripleElement("http://www.inria.fr"));

        Assert.assertEquals(
                "inria.fr",
                SemanticHelper.parseTripleElement("http://www.inria.fr/"));

        Assert.assertEquals(
                "bn177", SemanticHelper.parseTripleElement("_:bn177"));

        Assert.assertEquals(
                "literal", SemanticHelper.parseTripleElement("\"literal\""));

        Assert.assertEquals(
                "test.com",
                SemanticHelper.parseTripleElement("http://test.com"));

        Assert.assertEquals(
                "4t84t203",
                SemanticHelper.parseTripleElement("http://4t84t203"));

    }

}
