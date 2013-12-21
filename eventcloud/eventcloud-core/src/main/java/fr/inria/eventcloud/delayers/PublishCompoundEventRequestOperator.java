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
package fr.inria.eventcloud.delayers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.utils.Pair;
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

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.proxies.SubscribeProxy;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.pubsub.notifications.BindingNotification;
import fr.inria.eventcloud.pubsub.notifications.QuadruplesNotification;
import fr.inria.eventcloud.pubsub.notifications.SignalNotification;

/**
 * Delayer used to buffer write operations due to publications that are
 * published with SBCE3.
 * 
 * @author lpellegr
 */
public class PublishCompoundEventRequestOperator extends
        BufferOperator<CustomBuffer> {

    private static final Logger log =
            LoggerFactory.getLogger(PublishCompoundEventRequestOperator.class);

    public PublishCompoundEventRequestOperator(SemanticCanOverlay overlay) {
        super(overlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void _flushBuffer(CustomBuffer buffer) {
        TransactionalDatasetGraph txnGraph =
                this.overlay.getMiscDatastore().begin(AccessMode.WRITE);

        try {
            // the quadruple is stored by using its meta graph value
            for (ExtendedCompoundEvent extendedCompoundEvent : buffer.getExtendedCompoundEvents()) {
                for (Quadruple q : extendedCompoundEvent.getQuadruplesUsedForIndexing()) {
                    txnGraph.add(
                            q.createMetaGraphNode(), q.getSubject(),
                            q.getPredicate(), q.getObject());
                }
            }
            txnGraph.commit();
        } catch (Exception e) {
            e.printStackTrace();
            txnGraph.abort();
        } finally {
            txnGraph.end();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void _triggerAction(CustomBuffer buffer) {
        this.fireMatchingSubscriptions(buffer.getExtendedCompoundEvents());
    }

    private void fireMatchingSubscriptions(Collection<ExtendedCompoundEvent> extendedCompoundEvents) {
        final TransactionalDatasetGraph txnGraph =
                this.overlay.getSubscriptionsDatastore().begin(
                        AccessMode.READ_ONLY);

        QueryIterator it = null;

        try {
            Optimize.noOptimizer();

            // finds the subscriptions that have their first sub-subscription
            // that matches one of the quadruple contained by the compound
            // event which is published
            it =
                    Algebra.exec(
                            this.createFindSubscriptionsMatchingAlgebra(extendedCompoundEvents),
                            txnGraph.getUnderlyingDataset());

            List<MatchingResult> matchingResults =
                    this.identifyMatchingCompoundEvents(
                            it, extendedCompoundEvents);

            for (final MatchingResult matchingResult : matchingResults) {
                this.threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        CompoundEvent compoundEvent =
                                matchingResult.extendedCompoundEvent.compoundEvent;

                        Quadruple quadruple =
                                matchingResult.extendedCompoundEvent.getQuadruplesUsedForIndexing()[0];

                        final Subscription subscription =
                                PublishCompoundEventRequestOperator.this.overlay.findSubscription(
                                        txnGraph, matchingResult.subscriptionId);

                        if (PublishSubscribeUtils.filteredBySocialFilter(
                                PublishCompoundEventRequestOperator.this.overlay,
                                subscription, quadruple)) {
                            return;
                        }

                        boolean mustIgnoreSolution =
                                quadruple.getPublicationTime() < subscription.getIndexationTime();

                        if (mustIgnoreSolution) {
                            return;
                        }

                        // tries to rewrite the subscription (with the compound
                        // event published) that has the first sub-subscription
                        // that is matched in order to know whether the
                        // subscription is fully satisfied or not
                        Pair<Binding, Integer> result =
                                PublishSubscribeUtils.matches(
                                        compoundEvent, subscription);

                        if (result != null
                        // the quadruple used to route the compound event is the
                        // first quadruple matching the subscription
                                && matchingResult.extendedCompoundEvent.quadrupleIndexesUsedForIndexing.contains(result.getSecond())) {
                            // the overall subscription is verified
                            // we have to notify the subscriber about the
                            // solution
                            String source =
                                    PAActiveObject.getUrl(PublishCompoundEventRequestOperator.this.overlay.getStub());

                            Node metaGraphNode =
                                    quadruple.createMetaGraphNode();

                            try {
                                SubscribeProxy subscriber =
                                        subscription.getSubscriberProxy();

                                switch (subscription.getType()) {
                                    case BINDING:
                                        subscriber.receiveSbce3(new BindingNotification(
                                                subscription.getOriginalId(),
                                                metaGraphNode, source,
                                                result.getFirst()));
                                        break;
                                    case COMPOUND_EVENT:
                                        subscriber.receiveSbce3(new QuadruplesNotification(
                                                subscription.getOriginalId(),
                                                metaGraphNode, source,
                                                compoundEvent));
                                        break;
                                    case SIGNAL:
                                        subscriber.receiveSbce3(new SignalNotification(
                                                subscription.getOriginalId(),
                                                metaGraphNode, source));
                                        break;
                                }

                                log.debug(
                                        "Notification sent for graph {} because subscription {} and triggering condition satisfied on peer {}",
                                        compoundEvent.getGraph(),
                                        matchingResult.subscriptionId,
                                        PublishCompoundEventRequestOperator.this.overlay.getId());
                            } catch (Throwable t) {
                                PublishSubscribeUtils.logSubscribeProxyNotReachable(
                                        metaGraphNode.toString(),
                                        subscription.getOriginalId(),
                                        subscription.getSubscriberUrl());

                                // This could be due to a subscriber which has
                                // left without unsubscribing or a temporary
                                // network outage.
                                // In that case the subscription could be
                                // removed after some attempts and/or time
                                PublishSubscribeUtils.handleSubscriberConnectionFailure(
                                        PublishCompoundEventRequestOperator.this.overlay,
                                        subscription);
                            }
                        } else {
                            if (log.isTraceEnabled()) {
                                String reason;

                                if (result == null) {
                                    reason =
                                            "the subscription is not satisfied, CE="
                                                    + compoundEvent;
                                } else {
                                    reason =
                                            "the triggering notification condition is false: "
                                                    + matchingResult.extendedCompoundEvent.quadrupleIndexesUsedForIndexing
                                                    + " does not contains "
                                                    + result.getSecond();
                                }

                                log.trace(
                                        "Notification not sent for graph {} with subscription {} on peer {} because {}",
                                        compoundEvent.getGraph(),
                                        matchingResult.subscriptionId,
                                        PublishCompoundEventRequestOperator.this.overlay,
                                        reason);
                            }
                        }
                    }
                });

            }

            // looks for ephemeral subscriptions that can be satisfied
            // TODO: remove ephemeral subscriptions thanks to the meta graph
            // values returned by the following method call
            this.findAndHandleEphemeralSubscriptions(
                    txnGraph, extendedCompoundEvents);
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

    private List<Node> findAndHandleEphemeralSubscriptions(TransactionalDatasetGraph txnGraph,
                                                           Collection<ExtendedCompoundEvent> extendedCompoundEvents) {
        Builder<Node> result = ImmutableList.builder();

        try {
            QueryIterator it =
                    Algebra.exec(
                            this.createFindMatchedEphemeralSubscriptionsAlgebra(extendedCompoundEvents),
                            txnGraph.getUnderlyingDataset());

            while (it.hasNext()) {
                Binding binding = it.next();

                Node metaGraphNode =
                        binding.get(PublishSubscribeConstants.GRAPH_VAR);

                for (ExtendedCompoundEvent extendedCompoundEvent : extendedCompoundEvents) {
                    Node compoundEventMetaGraphNode =
                            extendedCompoundEvent.compoundEvent.get(0)
                                    .createMetaGraphNode();

                    if (compoundEventMetaGraphNode.equals(metaGraphNode)) {
                        SubscriptionId subscriptionId =
                                PublishSubscribeUtils.extractSubscriptionId(binding.get(PublishSubscribeConstants.SUBJECT_VAR));

                        String subscriberURL =
                                binding.get(
                                        PublishSubscribeConstants.OBJECT_VAR)
                                        .getURI();

                        final QuadruplesNotification n =
                                new QuadruplesNotification(
                                        subscriptionId,
                                        metaGraphNode,
                                        PAActiveObject.getUrl(this.overlay.getStub()),
                                        extendedCompoundEvent.compoundEvent);

                        try {
                            Subscription.SUBSCRIBE_PROXIES_CACHE.get(
                                    subscriberURL).receiveSbce2(n);
                            result.add(metaGraphNode);
                        } catch (Throwable t) {
                            PublishSubscribeUtils.logSubscribeProxyNotReachable(
                                    metaGraphNode.toString(), subscriptionId,
                                    subscriberURL);
                        }

                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }

        return result.build();
    }

    private Op createFindMatchedEphemeralSubscriptionsAlgebra(Collection<ExtendedCompoundEvent> extendedCompoundEvents) {
        BasicPattern bp = new BasicPattern();
        bp.add(Triple.create(
                PublishSubscribeConstants.SUBJECT_VAR,
                PublishSubscribeConstants.EPHEMERAL_SUBSCRIPTION_SUBSCRIBER_NODE,
                PublishSubscribeConstants.OBJECT_VAR));

        Builder<Binding> bindings = ImmutableList.builder();

        for (ExtendedCompoundEvent extendedCompoundEvent : extendedCompoundEvents) {
            BindingMap binding = new BindingHashMap();
            binding.add(
                    PublishSubscribeConstants.GRAPH_VAR,
                    extendedCompoundEvent.compoundEvent.get(0)
                            .createMetaGraphNode());
            bindings.add(binding);
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

    private List<MatchingResult> identifyMatchingCompoundEvents(QueryIterator it,
                                                                Collection<ExtendedCompoundEvent> extendedCompoundEvents) {
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

            for (ExtendedCompoundEvent extendedCompoundEvent : extendedCompoundEvents) {
                for (Quadruple q : extendedCompoundEvent.compoundEvent) {
                    if (this.matches(q.getObject(), ssObject)
                            && this.matches(q.getPredicate(), ssPredicate)
                            && this.matches(q.getSubject(), ssSubject)
                            && (this.matches(q.getGraph(), ssGraph) || q.getGraph()
                                    .getURI()
                                    .startsWith(ssGraph.getURI()))) {

                        builder.add(new MatchingResult(
                                subscriptionId, extendedCompoundEvent));

                        break;
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

    private Op createFindSubscriptionsMatchingAlgebra(Collection<ExtendedCompoundEvent> compoundEvents) {
        Iterator<ExtendedCompoundEvent> it = compoundEvents.iterator();

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

        while (it.hasNext()) {
            CompoundEvent compoundEvent = it.next().compoundEvent;

            for (Quadruple q : compoundEvent) {
                NodeValue graphExpr = NodeValue.makeNode(q.getGraph());

                E_LogicalOr graphConditions =
                        this.createGraphConditions(graphExpr);

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
        }

        // filter based on conditions
        Op filter = OpFilter.filter(filterConditions, new OpBGP(bp));

        // named graph + projection
        Op result =
                new OpProject(
                        new OpGraph(PublishSubscribeConstants.GRAPH_VAR, filter),
                        Arrays.asList(
                                PublishSubscribeConstants.SUBSCRIPTION_ID_VAR,
                                PublishSubscribeConstants.SUBSUBSCRIPTION_GRAPH_VAR,
                                PublishSubscribeConstants.SUBSUBSCRIPTION_SUBJECT_VAR,
                                PublishSubscribeConstants.SUBSUBSCRIPTION_PREDICATE_VAR,
                                PublishSubscribeConstants.SUBSUBSCRIPTION_OBJECT_VAR));

        return result;
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

    private static final class MatchingResult {

        public final SubscriptionId subscriptionId;

        public final ExtendedCompoundEvent extendedCompoundEvent;

        public MatchingResult(SubscriptionId subscriptionId,
                ExtendedCompoundEvent extendedCompoundEvent) {
            this.subscriptionId = subscriptionId;
            this.extendedCompoundEvent = extendedCompoundEvent;
        }

    }

}
