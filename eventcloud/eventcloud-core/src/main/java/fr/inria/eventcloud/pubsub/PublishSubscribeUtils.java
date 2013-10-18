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
package fr.inria.eventcloud.pubsub;

import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_NS;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_NS;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.mutable.MutableObject;
import org.apache.jena.riot.out.NodeFmtLib;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.algebra.TransformBase;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.tdb.TDBFactory;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.NotificationListenerType;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.exceptions.DecompositionException;
import fr.inria.eventcloud.messages.request.IndexEphemeralSubscriptionRequest;
import fr.inria.eventcloud.messages.request.IndexSubscriptionRequest;
import fr.inria.eventcloud.messages.request.UnsubscribeRequest;
import fr.inria.eventcloud.operations.can.RetrieveSubSolutionOperation;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.proxies.SubscribeProxy;
import fr.inria.eventcloud.pubsub.notifications.BindingNotification;
import fr.inria.eventcloud.pubsub.notifications.PollingSignalNotification;
import fr.inria.eventcloud.pubsub.notifications.QuadruplesNotification;
import fr.inria.eventcloud.pubsub.notifications.SignalNotification;
import fr.inria.eventcloud.reasoner.AtomicQuery;
import fr.inria.eventcloud.utils.SparqlResultSerializer;

/**
 * Some utility methods for the publish/subscribe algorithm.
 * 
 * @author lpellegr
 */
public final class PublishSubscribeUtils {

    private static final Logger log =
            LoggerFactory.getLogger(PublishSubscribeUtils.class);

    private PublishSubscribeUtils() {

    }

    /**
     * Creates an intermediate quadruple result. This is a quadruple that embeds
     * the intermediate values associated to a notification to send to a
     * subscriber. The quadruple generated contains the variables that are
     * matched and their associated value.
     * 
     * @param subscription
     *            the subscription which is matched by the specified quadruple.
     * @param quadruple
     *            the quadruple matching the subscription.
     * 
     * @return a quadruple embeds the intermediate values associated to a
     *         notification to send to a subscriber
     */
    public static final Quadruple createIntermediateQuadrupleResult(Subscription subscription,
                                                                    Quadruple quadruple) {
        Subsubscription firstSubSubscription;

        try {
            firstSubSubscription = subscription.getSubSubscriptions()[0];
        } catch (DecompositionException e) {
            throw new IllegalStateException(e);
        }

        AtomicQuery atomicQuery = firstSubSubscription.getAtomicQuery();

        StringBuilder objectValue = new StringBuilder();

        // generates the object value which is the concatenation of
        // the variables that are matched and their associated values
        for (int i = 0; i < 4; i++) {
            Node node = atomicQuery.getNode(i);

            // keep only variables that are part of the result vars set
            if (node.isVariable()
                    && subscription.getResultVars().contains(
                            Var.alloc(node.getName()))) {
                objectValue.append(node.getName());
                objectValue.append('=');
                objectValue.append(FmtUtils.stringForNode(quadruple.getTermByIndex(i)));
                objectValue.append(',');
            }
        }

        Quadruple q =
                new Quadruple(
                        createQuadrupleHashUri(quadruple),
                        NodeFactory.createURI(subscription.getSubscriberUrl()),
                        PublishSubscribeConstants.INTERMEDIATE_RESULTS_NODE,
                        NodeFactory.createLiteral(objectValue.substring(
                                0, objectValue.length() - 1)));

        return q;
    }

    /**
     * Finds the subscription identifiers that are issue from the specified
     * original subscription identifier (several rewritten subscriptions may be
     * issued from the same original subscription).
     * 
     * @return the subscription identifiers that are issue from the specified
     *         original subscription identifier (several rewritten subscriptions
     *         may be issued from the same original subscription).
     */
    public static final List<SubscriptionId> findSubscriptionIds(TransactionalTdbDatastore datastore,
                                                                 SubscriptionId originalSubscriptionId) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append(PublishSubscribeConstants.SUBSCRIPTION_ID_VAR.toString());
        query.append(" WHERE {\n    GRAPH ");
        query.append(NodeFmtLib.str(PublishSubscribeConstants.SUBSCRIPTION_NS_NODE));
        query.append(" {\n        ");
        query.append("?sIdUri ");
        query.append(NodeFmtLib.str(PublishSubscribeConstants.SUBSCRIPTION_ORIGINAL_ID_NODE));
        query.append(' ');
        query.append(NodeFmtLib.str(originalSubscriptionId.toJenaNode()));
        query.append(" .\n        ?sIdUri ");
        query.append(NodeFmtLib.str(PublishSubscribeConstants.SUBSCRIPTION_ID_NODE));
        query.append(' ');
        query.append(PublishSubscribeConstants.SUBSCRIPTION_ID_VAR.toString());
        query.append(" .\n    }\n}");

        List<SubscriptionId> ids = new ArrayList<SubscriptionId>();

        TransactionalDatasetGraph txnGraph =
                datastore.begin(AccessMode.READ_ONLY);

        QueryExecution qExec = null;
        try {
            qExec =
                    QueryExecutionFactory.create(
                            query.toString(), txnGraph.getUnderlyingDataset());
            ResultSet result = qExec.execSelect();

            while (result.hasNext()) {
                Binding binding = result.nextBinding();

                SubscriptionId subscriptionId =
                        SubscriptionId.parseSubscriptionId(binding.get(
                                PublishSubscribeConstants.SUBSCRIPTION_ID_VAR)
                                .getLiteralLexicalForm());
                ids.add(subscriptionId);
            }
        } finally {
            if (qExec != null) {
                qExec.close();
            }
            txnGraph.end();
        }

        return ids;
    }

    /**
     * Creates a quadruple hash URI by using the specified {@code quad}.
     * 
     * @param quad
     *            the quadruple to use.
     * 
     * @return the quadruple hash URI as a Jena {@link Node_URI}.
     */
    public static final Node createQuadrupleHashUri(Quadruple quad) {
        return createQuadrupleHashUri(quad.hashValue());
    }

    /**
     * Creates the quadruple hash URI by using the specified {@code quadHash}
     * which is assumed to be the hash value associated to a {@link Quadruple}.
     * 
     * @param quadHash
     *            the hash value to use.
     * 
     * @return a quadruple hash URI as a Jena {@link Node_URI}.
     */
    public static final Node createQuadrupleHashUri(HashCode quadHash) {
        return NodeFactory.createURI(PublishSubscribeConstants.QUADRUPLE_NS.concat(quadHash.toString()));
    }

    /**
     * Creates a subscription id URI from the specified {@code id}.
     * 
     * @param id
     *            the subscription identifier to use.
     * 
     * @return the subscription id URI as a Jena {@link Node_URI}.
     */
    public static final Node createSubscriptionIdUri(SubscriptionId id) {
        return createSubscriptionIdUri(id.toString());
    }

    /**
     * Creates a sub subscription id URI from the specified
     * {@code subscriptionId} which is assumed to be a {@link SubscriptionId}.
     * 
     * @param subSubscriptionId
     *            the subscription identifier to use.
     * 
     * @return the sub subscription id URI as a Jena {@link Node_URI}.
     */
    public static final Node createSubSubscriptionIdUri(String subSubscriptionId) {
        return NodeFactory.createURI(SUBSUBSCRIPTION_NS + subSubscriptionId);
    }

    /**
     * Creates a sub subscription id URI from the specified {@code id}.
     * 
     * @param subSubscriptionId
     *            the sub subscription identifier to use.
     * 
     * @return the sub subscription id URI as a Jena {@link Node_URI}.
     */
    public static final Node createSubSubscriptionIdUri(SubscriptionId subSubscriptionId) {
        return createSubSubscriptionIdUri(subSubscriptionId.toString());
    }

    /**
     * Creates a subscription id URI from the specified {@code subscriptionId}
     * which is assumed to be a {@link SubscriptionId}.
     * 
     * @param subscriptionId
     *            the subscription identifier to use.
     * 
     * @return the subscription id URI as a Jena {@link Node_URI}.
     */
    public static final Node createSubscriptionIdUri(String subscriptionId) {
        return NodeFactory.createURI(SUBSCRIPTION_NS + subscriptionId);
    }

    /**
     * Removes the specified {@code subscriptionId} from the given
     * {@code datastore}.
     * 
     * @param datastore
     *            the datastore from where the subscriptions are removed.
     * @param subscriptionId
     *            the subscriptions to remove.
     */
    public static final void deleteSubscription(TransactionalTdbDatastore datastore,
                                                SubscriptionId subscriptionId) {
        Node subscriptionIdUri =
                PublishSubscribeUtils.createSubscriptionIdUri(subscriptionId);

        TransactionalDatasetGraph txnGraph =
                datastore.begin(AccessMode.READ_ONLY);

        List<Quadruple> subscriptionQuadruples = null;
        try {
            subscriptionQuadruples =
                    Lists.newArrayList(txnGraph.find(
                            PublishSubscribeConstants.SUBSCRIPTION_NS_NODE,
                            subscriptionIdUri,
                            PublishSubscribeConstants.SUBSCRIPTION_HAS_SUBSUBSCRIPTION_NODE,
                            Node.ANY));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }

        if (subscriptionQuadruples != null) {
            txnGraph = datastore.begin(AccessMode.WRITE);

            try {
                for (Quadruple quad : subscriptionQuadruples) {
                    // removes the quadruples about the sub subscriptions
                    // associated
                    // to the subscription
                    txnGraph.delete(
                            Node.ANY,
                            createSubSubscriptionIdUri(quad.getObject()
                                    .getLiteralLexicalForm()), Node.ANY,
                            Node.ANY);
                }

                // removes the quadruples about the subscription
                txnGraph.delete(Node.ANY, subscriptionIdUri, Node.ANY, Node.ANY);
                txnGraph.commit();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                txnGraph.end();
            }
        }
    }

    /**
     * Extracts the {@link SubscriptionId} from the specified
     * {@code subscriptionIdUri}.
     * 
     * @param subscriptionIdUri
     *            the subscription id uri to use.
     * 
     * @return the {@link SubscriptionId} which has been extracted from the
     *         specified {@code subscriptionIdUri}.
     */
    public static final SubscriptionId extractSubscriptionId(Node subscriptionIdUri) {
        if (!subscriptionIdUri.isURI()
                || !subscriptionIdUri.getURI().startsWith(SUBSCRIPTION_NS)) {
            throw new IllegalArgumentException(
                    "The specified subscription id URI is not valid: "
                            + subscriptionIdUri);
        }

        return SubscriptionId.parseSubscriptionId(subscriptionIdUri.getURI()
                .substring(subscriptionIdUri.getURI().lastIndexOf(':') + 1));
    }

    public static final String extractSubscriptionId(String subscriptionIdUri) {
        int index = subscriptionIdUri.lastIndexOf(':');

        if (index == -1) {
            throw new IllegalArgumentException(
                    "The specified subscription id URI is not valid: "
                            + subscriptionIdUri);
        }

        return subscriptionIdUri.substring(index + 1);
    }

    /**
     * Creates a {@link Binding} from the specified {@link Quadruple}. Only the
     * variables, contained by the specified {@code resultVars} set, and their
     * associated value are kept in the final result. Finally, this method is
     * used to keep only the quadruple components that corresponds to the
     * variables that are declared as result var in the original subscription.
     * 
     * @param quad
     *            the quadruple to filter.
     * 
     * @param resultVars
     *            the result variables.
     * 
     * @param atomicQuery
     *            the {@link AtomicQuery} associated to the subscription which
     *            is matched by the specified {@code quad}.
     * 
     * @return a {@link Binding} where only the variables, contained by the
     *         specified {@code resultVars} set, and their associated value are
     *         kept in the final result.
     */
    public static final Binding filter(Quadruple quad, Set<Var> resultVars,
                                       AtomicQuery atomicQuery) {
        Set<Var> vars =
                Sets.intersection(resultVars, FluentIterable.from(
                        atomicQuery.getVars()).toSet());
        BindingMap binding = new BindingMap();
        Node[] quadNodes = quad.toArray();

        for (int i = 0; i < 4; i++) {
            Node node = atomicQuery.getNode(i);
            if (node.isVariable() && vars.contains(Var.alloc(node.getName()))) {
                Node resultNode;
                // the graph value which is returned is the meta graph value
                if (i == 0) {
                    resultNode = quad.createMetaGraphNode();
                } else {
                    resultNode = quadNodes[i];
                }

                binding.add(Var.alloc(node.getName()), resultNode);
            }
        }

        return binding;
    }

    /**
     * Indicates whether the specified quadruple is a meta-quadruple or not. A
     * meta-quadruple is a quadruple generated by the EventCloud and sent along
     * with a CE to indicate the number of quadruples contained into a CE.
     * 
     * @param q
     *            the quadruple to check for.
     * 
     * @return {@code true} if the specified quadruple is a meta-quadruple,
     *         {@code false} otherwise.
     */
    public static final boolean isMetaQuadruple(Quadruple q) {
        return q.getPredicate().equals(
                PublishSubscribeConstants.EVENT_NB_QUADRUPLES_NODE);
    }

    /**
     * Rewrites the specified {@code subscription} (written by using a SPARQL
     * Select query) to a new one where all the result vars have been removed
     * except for the graph variable.
     * 
     * @param subscription
     *            the subscription to rewrite.
     * 
     * @return new subscription where all the result vars have been removed
     *         except for the graph variable.
     */
    public static final String removeResultVarsExceptGraphVar(String subscription) {
        Query query = QueryFactory.create(subscription);

        final MutableObject graphNode = new MutableObject();

        ElementWalker.walk(query.getQueryPattern(), new ElementVisitorBase() {
            @Override
            public void visit(ElementNamedGraph el) {
                if (!el.getGraphNameNode().isVariable()) {
                    throw new IllegalArgumentException(
                            "The specified subscription does not have a graph variable: "
                                    + el.getGraphNameNode());
                }

                graphNode.setValue(el.getGraphNameNode());
            }
        });

        Op op = Transformer.transform(new TransformBase() {
            @Override
            public Op transform(OpProject opProject, Op subOp) {
                return new OpProject(
                        subOp, Lists.newArrayList((Var) graphNode.getValue()));
            }
        }, Algebra.compile(query));

        return OpAsQuery.asQuery(op).toString();
    }

    /**
     * Rewrites the subscription which is matched if some sub-subscriptions are
     * available or notify the subscriber about the solution if the subscription
     * cannot be rewritten.
     * 
     * @param semanticOverlay
     *            the overlay used to access to the datastore and to send
     *            requests.
     * @param subscription
     *            the subscription which is matched.
     * @param quadruple
     *            the quadruple matching the subscription.
     */
    public static void rewriteSubscriptionOrNotifySender(final SemanticCanOverlay semanticOverlay,
                                                         final Subscription subscription,
                                                         final Quadruple quadruple) {
        try {
            if (subscription.getSubSubscriptions().length == 1) {
                log.debug(
                        "{} matches a subscription which cannot be rewritten, a notification will be delivered",
                        quadruple);

                PublishSubscribeUtils.notifySubscriberAboutSolution(
                        semanticOverlay, subscription, quadruple);
            } else {
                PublishSubscribeUtils.rewriteAndIndexSubscription(
                        semanticOverlay, subscription, quadruple);
            }
        } catch (DecompositionException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Notifies the subscriber associated to the specified subscription about a
     * solution that matches his subscription.
     * 
     * @param semanticCanOverlay
     *            the overlay used to send requests.
     * @param subscription
     *            the subscription which is matched.
     * @param quadruple
     *            the quadruple that matches the subscription.
     */
    private static void notifySubscriberAboutSolution(final SemanticCanOverlay semanticCanOverlay,
                                                      final Subscription subscription,
                                                      final Quadruple quadruple) {
        if (filteredBySocialFilter(semanticCanOverlay, subscription, quadruple)) {
            return;
        }

        try {
            SubscribeProxy subscriber = subscription.getSubscriberProxy();

            String source = PAActiveObject.getUrl(semanticCanOverlay.getStub());;

            switch (subscription.getType()) {
                case BINDING:
                    BindingNotification notification =
                            new BindingNotification(
                                    subscription.getOriginalId(),
                                    quadruple.createMetaGraphNode(), source,
                                    createBindingSolution(
                                            subscription, quadruple));

                    // sends part of the solution to the subscriber
                    subscriber.receiveSbce1Or2(notification);

                    // broadcasts a message to all the stubs contained by
                    // the subscription to say to these peers to send the
                    // missing sub solutions to the subscriber
                    if (subscription.getIntermediatePeerReferences() != null) {
                        for (String peerURL : subscription.getIntermediatePeerReferences()
                                .keySet()) {
                            final SemanticPeer peerStub =
                                    semanticCanOverlay.findPeerStub(peerURL);

                            if (peerStub != null) {
                                Set<HashCode> hashCodes =
                                        subscription.getIntermediatePeerReferences()
                                                .get(peerURL);

                                peerStub.receive(new RetrieveSubSolutionOperation(
                                        notification.getId(),
                                        // copy to get a serializable set
                                        Sets.newHashSet(hashCodes)));
                            } else {
                                log.error(
                                        "Error while retrieving peer stub for URL: {}",
                                        peerURL);
                            }
                        }
                    }
                    break;
                case COMPOUND_EVENT:
                    if (EventCloudProperties.isSbce1PubSubAlgorithmUsed()) {
                        subscriber.receiveSbce1(new PollingSignalNotification(
                                subscription.getOriginalId(),
                                quadruple.createMetaGraphNode(), source));
                    } else if (EventCloudProperties.isSbce2PubSubAlgorithmUsed()
                            || EventCloudProperties.isSbce3PubSubAlgorithmUsed()) {
                        QuadruplesNotification quadruplesNotification =
                                new QuadruplesNotification(
                                        subscription.getOriginalId(),
                                        quadruple.createMetaGraphNode(),
                                        source, ImmutableList.of(quadruple));

                        subscriber.receiveSbce2(quadruplesNotification);
                        semanticCanOverlay.getStub().route(
                                new IndexEphemeralSubscriptionRequest(
                                        quadruple.createMetaGraphNode(),
                                        subscription.getOriginalId(),
                                        subscription.getSubscriberUrl()));
                    }
                    break;
                case SIGNAL:
                    subscriber.receiveSbce1Or2(new SignalNotification(
                            subscription.getOriginalId(),
                            quadruple.createMetaGraphNode(), source));
                    break;
            }

            log.debug(
                    "Notification sent for graph {} because subscription {} satisfied on peer {}",
                    quadruple.getGraph(), subscription.getId(),
                    semanticCanOverlay.getId());
        } catch (ExecutionException e) {
            log.warn("Notification cannot be sent because no SubscribeProxy found under URL: "
                    + subscription.getSubscriberUrl());

            // This could be due to a subscriber which has left
            // without unsubscribing or a temporary network outage.
            // In that case the subscription could be removed after some
            // attempts and/or time
            handleSubscriberConnectionFailure(semanticCanOverlay, subscription);
        }
    }

    public static boolean filteredBySocialFilter(final SemanticCanOverlay semanticCanOverlay,
                                                 final Subscription subscription,
                                                 final Quadruple quadruple) {
        if (semanticCanOverlay.hasSocialFilter()) {
            double relationshipStrength =
                    semanticCanOverlay.getSocialFilter()
                            .getRelationshipStrength(
                                    quadruple.getPublicationSource(),
                                    subscription.getSubscriptionDestination())
                            .getStrength();

            logSocialFilterAnswer(subscription, quadruple, relationshipStrength);

            // if relationshipStrength == 0.0, the source or
            // the target is unknown
            return (relationshipStrength > 0.0)
                    && (relationshipStrength < EventCloudProperties.SOCIAL_FILTER_THRESHOLD.getValue());
        }

        return false;
    }

    public static void handleSubscriberConnectionFailure(final SemanticCanOverlay semanticCanOverlay,
                                                         final Subscription subscription) {
        SubscriberConnectionFailure subscriberConnectionFailure;

        try {
            subscriberConnectionFailure =
                    semanticCanOverlay.getSubscriberConnectionFailures().get(
                            subscription.getOriginalId(),
                            new Callable<SubscriberConnectionFailure>() {
                                @Override
                                public SubscriberConnectionFailure call()
                                        throws Exception {
                                    return new SubscriberConnectionFailure();
                                }
                            });
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }

        synchronized (subscriberConnectionFailure) {
            subscriberConnectionFailure.incNbAttempts();

            // tries to remove subscriptions for proxies which are not reachable
            // after at least PROXY_MAX_LOOKUP_ATTEMPTS attempts. There is no
            // guarantee that it will be done exactly after this number of
            // attempts due to connection failure information that are stored by
            // using soft references.
            if (subscriberConnectionFailure.getNbAttempts() == EventCloudProperties.PROXY_MAX_LOOKUP_ATTEMPTS.getValue()) {
                try {
                    for (Subsubscription subSubscription : subscription.getSubSubscriptions()) {
                        semanticCanOverlay.getStub()
                                .route(
                                        new UnsubscribeRequest(
                                                subscription.getOriginalId(),
                                                subSubscription.getAtomicQuery(),
                                                subscription.getType() == NotificationListenerType.BINDING,
                                                true));

                        semanticCanOverlay.getSubscriberConnectionFailures()
                                .invalidate(subscription.getOriginalId());

                        log.info(
                                "Removed subscription {} due to subscriber which is not reachable at {}",
                                subscription.getId(),
                                subscription.getSubscriberUrl());
                    }
                } catch (DecompositionException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    private static void logSocialFilterAnswer(final Subscription subscription,
                                              final Quadruple quadruple,
                                              double relationshipStrength) {
        if (log.isDebugEnabled()) {
            log.debug(
                    "SocialFilterAnswer[source={}, destination={}, threshold={}, relationship_strengh={}, quadruple={}|{}|{}|{}]",
                    new Object[] {
                            quadruple.getPublicationSource(),
                            subscription.getSubscriptionDestination(),
                            EventCloudProperties.SOCIAL_FILTER_THRESHOLD.getValue(),
                            relationshipStrength, quadruple.getGraph(),
                            quadruple.getSubject(), quadruple.getPredicate(),
                            quadruple.getObject(),});
        }
    }

    /**
     * Creates a binding containing variables and their solution according to
     * the quadruple which is matched and the type of notification listener used
     * for the specified subscription.
     * 
     * @param subscription
     *            the subscription which is partially matched by the given
     *            quadruple.
     * @param quadruple
     *            the quadruple matching the first sub subscription of the
     *            specified subscription.
     * 
     * @return a binding containing variables and their solution according to
     *         the quadruple which is matched and the type of notification
     *         listener used for the specified subscription.
     */
    private static Binding createBindingSolution(final Subscription subscription,
                                                 final Quadruple quadruple) {
        // only the solutions for the result variables from the
        // original SPARQL query have to be returned to the
        // subscriber
        try {
            return PublishSubscribeUtils.filter(
                    quadruple, subscription.getResultVars(),
                    subscription.getSubSubscriptions()[0].getAtomicQuery());
        } catch (DecompositionException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Rewrites a subscription which is matched by the specified quadruple and
     * indexes it. Also, if the given subscription is associated to a binding
     * notification listener, a meta-quadruple containing the meta-information
     * to retrieve later is created.
     * 
     * @param overlay
     *            the overlay used to access to the datastore and to send
     *            requests.
     * @param subscription
     *            the subscription which is matched.
     * @param quadrupleMatching
     *            the quadruple that matches the specified subscription.
     */
    private static void rewriteAndIndexSubscription(final SemanticCanOverlay overlay,
                                                    final Subscription subscription,
                                                    final Quadruple quadrupleMatching) {
        if (subscription.getType() == NotificationListenerType.BINDING) {
            // creates an intermediate result that will be retrieved later when
            // all subsubscriptions are matched. This is to avoid to piggyback
            // intermediate values from peers to peers given that object values
            // may be some bytes or mega bytes when it is a literal
            Quadruple intermediateResult =
                    PublishSubscribeUtils.createIntermediateQuadrupleResult(
                            subscription, quadrupleMatching);

            TransactionalDatasetGraph txnGraph =
                    overlay.getSubscriptionsDatastore().begin(AccessMode.WRITE);

            try {
                txnGraph.add(intermediateResult);
                txnGraph.commit();
            } catch (Exception e) {
                e.printStackTrace();
                txnGraph.abort();
            } finally {
                txnGraph.end();
            }
        }

        // a subscription with more that one sub subscription (that matches the
        // quadruple which has been inserted) has been detected. Then we find
        // the subscription object associated to the subscriptionId that is
        // matched and we rewrite the subscription according to the quadruple
        // that matches the first sub-subscription.
        Subscription rewrittenSubscription =
                SubscriptionRewriter.rewrite(subscription, quadrupleMatching);

        if (subscription.getType() == NotificationListenerType.BINDING) {
            // stores the stub URL of the current peer in order to
            // have the possibility to retrieve the sub solutions later
            rewrittenSubscription.addIntermediatePeerReference(
                    PAActiveObject.getUrl(overlay.getStub()),
                    quadrupleMatching.hashValue());
        }

        if (P2PStructuredProperties.ENABLE_BENCHMARKS_INFORMATION.getValue()) {
            log.info("Peer "
                    + overlay
                    + " is about to dispatch a rewritten subscription, creation time = "
                    + rewrittenSubscription.getCreationTime()
                    + ", subscription: "
                    + rewrittenSubscription.getSparqlQuery());
        }

        try {
            overlay.getStub().route(
                    new IndexSubscriptionRequest(rewrittenSubscription));
        } catch (DecompositionException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Try to rewrite the specified {@code subscription} with the given
     * {@code compoundEvent} to know whether the subscription matched or not.
     * 
     * @param compoundEvent
     *            the compound event to test against the subscription.
     * @param subscription
     *            the subscription to rewrite.
     * 
     * @return {@code null} if the compound event does not match the or a
     *         {@link Pair} whose the first element contains the bindings
     *         associated to the parts of the subscription that are satisfied or
     *         {@code null}. The second element contains the index of the first
     *         quadruple that satisfies the subscription or {@code -1}.
     */
    public static final Pair<Binding, Integer> matches(CompoundEvent compoundEvent,
                                                       Subscription subscription) {
        Subsubscription[] subSubscriptions;
        try {
            subSubscriptions = subscription.getSubSubscriptions();
        } catch (DecompositionException e) {
            throw new IllegalStateException(e);
        }

        if (compoundEvent.size() < subSubscriptions.length) {
            return null;
        }

        BindingMap binding = new BindingMap();

        // number of sub-subscriptions contained initially by the subscription
        // we are trying to satisfy
        int nbSubSubscriptions = subSubscriptions.length;
        int nbSubSubscriptionSatisfied = 0;

        int indexFirstQuadrupleMatching = -1;

        for (int i = 0; i < nbSubSubscriptions; i++) {
            AtomicQuery aq = subSubscriptions[i].getAtomicQuery();

            for (int j = 0; j < compoundEvent.size(); j++) {
                BindingMap tmpBinding;

                if ((tmpBinding = matches(compoundEvent.get(j), aq)) == null) {
                    continue;
                } else if (indexFirstQuadrupleMatching == -1
                        && tmpBinding != null) {
                    indexFirstQuadrupleMatching = j;
                }

                binding.addAll(tmpBinding);
                nbSubSubscriptionSatisfied++;

                if (i < nbSubSubscriptions - 1) {
                    subscription =
                            SubscriptionRewriter.rewrite(
                                    subscription, compoundEvent.get(j));
                }

                if (log.isTraceEnabled()) {
                    log.trace(
                            "CE with graph {} matching subscription {} with {} as the index of the first quadruple matching the subscription",
                            compoundEvent.getGraph(), subscription.getId(),
                            indexFirstQuadrupleMatching);
                }

                // sub-subscription satisfied, look for the next
                break;
            }
        }

        if (binding.isEmpty()
                || nbSubSubscriptionSatisfied != nbSubSubscriptions) {
            return null;
        }

        return Pair.create((Binding) binding, indexFirstQuadrupleMatching);
    }

    /**
     * This method returns a list of vars and associated values if the specified
     * {@code quadruple} matches the given {@code atomicQuery}, otherwise
     * {@code null} is returned.
     * 
     * @param quadruple
     *            the quadruple to test.
     * @param atomicQuery
     *            the subscription to match with.
     * 
     * @return a list of vars and associated values if the specified
     *         {@code quadruple} matches the given {@code atomicQuery},
     *         otherwise {@code null} is returned.
     */
    public static final BindingMap matches(Quadruple quadruple,
                                           AtomicQuery atomicQuery) {
        if (atomicQuery.isFilterEvaluationRequired()) {
            return performMatchingQuadruplePatternWithFilter(
                    quadruple, atomicQuery);
        } else {
            return performMatchingQuadruplePattern(quadruple, atomicQuery);
        }
    }

    private static BindingMap performMatchingQuadruplePatternWithFilter(Quadruple quadruple,
                                                                        AtomicQuery atomicQuery) {
        DatasetGraph dataset = TDBFactory.createDatasetGraph();
        dataset.add(
                quadruple.getGraph(), quadruple.getSubject(),
                quadruple.getPredicate(), quadruple.getObject());

        QueryIterator it =
                Algebra.exec(atomicQuery.getOpRepresentation(), dataset);

        BindingMap result = null;

        if (it.hasNext()) {
            result = new PublishSubscribeUtils.BindingMap();

            while (it.hasNext()) {
                Binding binding = it.next();
                result.addAll(binding);
            }
        }

        TDBFactory.release(dataset);

        return result;
    }

    private static BindingMap performMatchingQuadruplePattern(Quadruple quadruple,
                                                              AtomicQuery atomicQuery) {
        Node graph = atomicQuery.getGraph();

        boolean graphIsVar = graph.isVariable();
        boolean graphVerified =
                graphIsVar
                        || graph.getURI().startsWith(
                                quadruple.getGraph().getURI());

        if (graphVerified) {
            Node subject = atomicQuery.getSubject();
            boolean subjectIsVar = subject.isVariable();
            boolean subjectVerified =
                    subjectIsVar || subject.equals(quadruple.getSubject());

            if (subjectVerified) {
                Node predicate = atomicQuery.getPredicate();
                boolean predicateIsVar = predicate.isVariable();
                boolean predicateVerified =
                        predicateIsVar
                                || predicate.equals(quadruple.getPredicate());

                if (predicateVerified) {
                    Node object = atomicQuery.getObject();
                    boolean objectIsVar = object.isVariable();
                    boolean objectVerified =
                            objectIsVar || object.equals(quadruple.getObject());

                    if (objectVerified) {
                        return extractBindings(
                                quadruple, graph, subject, predicate, object,
                                graphIsVar, subjectIsVar, predicateIsVar,
                                objectIsVar);
                    }
                }
            }
        }

        return null;
    }

    private static BindingMap extractBindings(Quadruple quadruple, Node graph,
                                              Node subject, Node predicate,
                                              Node object, boolean graphIsVar,
                                              boolean subjectIsVar,
                                              boolean predicateIsVar,
                                              boolean objectIsVar) {
        BindingMap binding = new PublishSubscribeUtils.BindingMap();

        if (graphIsVar) {
            binding.add(Var.alloc(graph.getName()), quadruple.getGraph());
        }

        if (subjectIsVar) {
            binding.add(Var.alloc(subject.getName()), quadruple.getSubject());
        }

        if (predicateIsVar) {
            binding.add(
                    Var.alloc(predicate.getName()), quadruple.getPredicate());
        }

        if (objectIsVar) {
            binding.add(Var.alloc(object.getName()), quadruple.getObject());
        }

        return binding;
    }

    /**
     * Stores the specified {@code quadruple} on the misc datastore contained by
     * the peer represented by the given {@code semanticOverlay}.
     * 
     * @param semanticOverlay
     *            the overlay representing the peer where the quadruple will be
     *            stored.
     * @param quadruple
     *            the quadruple to store.
     * @param metaGraphNode
     *            the meta graph node associated to the quadruple to store.
     */
    public static void storeQuadruple(SemanticCanOverlay semanticOverlay,
                                      final Quadruple quadruple,
                                      Node metaGraphNode) {
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
    }

    public static final class BindingMap implements
            com.hp.hpl.jena.sparql.engine.binding.BindingMap, Serializable {

        private static final long serialVersionUID = 160L;

        private transient Map<Var, Node> content = new HashMap<Var, Node>();

        public BindingMap() {

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<Var> vars() {
            return this.content.keySet().iterator();

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(Var var) {
            return this.content.containsKey(var);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Node get(Var var) {
            return this.content.get(var);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return this.content.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEmpty() {
            return this.content.isEmpty();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void add(Var var, Node node) {
            this.content.put(var, node);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void addAll(Binding binding) {
            Iterator<Var> varsIt = binding.vars();

            while (varsIt.hasNext()) {
                Var var = varsIt.next();
                this.content.put(var, binding.get(var));
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder result = new StringBuilder("(");

            Iterator<Entry<Var, Node>> it = this.content.entrySet().iterator();

            while (it.hasNext()) {
                Entry<Var, Node> entry = it.next();

                result.append(entry.getKey());
                result.append('=');
                result.append(entry.getValue());

                if (it.hasNext()) {
                    result.append(", ");
                }
            }

            result.append(')');

            return result.toString();
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();

            SparqlResultSerializer.serialize(
                    out, this, EventCloudProperties.COMPRESSION.getValue());
        }

        private void readObject(ObjectInputStream in) throws IOException,
                ClassNotFoundException {
            in.defaultReadObject();

            Binding binding =
                    SparqlResultSerializer.deserializeBinding(
                            in, EventCloudProperties.COMPRESSION.getValue());

            this.content = new HashMap<Var, Node>();
            Iterator<Var> it = binding.vars();

            while (it.hasNext()) {
                Var var = it.next();
                this.content.put(var, binding.get(var));
            }
        }
    }

}
