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
package fr.inria.eventcloud.messages.request.can;

import static fr.inria.eventcloud.pubsub.PublishSubscribeUtils.PUBLICATION_INSERTION_DATETIME_NODE;
import static fr.inria.eventcloud.pubsub.PublishSubscribeUtils.QUADRUPLE_MATCHES_SUBSCRIPTION_NODE;
import static fr.inria.eventcloud.pubsub.PublishSubscribeUtils.SUBSCRIPTION_NS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.util.FmtUtils;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.datastore.JenaDatastore;
import fr.inria.eventcloud.operations.can.RetrieveSubSolutionOperation;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.pubsub.Notification;
import fr.inria.eventcloud.pubsub.NotificationId;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.pubsub.SubscriptionRewriter;
import fr.inria.eventcloud.pubsub.Subsubscription;
import fr.inria.eventcloud.reasoner.AtomicQuery;

/**
 * Request that is used to index a subscription that have been rewritten after
 * the publication of a quadruple.
 * 
 * @author lpellegr
 */
public class IndexRewrittenSubscriptionRequest extends IndexSubscriptionRequest {

    private static final long serialVersionUID = 1L;

    private static final Map<Integer, Var> varIndexes;

    static {
        varIndexes = new HashMap<Integer, Var>(4, 1);
        varIndexes.put(0, Var.alloc("g"));
        varIndexes.put(1, Var.alloc("s"));
        varIndexes.put(2, Var.alloc("p"));
        varIndexes.put(3, Var.alloc("o"));
    }

    public IndexRewrittenSubscriptionRequest(Subscription subscription) {
        super(subscription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPeerValidatingKeyConstraints(CanOverlay overlay,
                                               QuadruplePattern quadruplePattern) {
        // writes the subscription into the cache and the local datastore
        super.onPeerValidatingKeyConstraints(overlay, quadruplePattern);

        JenaDatastore datastore = (JenaDatastore) overlay.getDatastore();

        Subscription subscription = super.subscription.getValue();
        Subsubscription firstSubsubscription =
                super.subscription.getValue().getSubSubscriptions()[0];

        ResultSet result =
                datastore.executeSparqlSelect(createQueryRetrievingQuadruplesMatchingRewrittenSubscription(firstSubsubscription));

        List<Binding> bindings = new ArrayList<Binding>();
        List<Node> quadHashValues = new ArrayList<Node>();

        // cannot iterate on the binding and perform operation on the datastore
        // at the same time
        while (result.hasNext()) {
            Binding binding = result.nextBinding();
            bindings.add(filter(
                    binding, subscription.getResultVars(),
                    firstSubsubscription.getAtomicQuery()));
            quadHashValues.add(binding.get(Var.alloc("h")));
        }

        for (int i = 0; i < bindings.size(); i++) {
            Quadruple matchingQuad =
                    new Quadruple(
                            quadHashValues.get(i),
                            Node.createURI(SUBSCRIPTION_NS
                                    + subscription.getParentId()),
                            QUADRUPLE_MATCHES_SUBSCRIPTION_NODE,
                            Node.createLiteral(
                                    subscription.getId().toString(), null,
                                    XSDDatatype.XSDlong));

            if (subscription.getSubSubscriptions().length == 1) {
                NotificationId notificationId =
                        new NotificationId(
                                subscription.getOriginalId(), System.nanoTime());

                // TODO check if it is ok?
                datastore.delete(matchingQuad);

                // broadcast a message to all the stubs contained by the
                // subscription to say to these peers to send their
                // sub-solutions to the subscriber
                // TODO: send message in parallel
                for (Subscription.Stub stub : subscription.getStubs()) {
                    try {
                        PAActiveObject.lookupActive(
                                SemanticPeer.class, stub.peerUrl).receive(
                                new RetrieveSubSolutionOperation(
                                        notificationId, stub.quadrupleHash));
                    } catch (ActiveObjectCreationException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // sends part of the solution to the subscriber
                // TODO; this operation can be done in parallel with the send
                // RetrieveSubSolutionOperation
                subscription.getSourceStub().receive(
                        new Notification(
                                notificationId,
                                PAActiveObject.getUrl(overlay.getStub()),
                                bindings.get(i)));
            } else {
                // writes a quadruple that indicates that the given quadruple
                // match the given subscription
                datastore.add(matchingQuad);

                Subscription rewrittenSubscription =
                        SubscriptionRewriter.rewrite(
                                subscription, bindings.get(i));

                // stores the url of the stub of the current peer in order to
                // have the possibility to retrieve the quadruple later
                rewrittenSubscription.addStub(new Subscription.Stub(
                        PAActiveObject.getUrl(overlay.getStub()),
                        (Long) quadHashValues.get(i).getLiteralValue()));

                try {
                    overlay.getStub().send(
                            new IndexRewrittenSubscriptionRequest(
                                    rewrittenSubscription));
                } catch (DispatchException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Filters the specified {@code binding} to keep only the variables that are
     * contained by the given{@code resultVars} list.
     * 
     * @param binding
     *            the binding to filter.
     * @param resultVars
     *            the variable to keep.
     * @param atomicQuery
     *            the atomicQuery that is used to filter the variables.
     * 
     * @return a binding which contains only the variables (and their associated
     *         value) from the specified list of variables.
     */
    private Binding filter(Binding binding, Set<Var> resultVars,
                           AtomicQuery atomicQuery) {
        Set<Var> vars =
                Sets.intersection(resultVars, atomicQuery.getVariables());

        Binding newBinding = BindingFactory.create();

        for (Var var : vars) {
            newBinding.add(
                    var,
                    binding.get(varIndexes.get(atomicQuery.getVarIndex(var.getName()))));
        }

        return newBinding;
    }

    private static final String createQueryRetrievingQuadruplesMatchingRewrittenSubscription(Subsubscription s) {
        String[] triplePatternValues = new String[3];
        String[] defaultVariables = {"?g", "?s", "?p", "?o"};
        List<String> resultVariables = new ArrayList<String>();
        resultVariables.add(defaultVariables[0]);

        Node[] nodes = s.getAtomicQuery().toArray();

        for (int i = 1; i < nodes.length; i++) {
            if (nodes[i].isVariable()) {
                resultVariables.add(defaultVariables[i]);
                triplePatternValues[i - 1] = defaultVariables[i];
            } else {
                triplePatternValues[i - 1] = FmtUtils.stringForNode(nodes[i]);
            }
        }

        StringBuilder result = new StringBuilder();
        result.append("SELECT ?h ");
        for (String resultVariable : resultVariables) {
            result.append(resultVariable);
            result.append(" ");
        }
        result.append(" WHERE {\n     GRAPH ?h {\n        ");
        for (String triplePatternValue : triplePatternValues) {
            result.append(triplePatternValue);
            result.append(" ");
        }
        result.append(" .\n        ?g <");
        result.append(PUBLICATION_INSERTION_DATETIME_NODE);
        result.append("> ?insertionTime .\n    }\n}");

        return result.toString();
    }

}
