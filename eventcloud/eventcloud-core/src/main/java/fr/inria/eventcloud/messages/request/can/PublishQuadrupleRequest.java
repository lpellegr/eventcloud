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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_SameTerm;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.E_StrStartsWith;
import com.hp.hpl.jena.sparql.expr.NodeValue;

import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
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

    private static final long serialVersionUID = 140L;

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

        PublishSubscribeUtils.storeQuadruple(
                semanticOverlay, quadruple, metaGraphNode);

        // finds the sub subscriptions which are stored locally and that are
        // matching the quadruple which have been just inserted into the
        // local datastore
        TransactionalDatasetGraph txnGraph =
                semanticOverlay.getSubscriptionsDatastore().begin(
                        AccessMode.READ_ONLY);

        List<Subscription> subscriptionsMatching =
                new ArrayList<Subscription>();

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
                                PublishSubscribeConstants.SUBSCRIPTION_ID_VAR)
                                .getLiteralLexicalForm());

                Subscription subscription =
                        semanticOverlay.findSubscription(
                                txnGraph, subscriptionId);

                boolean mustIgnoreQuadrupleMatching =
                        quadruple.getPublicationTime() < subscription.getIndexationTime();

                if (log.isDebugEnabled()) {
                    log.debug(
                            "Timestamp comparison, subscriptionTimestamp={}, quadrupleTimestamp={}, quadrupleId={}, quadruple must be ignored? {}",
                            new Object[] {
                                    subscription.getIndexationTime(),
                                    quadruple.getPublicationTime(),
                                    quadruple.getGraph(),
                                    mustIgnoreQuadrupleMatching});
                }

                // if s sent before q but q indexed before s then q must not be
                // notified
                if (!mustIgnoreQuadrupleMatching) {
                    // We have to use an intermediate collection because nested
                    // transactions are currently not allowed (i.e. a write
                    // transaction inside a read) with Jena (or we have to force
                    // the overall transaction to be a write transaction and to
                    // pass the txnGraph variable to the
                    // PublishSubscribeUtils.rewriteSubscriptionOrNotifySender
                    // method. However this implies more contention).
                    subscriptionsMatching.add(subscription);
                }
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
        if (EventCloudProperties.isSbce2PubSubAlgorithmUsed()
                || EventCloudProperties.isSbce3PubSubAlgorithmUsed()) {
            PublishSubscribeUtils.findAndHandleEphemeralSubscriptions(
                    semanticOverlay, quadruple, metaGraphNode);
        }
    }

    private static Op createAlgebraRetrievingSubscriptionsMatching(Quadruple quad) {
        // Basic Graph Pattern
        BasicPattern bp = new BasicPattern();
        bp.add(Triple.create(
                PublishSubscribeConstants.SUBSUBSCRIPTION_SOURCE_VAR,
                PublishSubscribeConstants.SUBSUBSCRIPTION_GRAPH_VALUE_NODE,
                PublishSubscribeConstants.SUBSUBSCRIPTION_GRAPH_VAR));
        bp.add(Triple.create(
                PublishSubscribeConstants.SUBSUBSCRIPTION_SOURCE_VAR,
                PublishSubscribeConstants.SUBSUBSCRIPTION_SUBJECT_VALUE_NODE,
                PublishSubscribeConstants.SUBSUBSCRIPTION_SUBJECT_VAR));
        bp.add(Triple.create(
                PublishSubscribeConstants.SUBSUBSCRIPTION_SOURCE_VAR,
                PublishSubscribeConstants.SUBSUBSCRIPTION_PREDICATE_VALUE_NODE,
                PublishSubscribeConstants.SUBSUBSCRIPTION_PREDICATE_VAR));
        bp.add(Triple.create(
                PublishSubscribeConstants.SUBSUBSCRIPTION_SOURCE_VAR,
                PublishSubscribeConstants.SUBSUBSCRIPTION_OBJECT_VALUE_NODE,
                PublishSubscribeConstants.SUBSUBSCRIPTION_OBJECT_VAR));
        bp.add(Triple.create(
                PublishSubscribeConstants.SUBSUBSCRIPTION_SOURCE_VAR,
                PublishSubscribeConstants.SUBSUBSCRIPTION_ID_NODE,
                PublishSubscribeConstants.SUBSUBSCRIPTION_ID_VAR));
        bp.add(Triple.create(
                PublishSubscribeConstants.SUBSCRIPTION_SOURCE_VAR,
                PublishSubscribeConstants.SUBSCRIPTION_INDEXED_WITH_NODE,
                PublishSubscribeConstants.SUBSUBSCRIPTION_ID_VAR));
        bp.add(Triple.create(
                PublishSubscribeConstants.SUBSCRIPTION_SOURCE_VAR,
                PublishSubscribeConstants.SUBSCRIPTION_ID_NODE,
                PublishSubscribeConstants.SUBSCRIPTION_ID_VAR));

        // Conditions
        NodeValue graphExpr = NodeValue.makeNode(quad.getGraph());

        E_LogicalOr graphConditions =
                new E_LogicalOr(
                        new E_StrStartsWith(
                                new E_Str(
                                        PublishSubscribeConstants.SUBSUBSCRIPTION_GRAPH_EXPR_VAR),
                                graphExpr),
                        new E_LogicalOr(
                                new E_Equals(
                                        PublishSubscribeConstants.SUBSUBSCRIPTION_GRAPH_EXPR_VAR,
                                        PublishSubscribeConstants.SUBSUBSCRIPTION_VARIABLE_EXPR),
                                // the following condition is here for finding
                                // subscriptions which have been rewritten by
                                // using the graph value (but not the meta graph
                                // value) associated to the quadruple which is
                                // matching the subscription
                                new E_Equals(
                                        PublishSubscribeConstants.SUBSUBSCRIPTION_GRAPH_EXPR_VAR,
                                        graphExpr)));

        E_LogicalOr subjectConditions =
                new E_LogicalOr(
                        new E_SameTerm(
                                PublishSubscribeConstants.SUBSUBSCRIPTION_SUBJECT_EXPR_VAR,
                                NodeValue.makeNode(quad.getSubject())),
                        new E_Equals(
                                PublishSubscribeConstants.SUBSUBSCRIPTION_SUBJECT_EXPR_VAR,
                                PublishSubscribeConstants.SUBSUBSCRIPTION_VARIABLE_EXPR));

        E_LogicalOr predicateConditions =
                new E_LogicalOr(
                        new E_SameTerm(
                                PublishSubscribeConstants.SUBSUBSCRIPTION_PREDICATE_EXPR_VAR,
                                NodeValue.makeNode(quad.getPredicate())),
                        new E_Equals(
                                PublishSubscribeConstants.SUBSUBSCRIPTION_PREDICATE_EXPR_VAR,
                                PublishSubscribeConstants.SUBSUBSCRIPTION_VARIABLE_EXPR));

        E_LogicalOr objectConditions =
                new E_LogicalOr(
                        new E_SameTerm(
                                PublishSubscribeConstants.SUBSUBSCRIPTION_OBJECT_EXPR_VAR,
                                NodeValue.makeNode(quad.getObject())),
                        new E_Equals(
                                PublishSubscribeConstants.SUBSUBSCRIPTION_OBJECT_EXPR_VAR,
                                PublishSubscribeConstants.SUBSUBSCRIPTION_VARIABLE_EXPR));

        // Filter based on conditions
        Op filter =
                OpFilter.filter(
                        new E_LogicalAnd(graphConditions, new E_LogicalAnd(
                                subjectConditions, new E_LogicalAnd(
                                        predicateConditions, objectConditions))),
                        new OpBGP(bp));

        // Named Graph + Projection
        return new OpProject(
                new OpGraph(PublishSubscribeConstants.GRAPH_VAR, filter),
                Arrays.asList(PublishSubscribeConstants.SUBSCRIPTION_ID_VAR));
    }

}
