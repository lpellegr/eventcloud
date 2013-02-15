package fr.inria.eventcloud.validator;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.util.ExprUtils;

import fr.inria.eventcloud.overlay.can.SemanticElement;
import fr.inria.eventcloud.overlay.can.SemanticZone;
import fr.inria.eventcloud.reasoner.AtomicQuery;

public class AtomicQueryConstraintsValidatorTest {

    private AtomicQuery atomicQuery;

    private ExprList exprList;

    private List<ExprList> exprListList;

    private AtomicQueryConstraintsValidator<SemanticElement> validator;

    private SemanticZone semanticZone;

    @Before
    public void setUp() {
        P2PStructuredProperties.CAN_NB_DIMENSIONS.setValue((byte) 4);
        this.semanticZone =
                new SemanticZone(
                        new Coordinate<SemanticElement>(
                                new SemanticElement(
                                        Node.createURI("http://www.graph.fr/eventcloud/begin_graph")),
                                new SemanticElement(
                                        Node.createURI("http://www.subject.fr/eventcloud/begin_subject")),
                                new SemanticElement(
                                        Node.createURI("http://www.predicate.fr/eventcloud/begin_predicate")),
                                new SemanticElement(
                                        Node.createURI("http://www.object.fr/eventcloud/begin_object"))),
                        new Coordinate<SemanticElement>(
                                new SemanticElement(
                                        Node.createURI("http://www.unice.fr/eventcloud/stop_graph")),
                                new SemanticElement(
                                        Node.createURI("http://www.unice.fr/eventcloud/stop_subject")),
                                new SemanticElement(
                                        Node.createURI("http://www.unice.fr/eventcloud/stop_predicate")),
                                new SemanticElement(
                                        Node.createURI("http://www.unice.fr/eventcloud/stop_object"))));

        this.atomicQuery =
                new AtomicQuery(
                        Node.createVariable("g"), Node.createVariable("s"),
                        Node.createVariable("p"), Node.createVariable("o"));
        this.exprList = new ExprList();
        this.exprListList = new ArrayList<ExprList>();
    }

    @Test
    public void andTestFalse() {
        this.exprList.add(ExprUtils.parse("(str(?s) = \"http://www.z.com/resource/aa\") && (\"http://www.namespace.org/test\" > str(?o))"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator =
                new AtomicQueryConstraintsValidator<SemanticElement>(
                        this.atomicQuery);
        boolean res = this.validator.validatesKeyConstraints(this.semanticZone);
        Assert.assertFalse(res);
    }

    @Test
    public void andTestTrue() {
        this.exprList.add(ExprUtils.parse("(str(?s) = \"http://www.z.com/resource/bus\") && (\"http://www.namespace.org/test\" > str(?o))"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator =
                new AtomicQueryConstraintsValidator<SemanticElement>(
                        this.atomicQuery);
        boolean res = this.validator.validatesKeyConstraints(this.semanticZone);
        Assert.assertTrue(res);
    }

    @Test
    public void andOrTest() {
        this.exprList.add(ExprUtils.parse("((\"http://www.namespace.org/test\" > str(?s)) || (\"http://www.unice.fr/test\" > str(?o) && str(?p) = \"http://www.test.org/zoo\") )"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator =
                new AtomicQueryConstraintsValidator<SemanticElement>(
                        this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.semanticZone));
    }

    @Test
    public void equalsTest() {
        this.exprList.add(ExprUtils.parse("str(?p) = \"http://www.predicate.fr/eventcloud/begin_predicate\""));
        this.exprList.add(ExprUtils.parse("\"http://www.predicate.fr/eventcloud/begin_predicate\" = str(?p)"));
        this.exprList.add(ExprUtils.parse("\"http://www.test.com/eventcloud/predicate\" = str(?p)"));
        this.exprList.add(ExprUtils.parse("str(?p) = \"http://www.test.com/eventcloud/predicate\""));
        this.exprList.add(ExprUtils.parse("\"http://www.unice.fr/eventcloud/stop_predicate\" = str(?p)"));
        this.exprList.add(ExprUtils.parse("str(?p) = \"http://www.unice.fr/eventcloud/stop_predicate\""));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator =
                new AtomicQueryConstraintsValidator<SemanticElement>(
                        this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.semanticZone));
    }

    @Test
    public void greatherThanTest() {
        this.exprList.add(ExprUtils.parse("str(?p) > \"http://www.test.com/eventcloud\""));
        this.exprList.add(ExprUtils.parse("\"http://www.test.com/eventcloud\" < str(?p)"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator =
                new AtomicQueryConstraintsValidator<SemanticElement>(
                        this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.semanticZone));
    }

    @Test
    public void greatherThanOrEqualTest() {
        this.exprList.add(ExprUtils.parse("str(?s) >= \"http://www.unice.fr/eventcloud/begin_subject\""));
        this.exprList.add(ExprUtils.parse("\"http://www.unice.fr/eventcloud\" <= str(?s)"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator =
                new AtomicQueryConstraintsValidator<SemanticElement>(
                        this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.semanticZone));
    }

    @Test
    public void lessThanTest() {
        this.exprList.add(ExprUtils.parse("str(?s) < \"http://www.test.com/eventcloud\""));
        this.exprList.add(ExprUtils.parse("\"http://www.test.com/eventcloud\" > str(?s)"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator =
                new AtomicQueryConstraintsValidator<SemanticElement>(
                        this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.semanticZone));
    }

    @Test
    public void lessThanOrEqualTest() {
        this.exprList.add(ExprUtils.parse("str(?o) <= \"http://www.object.fr/eventcloud/begin_object\""));
        this.exprList.add(ExprUtils.parse("\"http://www.object.fr/eventcloud/begin_object\" >= str(?o)"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator =
                new AtomicQueryConstraintsValidator<SemanticElement>(
                        this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.semanticZone));
    }

    @Test
    public void notEqualsTest() {
        this.exprList.add(ExprUtils.parse("str(?p) != \"http://www.predicate.fr/eventcloud/predicate\""));
        this.exprList.add(ExprUtils.parse("\"http://www.predicate.fr/eventcloud/predicate\" != str(?p)"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator =
                new AtomicQueryConstraintsValidator<SemanticElement>(
                        this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.semanticZone));
    }

    @Test
    public void orTest() {
        this.exprList.add(ExprUtils.parse("(((\"http://www.unice.fr/eventcloud/stop_subject\" < str(?s)) || (\"http://www.object.fr/eventcloud/begin_object\" > str(?o) || str(?p) != \"http://www.namespace.org/test\") ) || str(?s) > \"http://www.unice.fr/eventcloud/zoo\")"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator =
                new AtomicQueryConstraintsValidator<SemanticElement>(
                        this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.semanticZone));
    }

}
