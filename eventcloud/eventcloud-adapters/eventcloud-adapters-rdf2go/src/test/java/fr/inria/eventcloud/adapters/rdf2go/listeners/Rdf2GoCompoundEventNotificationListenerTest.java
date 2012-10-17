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
package fr.inria.eventcloud.adapters.rdf2go.listeners;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Assert;
import org.junit.Test;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.impl.jena29.TypeConversion;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.Variable;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;

/**
 * Test cases for {@link Rdf2GoCompoundEventNotificationListener}.
 * 
 * @author lpellegr
 */
public class Rdf2GoCompoundEventNotificationListenerTest {

    @Test
    public void test() {
        Node graph = Node.createURI("http://example.org/graph");

        final List<Quadruple> quadruples = new ArrayList<Quadruple>();
        for (int i = 0; i < 10; i++) {
            quadruples.add(QuadrupleGenerator.random(graph));
        }

        CompoundEvent ce = new CompoundEvent(quadruples);

        final MutableInt nbCalls = new MutableInt();

        Rdf2GoCompoundEventNotificationListener listener =
                new Rdf2GoCompoundEventNotificationListener() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void handle(SubscriptionId id, Model solution) {
                        ClosableIterator<Statement> it =
                                solution.findStatements(solution.createTriplePattern(
                                        Variable.ANY, Variable.ANY,
                                        Variable.ANY));

                        while (it.hasNext()) {
                            Statement stmt = it.next();

                            Quadruple q =
                                    new Quadruple(
                                            TypeConversion.toJenaNode(stmt.getContext()),
                                            TypeConversion.toJenaNode(stmt.getSubject()),
                                            TypeConversion.toJenaNode(stmt.getPredicate()),
                                            TypeConversion.toJenaNode(stmt.getObject()));

                            Assert.assertTrue(quadruples.contains(q));
                        }

                        nbCalls.increment();

                        solution.close();
                    }
                };

        listener.onNotification(new SubscriptionId(), ce);

        Assert.assertEquals(1, (int) nbCalls.getValue());
    }

}
