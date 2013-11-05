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
package fr.inria.eventcloud.validator;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.Point;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.util.ExprUtils;

import fr.inria.eventcloud.overlay.can.SemanticCoordinate;
import fr.inria.eventcloud.overlay.can.SemanticZone;
import fr.inria.eventcloud.reasoner.AtomicQuery;

/**
 * Test cases associated to {@link AtomicQueryConstraintsValidator}.
 * 
 * @author mantoine
 */
public class AtomicQueryConstraintsValidatorTest {

    private AtomicQuery atomicQuery;

    private ExprList exprList;

    private List<ExprList> exprListList;

    private AtomicQueryConstraintsValidator validator;

    private SemanticZone semanticZone;

    @Before
    public void setUp() {
        P2PStructuredProperties.CAN_NB_DIMENSIONS.setValue((byte) 4);
        this.semanticZone =
                new SemanticZone(
                        new Point<SemanticCoordinate>(
                                new SemanticCoordinate(
                                        NodeFactory.createURI("http://www.graph.fr/eventcloud/begin_graph")),
                                new SemanticCoordinate(
                                        NodeFactory.createURI("http://www.subject.fr/eventcloud/begin_subject")),
                                new SemanticCoordinate(
                                        NodeFactory.createURI("http://www.predicate.fr/eventcloud/begin_predicate")),
                                new SemanticCoordinate(
                                        NodeFactory.createURI("http://www.object.fr/eventcloud/begin_object"))),
                        new Point<SemanticCoordinate>(
                                new SemanticCoordinate(
                                        NodeFactory.createURI("http://www.unice.fr/eventcloud/stop_graph")),
                                new SemanticCoordinate(
                                        NodeFactory.createURI("http://www.unice.fr/eventcloud/stop_subject")),
                                new SemanticCoordinate(
                                        NodeFactory.createURI("http://www.unice.fr/eventcloud/stop_predicate")),
                                new SemanticCoordinate(
                                        NodeFactory.createURI("http://www.unice.fr/eventcloud/stop_object"))));

        this.atomicQuery =
                new AtomicQuery(
                        NodeFactory.createVariable("g"),
                        NodeFactory.createVariable("s"),
                        NodeFactory.createVariable("p"),
                        NodeFactory.createVariable("o"));
        this.exprList = new ExprList();
        this.exprListList = new ArrayList<ExprList>();
    }

    @Test
    public void andTestFalse() {
        this.exprList.add(ExprUtils.parse("(str(?s) = \"http://www.z.com/resource/aa\") && (\"http://www.namespace.org/test\" > str(?o))"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator = new AtomicQueryConstraintsValidator(this.atomicQuery);
        boolean res = this.validator.validatesKeyConstraints(this.semanticZone);
        Assert.assertFalse(res);
    }

    @Test
    public void andTestTrue() {
        this.exprList.add(ExprUtils.parse("(str(?s) = \"http://www.z.com/resource/bus\") && (\"http://www.namespace.org/test\" > str(?o))"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator = new AtomicQueryConstraintsValidator(this.atomicQuery);
        boolean res = this.validator.validatesKeyConstraints(this.semanticZone);
        Assert.assertTrue(res);
    }

    @Test
    public void andOrTest() {
        this.exprList.add(ExprUtils.parse("((\"http://www.namespace.org/test\" > str(?s)) || (\"http://www.unice.fr/test\" > str(?o) && str(?p) = \"http://www.test.org/zoo\") )"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator = new AtomicQueryConstraintsValidator(this.atomicQuery);
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
        this.validator = new AtomicQueryConstraintsValidator(this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.semanticZone));
    }

    @Test
    public void greatherThanTest() {
        this.exprList.add(ExprUtils.parse("str(?p) > \"http://www.test.com/eventcloud\""));
        this.exprList.add(ExprUtils.parse("\"http://www.test.com/eventcloud\" < str(?p)"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator = new AtomicQueryConstraintsValidator(this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.semanticZone));
    }

    @Test
    public void greatherThanOrEqualTest() {
        this.exprList.add(ExprUtils.parse("str(?s) >= \"http://www.unice.fr/eventcloud/begin_subject\""));
        this.exprList.add(ExprUtils.parse("\"http://www.unice.fr/eventcloud\" <= str(?s)"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator = new AtomicQueryConstraintsValidator(this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.semanticZone));
    }

    @Test
    public void lessThanTest() {
        this.exprList.add(ExprUtils.parse("str(?s) < \"http://www.test.com/eventcloud\""));
        this.exprList.add(ExprUtils.parse("\"http://www.test.com/eventcloud\" > str(?s)"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator = new AtomicQueryConstraintsValidator(this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.semanticZone));
    }

    @Test
    public void lessThanOrEqualTest() {
        this.exprList.add(ExprUtils.parse("str(?o) <= \"http://www.object.fr/eventcloud/begin_object\""));
        this.exprList.add(ExprUtils.parse("\"http://www.object.fr/eventcloud/begin_object\" >= str(?o)"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator = new AtomicQueryConstraintsValidator(this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.semanticZone));
    }

    @Test
    public void notEqualsTest() {
        this.exprList.add(ExprUtils.parse("str(?p) != \"http://www.predicate.fr/eventcloud/predicate\""));
        this.exprList.add(ExprUtils.parse("\"http://www.predicate.fr/eventcloud/predicate\" != str(?p)"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator = new AtomicQueryConstraintsValidator(this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.semanticZone));
    }

    @Test
    public void orTest() {
        this.exprList.add(ExprUtils.parse("(((\"http://www.unice.fr/eventcloud/stop_subject\" < str(?s)) || (\"http://www.object.fr/eventcloud/begin_object\" > str(?o) || str(?p) != \"http://www.namespace.org/test\") ) || str(?s) > \"http://www.unice.fr/eventcloud/zoo\")"));
        this.exprListList.add(this.exprList);
        this.atomicQuery.setFilterConstraints(this.exprListList);
        this.validator = new AtomicQueryConstraintsValidator(this.atomicQuery);
        Assert.assertTrue(this.validator.validatesKeyConstraints(this.semanticZone));
    }

}
