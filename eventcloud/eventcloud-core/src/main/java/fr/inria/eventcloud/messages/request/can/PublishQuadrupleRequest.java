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
package fr.inria.eventcloud.messages.request.can;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.optimize.Optimize;
import com.hp.hpl.jena.sparql.core.BasicPattern;
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
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.QuadrupleIterator;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.Vars;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.pubsub.notifications.QuadruplesNotification;

/**
 * Publishes a quadruple into the network. The publish operation consists in
 * storing the quadruple which is published on the peer managing the constraints
 * constituted by the quadruple. After that, an algorithm is triggered to detect
 * whether some subscriptions are matched or not.
 * 
 * @author lpellegr
 */
public class PublishQuadrupleRequest extends QuadrupleRequest {

    private static final long serialVersionUID = 130L;

    private static final Logger log =
            LoggerFactory.getLogger(PublishQuadrupleRequest.class);

    public PublishQuadrupleRequest(Quadruple quad) {
        super(quad, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestinationReached(final StructuredOverlay overlay,
                                     final Quadruple quadruple) {
        SemanticCanOverlay semanticOverlay = (SemanticCanOverlay) overlay;

        Node metaGraphNode = quadruple.createMetaGraphNode();

        if (P2PStructuredProperties.ENABLE_BENCHMARKS_INFORMATION.getValue()) {
            log.info("Peer " + overlay + " is about to store quadruple "
                    + quadruple.getSubject() + " " + quadruple.getPredicate()
                    + " " + quadruple.getObject());
        }

        TransactionalDatasetGraph txnGraph =
                semanticOverlay.getMiscDatastore().begin(AccessMode.WRITE);

        try {
            // the quadruple is stored by using its meta graph value
            txnGraph.add(
                    metaGraphNode, quadruple.getSubject(),
                    quadruple.getPredicate(), quadruple.getObject());
            txnGraph.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }

        // finds the sub subscriptions which are stored locally and that are
        // matching the quadruple which have been just inserted into the
        // local datastore
        txnGraph =
                semanticOverlay.getSubscriptionsDatastore().begin(
                        AccessMode.READ_ONLY);

        Set<Subscription> subscriptionsMatching = new HashSet<Subscription>();

        QueryIterator it = null;
        try {
            Optimize.noOptimizer();
            it =
                    Algebra.exec(
                            createAlgebraRetrievingSubscriptionsMatching(quadruple),
                            txnGraph.getUnderlyingDataset());

            while (it.hasNext()) {
                final Binding binding = it.nextBinding();
                log.debug(
                        "Peer {} has a sub subscription that matches the quadruple {} ",
                        overlay, quadruple);

                // the identifier of the sub subscription that is matched is
                // available from the result of the query which has been
                // executed
                SubscriptionId subscriptionId =
                        SubscriptionId.parseSubscriptionId(binding.get(
                                Vars.SUBSCRIPTION_ID).getLiteralLexicalForm());

                Subscription subscription =
                        semanticOverlay.findSubscription(
                                txnGraph, subscriptionId);

                // We have to use an intermediate collection because nested
                // transactions are currently not allowed (i.e. a write
                // transaction inside a read) with Jena (or we have to force the
                // overall transaction to be a write transaction and to pass the
                // txnGraph variable to the
                // PublishSubscribeUtils.rewriteSubscriptionOrNotifySender
                // method. However this implies more contention).
                subscriptionsMatching.add(subscription);
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

        for (Subscription subscriptionMatching : subscriptionsMatching) {
            // a subscription with only one sub subscription (that matches
            // the quadruple which has been inserted) has been detected
            PublishSubscribeUtils.rewriteSubscriptionOrNotifySender(
                    semanticOverlay, subscriptionMatching, quadruple);
        }

        // finds the ephemeral subscriptions that are resolved
        if (EventCloudProperties.isSbce2PubSubAlgorithmUsed()) {
            txnGraph =
                    semanticOverlay.getSubscriptionsDatastore().begin(
                            AccessMode.READ_ONLY);

            try {
                QuadrupleIterator qit =
                        txnGraph.find(new QuadruplePattern(
                                metaGraphNode,
                                null,
                                PublishSubscribeConstants.EPHEMERAL_SUBSCRIPTION_SUBSCRIBER_NODE,
                                null));

                while (qit.hasNext()) {
                    Quadruple q = qit.next();

                    SubscriptionId subscriptionId =
                            PublishSubscribeUtils.extractSubscriptionId(q.getSubject());

                    String subscriberUrl = q.getObject().getURI();

                    final QuadruplesNotification n =
                            new QuadruplesNotification(
                                    subscriptionId,
                                    metaGraphNode,
                                    PAActiveObject.getUrl(semanticOverlay.getStub()),
                                    ImmutableList.of(quadruple));

                    if (EventCloudProperties.PREVENT_CHUNK_DUPLICATES.getValue()
                            && semanticOverlay.markAsSent(n.getId(), quadruple)) {
                        Subscription.SUBSCRIBE_PROXIES_CACHE.get(subscriberUrl)
                                .receive(n);
                    } else if (!EventCloudProperties.PREVENT_CHUNK_DUPLICATES.getValue()) {
                        Subscription.SUBSCRIBE_PROXIES_CACHE.get(subscriberUrl)
                                .receive(n);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                txnGraph.end();
            }
        }
    }

    private static Op createAlgebraRetrievingSubscriptionsMatching(Quadruple quad) {
        // Basic Graph Pattern
        BasicPattern bp = new BasicPattern();
        bp.add(Triple.create(
                Vars.SUBSUBSCRIPTION_SOURCE,
                PublishSubscribeConstants.SUBSUBSCRIPTION_GRAPH_VALUE_NODE,
                Vars.SUBSUBSCRIPTION_GRAPH));
        bp.add(Triple.create(
                Vars.SUBSUBSCRIPTION_SOURCE,
                PublishSubscribeConstants.SUBSUBSCRIPTION_SUBJECT_VALUE_NODE,
                Vars.SUBSUBSCRIPTION_SUBJECT));
        bp.add(Triple.create(
                Vars.SUBSUBSCRIPTION_SOURCE,
                PublishSubscribeConstants.SUBSUBSCRIPTION_PREDICATE_VALUE_NODE,
                Vars.SUBSUBSCRIPTION_PREDICATE));
        bp.add(Triple.create(
                Vars.SUBSUBSCRIPTION_SOURCE,
                PublishSubscribeConstants.SUBSUBSCRIPTION_OBJECT_VALUE_NODE,
                Vars.SUBSUBSCRIPTION_OBJECT));
        bp.add(Triple.create(
                Vars.SUBSUBSCRIPTION_SOURCE,
                PublishSubscribeConstants.SUBSUBSCRIPTION_ID_NODE,
                Vars.SUBSUBSCRIPTION_ID));
        bp.add(Triple.create(
                Vars.SUBSCRIPTION_SOURCE,
                PublishSubscribeConstants.SUBSCRIPTION_INDEXED_WITH_NODE,
                Vars.SUBSUBSCRIPTION_ID));
        bp.add(Triple.create(
                Vars.SUBSCRIPTION_SOURCE,
                PublishSubscribeConstants.SUBSCRIPTION_ID_NODE,
                Vars.SUBSCRIPTION_ID));

        // Conditions
        NodeValue ssVariableExpr =
                NodeValue.makeNode(PublishSubscribeConstants.SUBSCRIPTION_VARIABLE_NODE);
        ExprVar ssGraphExprVar = new ExprVar(Vars.SUBSUBSCRIPTION_GRAPH);
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

        ExprVar ssSubjectExprVar = new ExprVar(Vars.SUBSUBSCRIPTION_SUBJECT);
        E_LogicalOr subjectConditions =
                new E_LogicalOr(
                        new E_SameTerm(
                                ssSubjectExprVar,
                                NodeValue.makeNode(quad.getSubject())),
                        new E_Equals(
                                new E_Datatype(ssSubjectExprVar),
                                ssVariableExpr));

        ExprVar ssPredicateExprVar =
                new ExprVar(Vars.SUBSUBSCRIPTION_PREDICATE);
        E_LogicalOr predicateConditions =
                new E_LogicalOr(new E_SameTerm(
                        ssPredicateExprVar,
                        NodeValue.makeNode(quad.getPredicate())), new E_Equals(
                        new E_Datatype(ssPredicateExprVar), ssVariableExpr));

        ExprVar ssObjectExprVar = new ExprVar(Vars.SUBSUBSCRIPTION_OBJECT);
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
                new OpGraph(Vars.GRAPH, filter),
                Arrays.asList(Vars.SUBSCRIPTION_ID));
    }

}
