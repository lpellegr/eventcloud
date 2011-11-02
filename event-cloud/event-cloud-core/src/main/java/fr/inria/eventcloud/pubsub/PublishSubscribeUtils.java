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

import org.objectweb.proactive.extensions.p2p.structured.utils.Pair;
import org.openjena.riot.out.NodeFmtLib;
import org.openjena.riot.out.OutputLangUtils;
import org.openjena.riot.tokens.Tokenizer;
import org.openjena.riot.tokens.TokenizerFactory;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.reasoner.AtomicQuery;
import fr.inria.eventcloud.utils.LongLong;

/**
 * Some utility methods for the publish/subscribe algorithm.
 * 
 * @author lpellegr
 */
public final class PublishSubscribeUtils {

    private PublishSubscribeUtils() {

    }

    /**
     * Creates a matching quadruple meta information. This is a quadruple that
     * indicates that a subscription identified by its {@code subscriptionIdUrl}
     * is matched for its {@code subSubscriptionId} with the
     * {@code quadrupleMatching} value.
     * 
     * @param quadrupleMatching
     *            the quadruple matching the subscription.
     * @param subscriptionIdUrl
     *            the url identifying the subscription which is matched.
     * @param subSubscriptionId
     *            the subSubscription which is really matched.
     * 
     * @return a quadruple containing the quadruple that is matched and the
     *         information that identify the subscription which is matched.
     */
    public static final Quadruple createMetaQuadruple(Quadruple quadrupleMatching,
                                                      Node subscriptionIdUrl,
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
                createQuadrupleHashUrl(quadrupleMatching), subscriptionIdUrl,
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
        query.append("?subscriptionIdUrl ");
        query.append(NodeFmtLib.str(PublishSubscribeConstants.SUBSCRIPTION_ORIGINAL_ID_NODE));
        query.append(" ");
        query.append(NodeFmtLib.str(originalSubscriptionId.toJenaNode()));
        query.append(" .\n        ?subscriptionIdUrl ");
        // query.append(NodeFmtLib.serialize(PublishSubscribeConstants.SUBSCRIPTION_INDEXED_WITH_NODE));
        // query.append(" ?subSubscriptionId .\n        ?subscriptionIdUrl ");
        query.append(NodeFmtLib.str(PublishSubscribeConstants.SUBSCRIPTION_ID_NODE));
        query.append(" ?subscriptionId .\n    }\n}");

        List<SubscriptionId> ids = new ArrayList<SubscriptionId>();

        TransactionalDatasetGraph txnGraph =
                datastore.begin(AccessMode.READ_ONLY);
        QueryExecution queryExecution =
                QueryExecutionFactory.create(
                        query.toString(), txnGraph.toDataset());
        ResultSet result = queryExecution.execSelect();

        try {
            while (result.hasNext()) {
                Binding binding = result.nextBinding();

                SubscriptionId subscriptionId =
                        SubscriptionId.parseFrom(binding.get(
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
            queryExecution.close();
            txnGraph.close();
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

        return new Pair<Quadruple, SubscriptionId>(
                new Quadruple(tokenizer.next().asNode(), tokenizer.next()
                        .asNode(), tokenizer.next().asNode(), tokenizer.next()
                        .asNode()),
                SubscriptionId.parseFrom(subSubscriptionId.getLiteralLexicalForm()));
    }

    /**
     * Creates a quadruple hash URL by using the specified {@code quad}.
     * 
     * @param quad
     *            the quadruple to use.
     * 
     * @return the quadruple hash URL as a Jena {@link Node_URI}.
     */
    public static final Node createQuadrupleHashUrl(Quadruple quad) {
        return createQuadrupleHashUrl(quad.hashValue());
    }

    /**
     * Creates the quadruple hash URL by using the specified {@code quadHash}
     * which is assumed to be the hash value associated to a {@link Quadruple}.
     * 
     * @param quadHash
     *            the hash value to use.
     * 
     * @return a quadruple hash URL as a Jena {@link Node_URI}.
     */
    public static final Node createQuadrupleHashUrl(LongLong quadHash) {
        return Node.createURI(PublishSubscribeConstants.QUADRUPLE_NS.concat(quadHash.toString()));
    }

    /**
     * Creates a subscription id URL from the specified {@code id}.
     * 
     * @param id
     *            the subscription identifier to use.
     * 
     * @return the subscription id URL as a Jena {@link Node_URI}.
     */
    public static final Node createSubscriptionIdUrl(SubscriptionId id) {
        return createSubscriptionIdUrl(id.toString());
    }

    /**
     * Creates a sub subscription id URL from the specified
     * {@code subscriptionId} which is assumed to be a {@link SubscriptionId}.
     * 
     * @param subSubscriptionId
     *            the subscription identifier to use.
     * 
     * @return the sub subscription id URL as a Jena {@link Node_URI}.
     */
    public static final Node createSubSubscriptionIdUrl(String subSubscriptionId) {
        return Node.createURI(SUBSUBSCRIPTION_NS + subSubscriptionId);
    }

    /**
     * Creates a sub subscription id URL from the specified {@code id}.
     * 
     * @param subSubscriptionId
     *            the sub subscription identifier to use.
     * 
     * @return the sub subscription id URL as a Jena {@link Node_URI}.
     */
    public static final Node createSubSubscriptionIdUrl(SubscriptionId subSubscriptionId) {
        return createSubSubscriptionIdUrl(subSubscriptionId.toString());
    }

    /**
     * Creates a subscription id URL from the specified {@code subscriptionId}
     * which is assumed to be a {@link SubscriptionId}.
     * 
     * @param subscriptionId
     *            the subscription identifier to use.
     * 
     * @return the subscription id URL as a Jena {@link Node_URI}.
     */
    public static final Node createSubscriptionIdUrl(String subscriptionId) {
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
        Node subscriptionIdUrl =
                PublishSubscribeUtils.createSubscriptionIdUrl(subscriptionId);

        TransactionalDatasetGraph tnxGraph =
                datastore.begin(AccessMode.READ_ONLY);
        Collection<Quadruple> subscriptionQuadruples =
                Collection.from(tnxGraph.find(
                        PublishSubscribeConstants.SUBSCRIPTION_NS_NODE,
                        subscriptionIdUrl,
                        PublishSubscribeConstants.SUBSCRIPTION_HAS_SUBSUBSCRIPTION_NODE,
                        Node.ANY));
        tnxGraph.close();

        tnxGraph = datastore.begin(AccessMode.WRITE);
        for (Quadruple quad : subscriptionQuadruples) {
            // removes the quadruples about the sub subscriptions associated to
            // the
            // subscription
            tnxGraph.delete(
                    Node.ANY, createSubSubscriptionIdUrl(quad.getObject()
                            .getLiteralLexicalForm()), Node.ANY, Node.ANY);
        }

        // removes the quadruples about the subscription
        tnxGraph.delete(Node.ANY, subscriptionIdUrl, Node.ANY, Node.ANY);
        tnxGraph.commit();
        tnxGraph.close();
    }

    /**
     * Extracts the {@link SubscriptionId} from the specified
     * {@code subscriptionIdUrl}.
     * 
     * @param subscriptionIdUrl
     *            the subscription id url to use.
     * @return the {@link SubscriptionId} which has been extracted from the
     *         specified {@code subscriptionIdUrl}.
     */
    public static final SubscriptionId extractSubscriptionId(Node subscriptionIdUrl) {
        if (!subscriptionIdUrl.isURI()
                || !subscriptionIdUrl.getURI().startsWith(SUBSCRIPTION_NS)) {
            throw new IllegalArgumentException("Not a subscriptionIdUrl: "
                    + subscriptionIdUrl);
        }

        return SubscriptionId.parseFrom(subscriptionIdUrl.getURI().substring(
                subscriptionIdUrl.getURI().lastIndexOf("/") + 1));
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

}
