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
package fr.inria.eventcloud.validator;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.exceptions.MalformedSparqlQueryException;
import fr.inria.eventcloud.deployment.JunitByClassEventCloudDeployer;

/**
 * Tests filter constraints on some historical queries executed on peers.
 * 
 * @author mantoine
 */
public class FilterValidatorTest extends JunitByClassEventCloudDeployer {

    public FilterValidatorTest() {
        super(1, 10);
    }

    @Test
    public void testExecuteSparqlSelectWithNodeLiteral()
            throws MalformedSparqlQueryException {
        Set<Quadruple> quadruples = new HashSet<Quadruple>();

        Quadruple quadruple =
                new Quadruple(
                        NodeFactory.createURI("http://www.namespace.org/g_abcde"),
                        NodeFactory.createURI("http://www.namespace.org/s_abcde"),
                        NodeFactory.createURI("http://www.namespace.org/predicate_abcde"),
                        NodeFactory.createLiteral("abcde"));
        quadruples.add(quadruple);
        super.getRandomSemanticPeer().add(quadruple);

        ResultSet resultSet =
                super.getRandomSemanticPeer()
                        .executeSparqlSelect(
                                "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o FILTER (?o <= \"zzzzz\")} }")
                        .getResult();

        Binding binding = null;
        Quadruple quad = null;

        Var vars[] = new Var[resultSet.getResultVars().size()];
        for (int i = 0; i < resultSet.getResultVars().size(); i++) {
            vars[i] = Var.alloc(resultSet.getResultVars().get(i));
        }

        int count = 0;
        while (resultSet.hasNext()) {
            binding = resultSet.nextBinding();
            quad =
                    new Quadruple(
                            binding.get(vars[0]), binding.get(vars[1]),
                            binding.get(vars[2]), binding.get(vars[3]));
            Assert.assertTrue(quadruples.contains(quad));
            count++;
        }

        Assert.assertEquals(1, count);
    }

    @Test
    public void testExecuteSparqlSelectWithNodeURI()
            throws MalformedSparqlQueryException {
        Set<Quadruple> quadruples = new HashSet<Quadruple>();

        Quadruple quadruple =
                new Quadruple(
                        NodeFactory.createURI("http://www.namespace.org/g_abcde"),
                        NodeFactory.createURI("http://www.namespace.org/s_abcde"),
                        NodeFactory.createURI("http://www.namespace.org/predicate_abcde"),
                        NodeFactory.createURI("http://www.namespace.org/abcde"));
        quadruples.add(quadruple);
        super.getRandomSemanticPeer().add(quadruple);

        ResultSet resultSet =
                super.getRandomSemanticPeer()
                        .executeSparqlSelect(
                                "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o FILTER (str(?o) <= \"http://www.namespace.org/zzzzz\")} }")
                        .getResult();

        Binding binding = null;
        Quadruple quad = null;

        Var vars[] = new Var[resultSet.getResultVars().size()];
        for (int i = 0; i < resultSet.getResultVars().size(); i++) {
            vars[i] = Var.alloc(resultSet.getResultVars().get(i));
        }

        int count = 0;
        while (resultSet.hasNext()) {
            binding = resultSet.nextBinding();
            quad =
                    new Quadruple(
                            binding.get(vars[0]), binding.get(vars[1]),
                            binding.get(vars[2]), binding.get(vars[3]));
            Assert.assertTrue(quadruples.contains(quad));
            count++;
        }

        Assert.assertEquals(1, count);
    }

}
