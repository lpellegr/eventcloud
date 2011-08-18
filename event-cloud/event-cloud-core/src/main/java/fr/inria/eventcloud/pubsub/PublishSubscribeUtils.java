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

import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.SUBSCRIPTION_NS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;

import org.objectweb.proactive.extensions.p2p.structured.utils.Pair;
import org.openjena.riot.out.OutputLangUtils;
import org.openjena.riot.tokens.Tokenizer;
import org.openjena.riot.tokens.TokenizerFactory;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.reasoner.AtomicQuery;

/**
 * Some utility methods for the publish/subscribe algorithm.
 * 
 * @author lpellegr
 */
public class PublishSubscribeUtils {

    private PublishSubscribeUtils() {
        
    }
    
    /**
     * Creates the matching quadruple meta information. This is a quadruple that
     * indicates that a subscription identified by {@code subscriptionIdUrl} is
     * matched for its {@code subSubscriptionId} with the
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
                osw, quadrupleMatching.getGraph(),
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
        return Node.createURI(PublishSubscribeConstants.QUADRUPLE_NS
                + quad.hashValue());
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
    public static final Node createQuadrupleHashUrl(long quadHash) {
        return Node.createURI(PublishSubscribeConstants.QUADRUPLE_NS + quadHash);
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
        Binding binding = BindingFactory.create();
        Node[] nodes = quad.toArray();

        int i = 0;
        for (Node node : atomicQuery.toArray()) {
            if (node.isVariable() && vars.contains(Var.alloc(node.getName()))) {
                binding.add(Var.alloc(node.getName()), nodes[i]);
            }
            i++;
        }

        return binding;
    }

}
