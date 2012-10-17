package fr.inria.eventcloud.validator;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.UnicodeZoneView;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.util.ExprUtils;

import fr.inria.eventcloud.reasoner.AtomicQuery;

public class AtomicQueryConstraintsValidatorTest {

    private Zone zone;

    private AtomicQuery atomicQuery;

    private ExprList exprList;

    private List<ExprList> exprListList;

    private AtomicQueryConstraintsValidator validator;

    @Before
    public void setUp() {
        P2PStructuredProperties.CAN_NB_DIMENSIONS.setValue((byte) 4);
        UnicodeZoneView unicodeZoneView =
                new UnicodeZoneView(
                        new StringCoordinate(
                                new StringElement[] {
                                        new StringElement(
                                                "http://www.graph.fr/eventcloud/graph"),
                                        new StringElement(
                                                "http://www.subject.fr/eventcloud/subject"),
                                        new StringElement(
                                                "http://www.predicate.fr/eventcloud/predicate"),
                                        new StringElement(
                                                "http://www.object.fr/eventcloud/object")}),
                        new StringCoordinate(
                                new StringElement[] {
                                        new StringElement(
                                                "http://www.unice.fr/eventcloud/graph"),
                                        new StringElement(
                                                "http://www.unice.fr/eventcloud/subject"),
                                        new StringElement(
                                                "http://www.unice.fr/eventcloud/predicate"),
                                        new StringElement(
                                                "http://www.unice.fr/eventcloud/object")}));
        this.zone = new Zone(unicodeZoneView, null);
        this.atomicQuery =
                new AtomicQuery(
                        Node.ANY, Node.createVariable("s"),
                        Node.createVariable("p"), Node.createVariable("o"));
        this.exprList = new ExprList();
        this.exprListList = new ArrayList<ExprList>();
    }

    @Test
    public void andTest() {
        this.exprList.add(ExprUtils.parse("(((\"http://www.namespace.org/test\" < ?s) && (\"http://www.unice.fr/test\" >= ?o && ?p != \"http://www.namespace.org/test\") ) && ?s = \"http://www.unice.fr/eventcloud/online\")"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator = new AtomicQueryConstraintsValidator(this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.zone));
    }

    @Test
    public void andOrTest() {
        this.exprList.add(ExprUtils.parse("((\"http://www.namespace.org/test\" > ?s) || (\"http://www.unice.fr/test\" > ?o && ?p = \"http://www.test.org/eventcloud\") )"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator = new AtomicQueryConstraintsValidator(this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.zone));
    }

    @Test
    public void equalsTest() {
        this.exprList.add(ExprUtils.parse("?p = \"http://www.predicate.fr/eventcloud/predicate\""));
        this.exprList.add(ExprUtils.parse("\"http://www.predicate.fr/eventcloud/predicate\" = ?p"));
        this.exprList.add(ExprUtils.parse("\"http://www.test.com/eventcloud/testpredicate\" = ?p"));
        this.exprList.add(ExprUtils.parse("?p = \"http://www.test.com/eventcloud/testpredicate\""));
        this.exprList.add(ExprUtils.parse("\"http://www.unice.fr/eventcloud/predicate\" = ?p"));
        this.exprList.add(ExprUtils.parse("?p = \"http://www.unice.fr/eventcloud/predicate\""));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator = new AtomicQueryConstraintsValidator(this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.zone));
    }

    @Test
    public void greatherThanTest() {
        this.exprList.add(ExprUtils.parse("?p > \"http://www.test.com/eventcloud\""));
        this.exprList.add(ExprUtils.parse("\"http://www.test.com/eventcloud\" < ?p"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator = new AtomicQueryConstraintsValidator(this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.zone));
    }

    @Test
    public void greatherThanOrEqualTest() {
        this.exprList.add(ExprUtils.parse("?s >= \"http://www.unice.fr/eventcloud/subject\""));
        this.exprList.add(ExprUtils.parse("\"http://www.unice.fr/eventcloud/subject\" <= ?s"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator = new AtomicQueryConstraintsValidator(this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.zone));
    }

    @Test
    public void lessThanTest() {
        this.exprList.add(ExprUtils.parse("?s < \"http://www.test.com/eventcloud\""));
        this.exprList.add(ExprUtils.parse("\"http://www.test.com/eventcloud\" > ?s"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator = new AtomicQueryConstraintsValidator(this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.zone));
    }

    @Test
    public void lessThanOrEqualTest() {
        this.exprList.add(ExprUtils.parse("?o <= \"http://www.object.fr/eventcloud/object\""));
        this.exprList.add(ExprUtils.parse("\"http://www.object.fr/eventcloud/object\" >= ?o"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator = new AtomicQueryConstraintsValidator(this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.zone));
    }

    @Test
    public void notEqualsTest() {
        this.exprList.add(ExprUtils.parse("?p != \"http://www.predicate.fr/eventcloud/predicate\""));
        this.exprList.add(ExprUtils.parse("\"http://www.predicate.fr/eventcloud/predicate\" != ?p"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator = new AtomicQueryConstraintsValidator(this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.zone));
    }

    @Test
    public void orTest() {
        this.exprList.add(ExprUtils.parse("(((\"http://www.unice.fr/eventcloud/subject\" < ?s) || (\"http://www.object.fr/eventcloud/object\" > ?o || ?p != \"http://www.namespace.org/test\") ) || ?s > \"http://www.unice.fr/eventcloud/zoo\")"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator = new AtomicQueryConstraintsValidator(this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.zone));
    }

}
