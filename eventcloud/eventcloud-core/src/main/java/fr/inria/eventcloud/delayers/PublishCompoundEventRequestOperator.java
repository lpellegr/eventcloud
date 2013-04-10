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

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.utils.Pair;
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
    public void flushBuffer(CustomBuffer buffer) {
        TransactionalDatasetGraph txnGraph =
                this.overlay.getMiscDatastore().begin(AccessMode.WRITE);

        try {
            // the quadruple is stored by using its meta graph value
            for (ExtendedCompoundEvent ec : buffer.getCompoundEvents()) {
                Quadruple q = ec.getIndexedQuadruple();

                txnGraph.add(
                        q.createMetaGraphNode(), q.getSubject(),
                        q.getPredicate(), q.getObject());
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
    public void triggerAction(CustomBuffer buffer) {
        for (ExtendedCompoundEvent ce : buffer.getCompoundEvents()) {
            this.fireMatchingSubscriptions(
                    ce.compoundEvent, ce.indexQuadrupleUsedForIndexing);
        }
    }

    private void fireMatchingSubscriptions(CompoundEvent compoundEvent,
                                           int indexQuadrupleUsedForIndexing) {
        Quadruple quadruple = compoundEvent.get(indexQuadrupleUsedForIndexing);

        Node metaGraphNode = quadruple.createMetaGraphNode();

        TransactionalDatasetGraph txnGraph =
                this.overlay.getSubscriptionsDatastore().begin(
                        AccessMode.READ_ONLY);

        QueryIterator it = null;
        try {
            Optimize.noOptimizer();

            // finds the subscriptions that have their first sub
            // subscription that matches one of the quadruple contained by
            // the compound event which is published
            it =
                    Algebra.exec(
                            createAlgebraRetrievingSubscriptionsMatching(compoundEvent),
                            txnGraph.getUnderlyingDataset());

            while (it.hasNext()) {
                final Binding binding = it.nextBinding();

                // the identifier of the subscription that is matched is
                // available from the result of the query which has been
                // executed
                SubscriptionId subscriptionId =
                        SubscriptionId.parseSubscriptionId(binding.get(
                                PublishSubscribeConstants.SUBSCRIPTION_ID_VAR)
                                .getLiteralLexicalForm());

                Subscription subscription =
                        this.overlay.findSubscription(txnGraph, subscriptionId);

                boolean mustIgnoreSolution =
                        quadruple.getPublicationTime() < subscription.getIndexationTime();

                if (mustIgnoreSolution) {
                    continue;
                }

                // tries to rewrite the subscription (with the compound
                // event published) that has the first sub subscription that
                // is matched in order to know whether the subscription is
                // fully satisfied or not
                Pair<Binding, Integer> matchingResult =
                        PublishSubscribeUtils.matches(
                                compoundEvent, subscription);

                if (matchingResult.getFirst() != null
                // the quadruple used to route the compound event is the first
                // quadruple matching the subscription
                        && indexQuadrupleUsedForIndexing == matchingResult.getSecond()) {
                    // the overall subscription is verified
                    // we have to notify the subscriber about the solution
                    SubscribeProxy subscriber =
                            subscription.getSubscriberProxy();

                    String source =
                            PAActiveObject.getUrl(this.overlay.getStub());

                    switch (subscription.getType()) {
                        case BINDING:
                            subscriber.receiveSbce3(new BindingNotification(
                                    subscription.getOriginalId(),
                                    metaGraphNode, source,
                                    matchingResult.getFirst()));
                            break;
                        case COMPOUND_EVENT:
                            subscriber.receiveSbce3(new QuadruplesNotification(
                                    subscription.getOriginalId(),
                                    metaGraphNode, source,
                                    ImmutableList.copyOf(compoundEvent)));
                            break;
                        case SIGNAL:
                            subscriber.receiveSbce3(new SignalNotification(
                                    subscription.getOriginalId(),
                                    metaGraphNode, source));
                            break;
                    }

                    log.debug(
                            "Notification sent for graph {} because subscription {} satisfied on peer {}",
                            compoundEvent.getGraph(), subscriptionId,
                            super.overlay.getId());
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

        PublishSubscribeUtils.findAndHandleEphemeralSubscriptions(
                this.overlay, quadruple, metaGraphNode);
    }

    private static Op createAlgebraRetrievingSubscriptionsMatching(CompoundEvent compoundEvent) {
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

        for (Quadruple quad : compoundEvent) {
            NodeValue graphExpr = NodeValue.makeNode(quad.getGraph());

            E_LogicalOr graphConditions = createGraphConditions(graphExpr);

            E_LogicalOr subjectConditions =
                    createSubjectConditions(NodeValue.makeNode(quad.getSubject()));

            E_LogicalOr predicateConditions =
                    createPredicateConditions(NodeValue.makeNode(quad.getPredicate()));

            E_LogicalOr objectConditions =
                    createObjectConditions(NodeValue.makeNode(quad.getObject()));

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
        return new OpProject(
                new OpGraph(PublishSubscribeConstants.GRAPH_VAR, filter),
                Arrays.asList(PublishSubscribeConstants.SUBSCRIPTION_ID_VAR));
    }

    private static E_LogicalOr createGraphConditions(NodeValue graphExpr) {
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

    private static E_LogicalOr createSubjectConditions(NodeValue subjectExpr) {
        return new E_LogicalOr(new E_SameTerm(
                PublishSubscribeConstants.SUBSUBSCRIPTION_SUBJECT_EXPR_VAR,
                subjectExpr), new E_Equals(
                PublishSubscribeConstants.SUBSUBSCRIPTION_SUBJECT_EXPR_VAR,
                PublishSubscribeConstants.SUBSUBSCRIPTION_VARIABLE_EXPR));
    }

    private static E_LogicalOr createPredicateConditions(NodeValue predicateExpr) {
        return new E_LogicalOr(new E_SameTerm(
                PublishSubscribeConstants.SUBSUBSCRIPTION_PREDICATE_EXPR_VAR,
                predicateExpr), new E_Equals(
                PublishSubscribeConstants.SUBSUBSCRIPTION_PREDICATE_EXPR_VAR,
                PublishSubscribeConstants.SUBSUBSCRIPTION_VARIABLE_EXPR));
    }

    private static E_LogicalOr createObjectConditions(NodeValue objectExpr) {
        return new E_LogicalOr(new E_SameTerm(
                PublishSubscribeConstants.SUBSUBSCRIPTION_OBJECT_EXPR_VAR,
                objectExpr), new E_Equals(
                PublishSubscribeConstants.SUBSUBSCRIPTION_OBJECT_EXPR_VAR,
                PublishSubscribeConstants.SUBSUBSCRIPTION_VARIABLE_EXPR));
    }

}
