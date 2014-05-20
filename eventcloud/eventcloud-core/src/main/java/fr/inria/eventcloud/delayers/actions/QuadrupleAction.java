/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.delayers.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.api.PAActiveObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpTable;
import com.hp.hpl.jena.sparql.algebra.optimize.Optimize;
import com.hp.hpl.jena.sparql.algebra.table.TableData;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_SameTerm;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.E_StrStartsWith;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;

import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.delayers.Delayer;
import fr.inria.eventcloud.delayers.buffers.Buffer;
import fr.inria.eventcloud.delayers.buffers.QuadrupleBuffer;
import fr.inria.eventcloud.exceptions.DecompositionException;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.pubsub.notifications.QuadruplesNotification;
import fr.inria.eventcloud.reasoner.AtomicQuery;

/**
 * Action used by a delayer to detect satisfied subscriptions and rewritten
 * subscriptions or notification solutions.
 * 
 * @author lpellegr
 * 
 * @see Delayer
 * @see QuadrupleBuffer
 */
public final class QuadrupleAction extends Action<Quadruple> {

    private static final Logger LOG =
            LoggerFactory.getLogger(QuadrupleAction.class);

    public QuadrupleAction(SemanticCanOverlay overlay, int threadPoolSize) {
        super(overlay, threadPoolSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void perform(Buffer<Quadruple> buffer) {
        this.fireMatchingSubscriptions((QuadrupleBuffer) buffer);
    }

    private void fireMatchingSubscriptions(QuadrupleBuffer quadruples) {
        // finds the sub-subscriptions which are stored locally and that are
        // matching the quadruple which have been just inserted into the
        // local datastore
        TransactionalDatasetGraph txnGraph;

        QueryIterator it = null;
        List<MatchingResult> matchingResults = Collections.emptyList();

        // meta-quadruples are not necessary to find the subscriptions that are
        // matched
        List<Quadruple> nonMetaQuadruples = quadruples.getNonMetaQuadruples();

        if (!nonMetaQuadruples.isEmpty()) {
            txnGraph =
                    super.overlay.getSubscriptionsDatastore().begin(
                            AccessMode.READ_ONLY);

            try {
                Optimize.noOptimizer();
                it =
                        Algebra.exec(
                                this.createFindSubscriptionsMatchingAlgebra(nonMetaQuadruples),
                                txnGraph.getUnderlyingDataset());

                matchingResults =
                        this.identifySubscriptionsMatched(
                                txnGraph, it, nonMetaQuadruples);

                Iterator<MatchingResult> matchingResultsIterator =
                        matchingResults.iterator();

                while (matchingResultsIterator.hasNext()) {
                    MatchingResult matchingResult =
                            matchingResultsIterator.next();
                    Quadruple quadruple = matchingResult.quadruple;
                    Subscription subscription = matchingResult.subscription;

                    LOG.debug(
                            "Peer {} has a sub subscription that matches the quadruple {} ",
                            super.overlay, quadruple);

                    boolean mustIgnoreQuadrupleMatching =
                            quadruple.getPublicationTime() < subscription.getIndexationTime();

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                                "Timestamp comparison, subscriptionTimestamp={}, quadrupleTimestamp={}, quadrupleId={}, quadruple must be ignored? {}",
                                subscription.getIndexationTime(),
                                quadruple.getPublicationTime(),
                                quadruple.getGraph(),
                                mustIgnoreQuadrupleMatching);
                    }

                    // if s sent before q but q indexed before s then q must not
                    // be notified
                    if (mustIgnoreQuadrupleMatching) {
                        matchingResultsIterator.remove();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                txnGraph.abort();
            } finally {
                if (it != null) {
                    it.close();
                }
                txnGraph.end();
                Optimize.setFactory(Optimize.stdOptimizationFactory);
            }

            for (MatchingResult matchingResult : matchingResults) {
                // TODO each quadrupleMatching could be handled in parallel to
                // another by using the thread pool from the super class but
                // doing so requires to check that no new concurrent issue
                // occurs

                // a subscription with only one sub-subscription (that matches
                // the quadruple which has been inserted) has been detected
                PublishSubscribeUtils.rewriteSubscriptionOrNotifySender(
                        super.overlay, matchingResult.subscription,
                        matchingResult.quadruple);
            }
        }

        // finds the ephemeral subscriptions that are satisfied
        if (EventCloudProperties.isSbce2PubSubAlgorithmUsed()
                || EventCloudProperties.isSbce3PubSubAlgorithmUsed()) {
            txnGraph =
                    super.overlay.getSubscriptionsDatastore().begin(
                            AccessMode.READ_ONLY);

            try {
                // the quadruple list contains meta and non meta-quadruples
                this.findAndHandleEphemeralSubscriptions(txnGraph, quadruples);
            } catch (Exception e) {
                e.printStackTrace();
                txnGraph.abort();
            } finally {
                txnGraph.end();
            }
        }
    }

    private void findAndHandleEphemeralSubscriptions(TransactionalDatasetGraph txnGraph,
                                                     Buffer<Quadruple> quadruples) {
        try {
            QueryIterator it =
                    Algebra.exec(
                            this.createFindMatchedEphemeralSubscriptionsAlgebra(quadruples),
                            txnGraph.getUnderlyingDataset());

            while (it.hasNext()) {
                Binding binding = it.next();

                Node ephemeralSubscriptionMetaGraphNode =
                        binding.get(PublishSubscribeConstants.GRAPH_VAR);

                boolean hasSolutions = false;
                Builder<Quadruple> matchingQuadruples = ImmutableList.builder();

                for (Quadruple quadruple : quadruples) {
                    Node quadrupleMetaGraphNode =
                            quadruple.createMetaGraphNode();

                    if (quadrupleMetaGraphNode.equals(ephemeralSubscriptionMetaGraphNode)) {
                        hasSolutions = true;
                        matchingQuadruples.add(quadruple);
                    }
                }

                if (hasSolutions) {
                    SubscriptionId subscriptionId =
                            PublishSubscribeUtils.extractSubscriptionId(binding.get(PublishSubscribeConstants.SUBJECT_VAR));

                    String subscriberURL =
                            binding.get(PublishSubscribeConstants.OBJECT_VAR)
                                    .getURI();

                    final QuadruplesNotification n =
                            new QuadruplesNotification(
                                    subscriptionId,
                                    ephemeralSubscriptionMetaGraphNode,
                                    PAActiveObject.getUrl(this.overlay.getStub()),
                                    matchingQuadruples.build());

                    Subscription.SUBSCRIBE_PROXIES_CACHE.get(subscriberURL)
                            .receiveSbce2(n);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }
    }

    private Op createFindMatchedEphemeralSubscriptionsAlgebra(Buffer<Quadruple> quadruples) {
        BasicPattern bp = new BasicPattern();
        bp.add(Triple.create(
                PublishSubscribeConstants.SUBJECT_VAR,
                PublishSubscribeConstants.EPHEMERAL_SUBSCRIPTION_SUBSCRIBER_NODE,
                PublishSubscribeConstants.OBJECT_VAR));

        Builder<Binding> bindings = ImmutableList.builder();

        Set<Node> metaGraphNodesAlreadyUsed =
                new HashSet<Node>(quadruples.size(), 1);

        for (Quadruple quadruple : quadruples) {
            Node metaGraphNode = quadruple.createMetaGraphNode();

            // we may have several quadruples from a same CE that share the same
            // graph value
            if (metaGraphNodesAlreadyUsed.add(metaGraphNode)) {
                BindingMap binding = new BindingHashMap();
                binding.add(
                        PublishSubscribeConstants.GRAPH_VAR,
                        quadruple.createMetaGraphNode());
                bindings.add(binding);
            }
        }

        Table table =
                new TableData(
                        ImmutableList.of(PublishSubscribeConstants.GRAPH_VAR),
                        bindings.build());

        Op result =
                new OpGraph(PublishSubscribeConstants.GRAPH_VAR, new OpBGP(bp));
        result = OpJoin.create(result, OpTable.create(table));
        result =
                new OpProject(result, ImmutableList.of(
                        PublishSubscribeConstants.GRAPH_VAR,
                        PublishSubscribeConstants.SUBJECT_VAR,
                        PublishSubscribeConstants.OBJECT_VAR));

        return result;
    }

    private List<MatchingResult> identifySubscriptionsMatched(TransactionalDatasetGraph txnGraph,
                                                              QueryIterator it,
                                                              List<Quadruple> quadruples) {
        Builder<MatchingResult> builder = ImmutableList.builder();

        while (it.hasNext()) {
            final Binding binding = it.nextBinding();

            Node ssGraph =
                    binding.get(PublishSubscribeConstants.SUBSUBSCRIPTION_GRAPH_VAR);
            Node ssSubject =
                    binding.get(PublishSubscribeConstants.SUBSUBSCRIPTION_SUBJECT_VAR);
            Node ssPredicate =
                    binding.get(PublishSubscribeConstants.SUBSUBSCRIPTION_PREDICATE_VAR);
            Node ssObject =
                    binding.get(PublishSubscribeConstants.SUBSUBSCRIPTION_OBJECT_VAR);

            SubscriptionId subscriptionId =
                    SubscriptionId.parseSubscriptionId(binding.get(
                            PublishSubscribeConstants.SUBSCRIPTION_ID_VAR)
                            .getLiteralLexicalForm());

            Subscription subscription =
                    this.overlay.findSubscription(txnGraph, subscriptionId);

            for (Quadruple q : quadruples) {
                if (this.matches(q.getObject(), ssObject)
                        && this.matches(q.getPredicate(), ssPredicate)
                        && this.matches(q.getSubject(), ssSubject)
                        && (this.matches(q.getGraph(), ssGraph) || q.getGraph()
                                .getURI()
                                .startsWith(ssGraph.getURI()))) {

                    AtomicQuery firstSSAQ = null;
                    try {
                        firstSSAQ =
                                subscription.getSubSubscriptions()[0].getAtomicQuery();
                    } catch (DecompositionException e) {
                        throw new IllegalStateException(e);
                    }

                    if (firstSSAQ.isFilterEvaluationRequired()) {
                        if (PublishSubscribeUtils.matches(q, firstSSAQ) != null) {
                            builder.add(new MatchingResult(subscription, q));
                        }
                    } else {
                        builder.add(new MatchingResult(subscription, q));
                    }
                }
            }
        }

        return builder.build();
    }

    private boolean matches(Node publicationTerm, Node subscriptionTerm) {
        return subscriptionTerm.equals(PublishSubscribeConstants.SUBSCRIPTION_VARIABLE_NODE)
                || publicationTerm.equals(subscriptionTerm);
    }

    private Op createFindSubscriptionsMatchingAlgebra(List<Quadruple> quadruples) {

        // basic graph pattern
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

        // conditions
        Expr filterConditions = null;

        for (Quadruple q : quadruples) {
            NodeValue graphExpr = NodeValue.makeNode(q.getGraph());

            E_LogicalOr graphConditions = this.createGraphConditions(graphExpr);

            E_LogicalOr subjectConditions =
                    this.createSubjectConditions(NodeValue.makeNode(q.getSubject()));

            E_LogicalOr predicateConditions =
                    this.createPredicateConditions(NodeValue.makeNode(q.getPredicate()));

            E_LogicalOr objectConditions =
                    this.createObjectConditions(NodeValue.makeNode(q.getObject()));

            Expr conditions =
                    new E_LogicalAnd(graphConditions, new E_LogicalAnd(
                            subjectConditions, new E_LogicalAnd(
                                    predicateConditions, objectConditions)));

            if (filterConditions == null) {
                filterConditions = conditions;
            } else {
                filterConditions =
                        new E_LogicalOr(filterConditions, conditions);
            }
        }

        // filter based on conditions
        Op filter = OpFilter.filter(filterConditions, new OpBGP(bp));

        // named graph + projection
        return new OpProject(new OpGraph(
                PublishSubscribeConstants.GRAPH_VAR, filter), Arrays.asList(
                PublishSubscribeConstants.SUBSCRIPTION_ID_VAR,
                PublishSubscribeConstants.SUBSUBSCRIPTION_GRAPH_VAR,
                PublishSubscribeConstants.SUBSUBSCRIPTION_SUBJECT_VAR,
                PublishSubscribeConstants.SUBSUBSCRIPTION_PREDICATE_VAR,
                PublishSubscribeConstants.SUBSUBSCRIPTION_OBJECT_VAR));
    }

    private E_LogicalOr createGraphConditions(NodeValue graphExpr) {
        return new E_LogicalOr(
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
                        // using the graph value (but not the meta graph value)
                        // associated to the quadruple which is matching the
                        // subscription
                        new E_Equals(
                                PublishSubscribeConstants.SUBSUBSCRIPTION_GRAPH_EXPR_VAR,
                                graphExpr)));
    }

    private E_LogicalOr createSubjectConditions(NodeValue subjectExpr) {
        return new E_LogicalOr(new E_SameTerm(
                PublishSubscribeConstants.SUBSUBSCRIPTION_SUBJECT_EXPR_VAR,
                subjectExpr), new E_Equals(
                PublishSubscribeConstants.SUBSUBSCRIPTION_SUBJECT_EXPR_VAR,
                PublishSubscribeConstants.SUBSUBSCRIPTION_VARIABLE_EXPR));
    }

    private E_LogicalOr createPredicateConditions(NodeValue predicateExpr) {
        return new E_LogicalOr(new E_SameTerm(
                PublishSubscribeConstants.SUBSUBSCRIPTION_PREDICATE_EXPR_VAR,
                predicateExpr), new E_Equals(
                PublishSubscribeConstants.SUBSUBSCRIPTION_PREDICATE_EXPR_VAR,
                PublishSubscribeConstants.SUBSUBSCRIPTION_VARIABLE_EXPR));
    }

    private E_LogicalOr createObjectConditions(NodeValue objectExpr) {
        return new E_LogicalOr(new E_SameTerm(
                PublishSubscribeConstants.SUBSUBSCRIPTION_OBJECT_EXPR_VAR,
                objectExpr), new E_Equals(
                PublishSubscribeConstants.SUBSUBSCRIPTION_OBJECT_EXPR_VAR,
                PublishSubscribeConstants.SUBSUBSCRIPTION_VARIABLE_EXPR));
    }

    private static class MatchingResult {

        public final Subscription subscription;

        public final Quadruple quadruple;

        public MatchingResult(Subscription subscription, Quadruple quadruple) {
            this.subscription = subscription;
            this.quadruple = quadruple;
        }

    }

}
