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
package fr.inria.eventcloud.pubsub;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.NotificationListenerType;

/**
 * Test cases associated to {@link PublishSubscribeUtils}.
 * 
 * @author lpellegr
 */
public class PublishSubscribeUtilsTest {

    @Test
    public void testRemoveResultVarsExceptGraphVar1() {
        testRemoveResultVarsExceptGraphVar("SELECT ?oddName ?s ?p ?o WHERE { GRAPH ?oddName { ?s ?p ?o }}");
    }

    @Test
    public void testRemoveResultVarsExceptGraphVar2() {
        testRemoveResultVarsExceptGraphVar("SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o }}");
    }

    @Test
    public void testRemoveResultVarsExceptGraphVar3() {
        testRemoveResultVarsExceptGraphVar("SELECT ?g ?p ?o WHERE { GRAPH ?g { <urn:a> ?p ?o }}");
    }

    @Test
    public void testRemoveResultVarsExceptGraphVar4() {
        testRemoveResultVarsExceptGraphVar("SELECT ?g ?s ?o WHERE { GRAPH ?g { ?s <urn:a> ?o }}");
    }

    @Test
    public void testRemoveResultVarsExceptGraphVar5() {
        testRemoveResultVarsExceptGraphVar("SELECT ?g ?p ?o WHERE { GRAPH ?g { ?s ?p <urn:a> }}");
    }

    @Test
    public void testRemoveResultVarsExceptGraphVar6() {
        testRemoveResultVarsExceptGraphVar("SELECT ?g ?s WHERE { GRAPH ?g { ?s <urn:a> <urn:a> }}");
    }

    @Test
    public void testRemoveResultVarsExceptGraphVar7() {
        testRemoveResultVarsExceptGraphVar("SELECT ?g ?p WHERE { GRAPH ?g { <urn:a> ?p <urn:a> }}");
    }

    @Test
    public void testRemoveResultVarsExceptGraphVar8() {
        testRemoveResultVarsExceptGraphVar("SELECT ?g ?o WHERE { GRAPH ?g { <urn:a> <urn:a> ?o }}");
    }

    @Test
    public void testRemoveResultVarsExceptGraphVar9() {
        testRemoveResultVarsExceptGraphVar("SELECT ?g WHERE { GRAPH ?g { <urn:a> <urn:a> <urn:a> }}");
    }

    @Test
    public void testRemoveResultVarsExceptGraphVar10() {
        testRemoveResultVarsExceptGraphVar("SELECT ?a ?b ?c ?g WHERE { GRAPH ?g { ?s ?p ?o }}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveResultVarsExceptGraphVarWithoutGraphVar() {
        testRemoveResultVarsExceptGraphVar("SELECT ?s ?p ?o WHERE { GRAPH <urn:a> { ?s ?p ?o }}");
    }

    private static void testRemoveResultVarsExceptGraphVar(String subscription) {
        Query rewrittenQuery =
                QueryFactory.create(PublishSubscribeUtils.removeResultVarsExceptGraphVar(subscription));

        Assert.assertEquals(1, rewrittenQuery.getResultVars().size());
    }

    @Test
    public void testMatchingCompoundEventSubscription() {
        ImmutableList<Quadruple> quadruples =
                ImmutableList.of(
                        new Quadruple(
                                Node.createURI("urn:g"),
                                Node.createURI("urn:s1"),
                                Node.createURI("urn:p1"),
                                Node.createURI("urn:o1")), new Quadruple(
                                Node.createURI("urn:g"),
                                Node.createURI("urn:s1"),
                                Node.createURI("urn:p2"),
                                Node.createURI("urn:o2")), new Quadruple(
                                Node.createURI("urn:g"),
                                Node.createURI("urn:s1"),
                                Node.createURI("urn:p3"),
                                Node.createURI("urn:o3")), new Quadruple(
                                Node.createURI("urn:g"),
                                Node.createURI("urn:s4"),
                                Node.createURI("urn:p4"),
                                Node.createURI("urn:o4")));

        Subscription subscription =
                new Subscription(
                        new SubscriptionId(),
                        new SubscriptionId(),
                        new SubscriptionId(),
                        System.currentTimeMillis(),
                        System.currentTimeMillis(),
                        "SELECT ?g WHERE { GRAPH ?g { ?a <urn:p1> ?b . ?a <urn:p2> ?c . ?a <urn:p3> ?d } }",
                        null, null, NotificationListenerType.COMPOUND_EVENT);

        String[][] expectedResult =
                new String[][] {
                        {"a", "urn:s1"}, {"b", "urn:o1"}, {"c", "urn:o2"},
                        {"d", "urn:o3"}, {"g", "urn:g"}};

        // test all the permutations from quadruples
        for (List<Quadruple> permutation : Collections2.permutations(quadruples)) {
            CompoundEvent compoundEvent = new CompoundEvent(permutation);

            Binding binding =
                    PublishSubscribeUtils.matches(compoundEvent, subscription);

            assertEquals(binding, expectedResult);
        }
    }

    private static void assertEquals(Binding binding, String[][] expectedResults) {
        Assert.assertNotNull(
                "Binding does not contain the expected number of results",
                binding);

        for (int i = 0; i < expectedResults.length; i++) {
            String var = expectedResults[i][0];
            String expectedResult = expectedResults[i][1];

            Node result = binding.get(Var.alloc(var));

            Assert.assertNotNull(
                    "Binding does not contain a value for variable ?"
                            + expectedResult, result);
            Assert.assertEquals(
                    "Binding does not contain the expected value for variable ?"
                            + var, expectedResult, result.getURI());
        }
    }

}
