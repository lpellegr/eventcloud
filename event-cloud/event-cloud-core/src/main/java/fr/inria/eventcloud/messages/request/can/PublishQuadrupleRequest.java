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

import java.util.Arrays;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.optimize.Optimize;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.E_Datatype;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_SameTerm;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.E_StrStartsWith;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;

import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.Subscription;

/**
 * Publishes a quadruple into the network. The publish operation consists in
 * storing the quadruple which is published on the peer managing the constraints
 * constituted by the quadruple. After that, an algorithm is triggered to detect
 * whether some subscriptions are matched or not.
 * 
 * @author lpellegr
 */
public class PublishQuadrupleRequest extends QuadrupleRequest {

    private static final long serialVersionUID = 1L;

    private static final Logger log =
            LoggerFactory.getLogger(PublishQuadrupleRequest.class);

    /*
     * Jena variables used to build the algebra
     */
    private static final Var subscriptionIdVar = Var.alloc("sId");

    private static final Var subscriptionSourceVar = Var.alloc("sSrc");

    private static final Var subSubscriptionIdVar = Var.alloc("ssId");

    private static final Var subSubscriptionSourceVar = Var.alloc("ssSrc");

    private static final Var subSubscriptionGraphVar = Var.alloc("ssGraph");

    private static final Var subSubscriptionSubjectVar = Var.alloc("ssSubject");

    private static final Var subSubscriptionPredicateVar =
            Var.alloc("ssPredicate");

    private static final Var subSubscriptionObjectVar = Var.alloc("ssObject");

    public PublishQuadrupleRequest(Quadruple quad) {
        super(quad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestinationReached(final StructuredOverlay overlay,
                                     final Quadruple quadrupleMatching) {
        final TransactionalTdbDatastore datastore =
                ((TransactionalTdbDatastore) overlay.getDatastore());

        TransactionalDatasetGraph txnGraph = datastore.begin(AccessMode.WRITE);

        try {
            // the quadruple is stored by using its timestamped graph value
            txnGraph.add(
                    quadrupleMatching.createMetaGraphNode(),
                    quadrupleMatching.getSubject(),
                    quadrupleMatching.getPredicate(),
                    quadrupleMatching.getObject());
            txnGraph.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }

        // finds the sub subscriptions which are stored locally and that are
        // matching the quadruple which have been just inserted into the
        // local datastore
        txnGraph = datastore.begin(AccessMode.READ_ONLY);

        QueryIterator it = null;
        try {
            Optimize.noOptimizer();
            it =
                    Algebra.exec(
                            createAlgebraRetrievingSubscriptionsMatching(quadrupleMatching),
                            txnGraph.toDataset());

            while (it.hasNext()) {
                final Binding binding = it.nextBinding();

                log.debug(
                        "Peer {} has a sub-subscription that matches the quadruple {} ",
                        overlay, quadrupleMatching);

                // the identifier of the sub subscription that is matched is
                // available from the result of the query which has been
                // executed
                SubscriptionId subscriptionId =
                        SubscriptionId.parseSubscriptionId(binding.get(
                                Var.alloc("sId")).getLiteralLexicalForm());

                Subscription subscription =
                        ((SemanticCanOverlay) overlay).findSubscription(subscriptionId);

                // a subscription with only one sub subscription (that matches
                // the quadruple which has been inserted) has been detected
                PublishSubscribeUtils.rewriteSubscriptionOrNotifySender(
                        (SemanticCanOverlay) overlay, subscription,
                        quadrupleMatching);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (it != null) {
                it.close();
            }
            txnGraph.end();
            Optimize.setFactory(Optimize.stdOptimizationFactory);
        }
    }

    private static Op createAlgebraRetrievingSubscriptionsMatching(Quadruple quad) {
        // Basic Graph Pattern
        BasicPattern bp = new BasicPattern();
        bp.add(Triple.create(
                subSubscriptionSourceVar,
                PublishSubscribeConstants.SUBSUBSCRIPTION_GRAPH_VALUE_NODE,
                subSubscriptionGraphVar));
        bp.add(Triple.create(
                subSubscriptionSourceVar,
                PublishSubscribeConstants.SUBSUBSCRIPTION_SUBJECT_VALUE_NODE,
                subSubscriptionSubjectVar));
        bp.add(Triple.create(
                subSubscriptionSourceVar,
                PublishSubscribeConstants.SUBSUBSCRIPTION_PREDICATE_VALUE_NODE,
                subSubscriptionPredicateVar));
        bp.add(Triple.create(
                subSubscriptionSourceVar,
                PublishSubscribeConstants.SUBSUBSCRIPTION_OBJECT_VALUE_NODE,
                subSubscriptionObjectVar));
        bp.add(Triple.create(
                subSubscriptionSourceVar,
                PublishSubscribeConstants.SUBSUBSCRIPTION_ID_NODE,
                subSubscriptionIdVar));
        bp.add(Triple.create(
                subscriptionSourceVar,
                PublishSubscribeConstants.SUBSCRIPTION_INDEXED_WITH_NODE,
                subSubscriptionIdVar));
        bp.add(Triple.create(
                subscriptionSourceVar,
                PublishSubscribeConstants.SUBSCRIPTION_ID_NODE,
                subscriptionIdVar));

        // Conditions
        NodeValue ssVariableExpr =
                NodeValue.makeNode(PublishSubscribeConstants.SUBSCRIPTION_VARIABLE_NODE);
        ExprVar ssGraphExprVar = new ExprVar(subSubscriptionGraphVar);
        NodeValue graphExpr = NodeValue.makeNode(quad.getGraph());

        E_LogicalOr graphConditions =
                new E_LogicalOr(
                        new E_StrStartsWith(
                                new E_Str(ssGraphExprVar), graphExpr),
                        new E_LogicalOr(new E_Equals(new E_Datatype(
                                ssGraphExprVar), ssVariableExpr),
                        // the following condition is here for finding
                        // subscriptions which have been rewritten by using the
                        // graph value (but not the meta graph value) associated
                        // to the quadruple which is matching the subscription
                                new E_Equals(ssGraphExprVar, graphExpr)));

        ExprVar ssSubjectExprVar = new ExprVar(subSubscriptionSubjectVar);
        E_LogicalOr subjectConditions =
                new E_LogicalOr(
                        new E_SameTerm(
                                ssSubjectExprVar,
                                NodeValue.makeNode(quad.getSubject())),
                        new E_Equals(
                                new E_Datatype(ssSubjectExprVar),
                                ssVariableExpr));

        ExprVar ssPredicateExprVar = new ExprVar(subSubscriptionPredicateVar);
        E_LogicalOr predicateConditions =
                new E_LogicalOr(new E_SameTerm(
                        ssPredicateExprVar,
                        NodeValue.makeNode(quad.getPredicate())), new E_Equals(
                        new E_Datatype(ssPredicateExprVar), ssVariableExpr));

        ExprVar ssObjectExprVar = new ExprVar(subSubscriptionObjectVar);
        E_LogicalOr objectConditions =
                new E_LogicalOr(
                        new E_SameTerm(
                                ssObjectExprVar,
                                NodeValue.makeNode(quad.getObject())),
                        new E_Equals(
                                new E_Datatype(ssObjectExprVar), ssVariableExpr));

        // Filter based on conditions
        Op filter =
                OpFilter.filter(
                        new E_LogicalAnd(graphConditions, new E_LogicalAnd(
                                subjectConditions, new E_LogicalAnd(
                                        predicateConditions, objectConditions))),
                        new OpBGP(bp));

        // Named Graph + Projection
        return new OpProject(
                new OpGraph(
                        PublishSubscribeConstants.SUBSCRIPTION_NS_NODE, filter),
                Arrays.asList(subscriptionIdVar));
    }

}
