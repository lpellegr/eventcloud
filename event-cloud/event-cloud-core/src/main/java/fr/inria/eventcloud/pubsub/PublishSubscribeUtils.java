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
package fr.inria.eventcloud.pubsub;

import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_NS;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_NS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.mutable.MutableObject;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.utils.Pair;
import org.openjena.riot.out.NodeFmtLib;
import org.openjena.riot.out.OutputLangUtils;
import org.openjena.riot.tokens.Tokenizer;
import org.openjena.riot.tokens.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
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
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;

import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.NotificationListenerType;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.messages.request.can.IndexSubscriptionRequest;
import fr.inria.eventcloud.operations.can.RetrieveSubSolutionOperation;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.proxies.SubscribeProxy;
import fr.inria.eventcloud.reasoner.AtomicQuery;

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
     * Creates a matching quadruple meta information. This is a quadruple that
     * indicates that a subscription identified by its {@code subscriptionIdUri}
     * is matched for its {@code subSubscriptionId} with the
     * {@code quadrupleMatching} value.
     * 
     * @param quadrupleMatching
     *            the quadruple matching the subscription.
     * @param subscriptionIdUri
     *            an URI identifying the subscription which is matched.
     * @param subSubscriptionId
     *            the subSubscription which is really matched.
     * 
     * @return a quadruple containing the quadruple that is matched and the
     *         information that identify the subscription which is matched.
     */
    public static final Quadruple createMetaQuadruple(Quadruple quadrupleMatching,
                                                      Node subscriptionIdUri,
                                                      Node subSubscriptionId) {
        // generates the object value which is the concatenation of
        // subSubscriptionId and quadrupleMatching
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(baos);
        OutputLangUtils.output(osw, subSubscriptionId, null);
        try {
            osw.write(' ');
        } catch (IOException e) {
            e.printStackTrace();
        }
        OutputLangUtils.output(
                osw, quadrupleMatching.createMetaGraphNode(),
                quadrupleMatching.getSubject(),
                quadrupleMatching.getPredicate(),
                quadrupleMatching.getObject(), null, null);
        try {
            osw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Quadruple(
                createQuadrupleHashUri(quadrupleMatching), subscriptionIdUri,
                PublishSubscribeConstants.QUADRUPLE_MATCHES_SUBSCRIPTION_NODE,
                Node.createLiteral(new String(baos.toByteArray())));
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
        query.append("SELECT ?subscriptionId WHERE {\n    GRAPH ");
        query.append(NodeFmtLib.str(PublishSubscribeConstants.SUBSCRIPTION_NS_NODE));
        query.append(" {\n        ");
        query.append("?subscriptionIdUri ");
        query.append(NodeFmtLib.str(PublishSubscribeConstants.SUBSCRIPTION_ORIGINAL_ID_NODE));
        query.append(' ');
        query.append(NodeFmtLib.str(originalSubscriptionId.toJenaNode()));
        query.append(" .\n        ?subscriptionIdUri ");
        // query.append(NodeFmtLib.serialize(PublishSubscribeConstants.SUBSCRIPTION_INDEXED_WITH_NODE));
        // query.append(" ?subSubscriptionId .\n        ?subscriptionIdUri ");
        query.append(NodeFmtLib.str(PublishSubscribeConstants.SUBSCRIPTION_ID_NODE));
        query.append(" ?subscriptionId .\n    }\n}");

        List<SubscriptionId> ids = new ArrayList<SubscriptionId>();

        TransactionalDatasetGraph txnGraph =
                datastore.begin(AccessMode.READ_ONLY);

        QueryExecution qExec = null;
        try {
            qExec =
                    QueryExecutionFactory.create(
                            query.toString(), txnGraph.toDataset());
            ResultSet result = qExec.execSelect();

            while (result.hasNext()) {
                Binding binding = result.nextBinding();

                SubscriptionId subscriptionId =
                        SubscriptionId.parseSubscriptionId(binding.get(
                                Var.alloc("subscriptionId"))
                                .getLiteralLexicalForm());
                // SubscriptionId subSubscriptionId =
                // new SubscriptionId(
                // ((Number) binding.get(
                // Var.alloc("subSubscriptionId"))
                // .getLiteralValue()).longValue());
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
     * Extracts the {@link Quadruple} and the sub-subscription id contained by
     * meta quadruple that has been previously created by a call to
     * {@link PublishSubscribeUtils#createMetaQuadruple(Quadruple, Node, Node)}
     * .
     * 
     * @param metaQuad
     *            the meta quadruple.
     * 
     * @return extracts the {@link Quadruple} and the sub-subscription id
     *         contained by a meta quadruple that has been previously created by
     *         a call to
     *         {@link PublishSubscribeUtils#createMetaQuadruple(Quadruple, Node, Node)}
     *         .
     */
    public static final Pair<Quadruple, SubscriptionId> extractMetaInformation(Quadruple metaQuad) {
        String objectValue = metaQuad.getObject().getLiteralLexicalForm();

        ByteArrayInputStream bais =
                new ByteArrayInputStream(objectValue.getBytes());

        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(bais);

        Node subSubscriptionId = tokenizer.next().asNode();

        return Pair.create(
                new Quadruple(tokenizer.next().asNode(), tokenizer.next()
                        .asNode(), tokenizer.next().asNode(), tokenizer.next()
                        .asNode()),
                SubscriptionId.parseSubscriptionId(subSubscriptionId.getLiteralLexicalForm()));
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
        return Node.createURI(PublishSubscribeConstants.QUADRUPLE_NS.concat(quadHash.toString()));
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
        return Node.createURI(SUBSUBSCRIPTION_NS + subSubscriptionId);
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
        return Node.createURI(SUBSCRIPTION_NS + subscriptionId);
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
                Sets.intersection(resultVars, atomicQuery.getVariables());
        BindingMap binding = BindingFactory.create();
        Node[] quadNodes = quad.toArray();

        int i = 0;
        for (Node node : atomicQuery.toArray()) {
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
            i++;
        }

        return binding;
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
        if (semanticCanOverlay.hasSocialFilter()) {
            double relationshipStrength =
                    semanticCanOverlay.getSocialFilter()
                            .getRelationshipStrength(
                                    quadruple.getPublicationSource(),
                                    subscription.getSubscriptionDestination())
                            .getStrength();

            logSocialFilterAnswer(subscription, quadruple, relationshipStrength);

            if (relationshipStrength < EventCloudProperties.SOCIAL_FILTER_THRESHOLD.getValue()) {
                return;
            }
        }

        try {
            final SubscribeProxy subscriber = subscription.getSubscriberProxy();

            final NotificationId notificationId =
                    new NotificationId(subscription.getOriginalId());

            final Notification n =
                    new Notification(
                            notificationId,
                            PAActiveObject.getUrl(semanticCanOverlay.getStub()),
                            createBindingSolution(subscription, quadruple));

            // FIXME issue #24
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    subscriber.receive(n);
                }
            });
            thread.setName("NotifySubscriberThread");
            thread.start();

            if (subscription.getType() == NotificationListenerType.BINDING) {
                // broadcasts a message to all the stubs contained by
                // the subscription to say to these peers to send their
                // sub-solutions to the subscriber
                for (final Subscription.Stub stub : subscription.getStubs()) {
                    final SemanticPeer peerStub =
                            semanticCanOverlay.findPeerStub(stub.peerUrl);

                    if (peerStub != null) {
                        // FIXME: issue #24
                        thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                peerStub.receive(new RetrieveSubSolutionOperation(
                                        notificationId, stub.quadrupleHash));
                            }
                        });
                        thread.setName("RetrieveSubSolutionThread");
                        thread.start();
                    } else {
                        log.error(
                                "Error while retrieving peer stub for url: {}",
                                stub.peerUrl);
                    }
                }
            }
        } catch (ExecutionException e) {
            log.warn(
                    "Notification cannot be sent because no SubscribeProxy found under URL: "
                            + subscription.getSubscriberUrl(), e);

            // TODO: this could be due to a subscriber which has left
            // without unsubscribing. In that case we can remove the
            // subscription information associated to this subscriber
            // and also send a message
        }
    }

    private static void logSocialFilterAnswer(final Subscription subscription,
                                              final Quadruple quadruple,
                                              double relationshipStrength) {
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

    /**
     * Creates a binding containing variables and their solution according to
     * the quadruple which is matched and the type of notification listener used
     * for the specified subscription.
     * 
     * @param subscription
     *            the subscription which is partially matched by the given
     *            quadruple.
     * @param quadruple
     *            the quadruple matching the first subsubscription of the
     *            specified subscription.
     * 
     * @return a binding containing variables and their solution according to
     *         the quadruple which is matched and the type of notification
     *         listener used for the specified subscription.
     */
    private static Binding createBindingSolution(final Subscription subscription,
                                                 Quadruple quadruple) {
        Binding binding = null;

        // for a signal notification listener no binding are returned
        if (subscription.getType() == NotificationListenerType.BINDING) {
            // only the solutions for the result variables from the
            // original SPARQL query have to be returned to the
            // subscriber
            binding =
                    PublishSubscribeUtils.filter(
                            quadruple,
                            subscription.getResultVars(),
                            subscription.getSubSubscriptions()[0].getAtomicQuery());
        } else if (subscription.getType() == NotificationListenerType.COMPOUND_EVENT) {
            // only the graph value has to be returned to the subscriber
            binding =
                    BindingFactory.binding(
                            Var.alloc("g"), quadruple.createMetaGraphNode());
        }

        return binding;
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
        TransactionalTdbDatastore datastore =
                (TransactionalTdbDatastore) overlay.getDatastore();

        if (subscription.getType() == NotificationListenerType.BINDING) {
            // stores a quadruple that contains the information about the
            // subscription that is matched and the quadruple that matches the
            // subscription. This is useful to create the notification later.
            // The matching quadruple is not sent directly to the next peers
            // because the quadruple value will be stored in memory on several
            // peer. Moreover, there is no limit about the size of a quadruple.
            Quadruple metaQuad =
                    PublishSubscribeUtils.createMetaQuadruple(
                            quadrupleMatching,
                            PublishSubscribeUtils.createSubscriptionIdUri(subscription.getId()),
                            Node.createLiteral(
                                    subscription.getId().toString(),
                                    XSDDatatype.XSDlong));

            TransactionalDatasetGraph txnGraph =
                    datastore.begin(AccessMode.WRITE);

            try {
                txnGraph.add(metaQuad);
                txnGraph.commit();
            } catch (Exception e) {
                e.printStackTrace();
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

        // stores the stub URL of the current peer in order to
        // have the possibility to retrieve the sub-solution later
        rewrittenSubscription.addStub(new Subscription.Stub(
                PAActiveObject.getUrl(overlay.getStub()),
                quadrupleMatching.hashValue()));

        try {
            overlay.dispatchv(new IndexSubscriptionRequest(
                    rewrittenSubscription));
        } catch (DispatchException e) {
            e.printStackTrace();
        }
    }

}
