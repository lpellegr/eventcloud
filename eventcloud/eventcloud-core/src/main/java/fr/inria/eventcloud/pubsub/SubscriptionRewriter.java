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
package fr.inria.eventcloud.pubsub;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.hash.HashCode;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;

/**
 * This class is used to rewrite a {@link Subscription}. For more information
 * look at the description of the {@link #rewrite(Subscription, Quadruple)}
 * method.
 * 
 * @author lpellegr
 */
public final class SubscriptionRewriter {

    private SubscriptionRewriter() {

    }

    /**
     * Rewrites the first triple pattern from the SPARQL query. The rewrite
     * operation consists in removing the first triple pattern from the SPARQL
     * query and to replace all the variables (identified with the first triple
     * pattern) by the value associated with the specified {@code quad}.
     * 
     * @param subscription
     *            the subscription to rewrite.
     * @param quad
     *            the quadruple that is used to replace the variables.
     * 
     * @return a new subscription which has been rewritten and which has the
     *         original subscription as parent.
     */
    public static final Subscription rewrite(Subscription subscription,
                                             Quadruple quad) {
        return removeFirstTriplePatternAndReplaceVars(
                subscription, createBGPTransformCopy(quad));
    }

    /**
     * Rewrites the first triple pattern from the SPARQL query. The rewrite
     * operation consists in removing the first triple pattern from the SPARQL
     * query and to replace all the variables (identified with the first triple
     * pattern) by the value associated with the specified {@code binding}.
     * 
     * @param subscription
     *            the subscription to rewrite.
     * @param binding
     *            the binding that is used to replace the variables.
     * 
     * @return a new subscription which has been rewritten and which has the
     *         original subscription as parent.
     */
    public static final Subscription rewrite(Subscription subscription,
                                             Binding binding) {
        return removeFirstTriplePatternAndReplaceVars(
                subscription, createBGPTransformCopy(binding));
    }

    /**
     * Removes the first triple pattern from the SPARQL query and rewrites the
     * next triple patterns. This method assumes that the SPARQL query has only
     * {@code one} Basic Graph Pattern.
     * 
     * @param subscription
     *            the subscription to transform.
     * 
     * @param tc
     *            the transform copy that is used to remove the first triple
     *            pattern and to rewrite the next.
     * 
     * @return a new SPARQL query with the first triple pattern which has been
     *         removed or {@code null} if the original SPARQL query contains
     *         only one triple pattern.
     */
    private static final Subscription removeFirstTriplePatternAndReplaceVars(Subscription subscription,
                                                                             TransformCopy tc) {
        Subscription result =
                new Subscription(
                        subscription.getOriginalId(),
                        subscription.getId(),
                        new SubscriptionId(),
                        subscription.getCreationTime(),
                        subscription.getIndexationTime(),
                        OpAsQuery.asQuery(
                                Transformer.transform(
                                        tc,
                                        Algebra.compile(QueryFactory.create(subscription.getSparqlQuery()))))
                                .toString(), subscription.getSubscriberUrl(),
                        subscription.getSubscriptionDestination(),
                        subscription.getType());

        if (subscription.getIntermediatePeerReferences() != null) {
            for (String peerURL : subscription.getIntermediatePeerReferences()
                    .keys()) {
                for (HashCode hc : subscription.getIntermediatePeerReferences()
                        .get(peerURL)) {
                    result.addIntermediatePeerReference(peerURL, hc);
                }
            }
        }

        return result;
    }

    private static final TransformCopy createBGPTransformCopy(final Quadruple quad) {
        // vars that are contained by the first triple pattern
        final Map<Node, Node> vars = new HashMap<Node, Node>(3);

        return new TransformCopy() {
            @Override
            public Op transform(OpBGP opBGP) {
                BasicPattern oldBasicPattern = opBGP.getPattern();
                BasicPattern newBasicPattern = new BasicPattern();

                Iterator<Triple> it = oldBasicPattern.iterator();
                Triple triple;

                // skips the first triple pattern if possible
                if (!it.hasNext()) {
                    throw new IllegalArgumentException(
                            "The SPARQL query to rewrite must have at least 2 triple patterns");
                } else {
                    triple = it.next();

                    // extracts the variables from the first triple pattern
                    if (triple.getSubject().isVariable()) {
                        vars.put(triple.getSubject(), quad.getSubject());
                    }
                    if (triple.getPredicate().isVariable()) {
                        vars.put(triple.getPredicate(), quad.getPredicate());
                    }
                    if (triple.getObject().isVariable()) {
                        vars.put(triple.getObject(), quad.getObject());
                    }
                }

                // the query is assumed to have at least two triple patterns
                if (!it.hasNext()) {
                    throw new IllegalArgumentException(
                            "The SPARQL query to rewrite must have at least 2 triple patterns");
                }

                while (it.hasNext()) {
                    triple = it.next();
                    // replaces the variables which have the same name as the
                    // variables from the first triple pattern by the value from
                    // the quadruple that match the first triple pattern
                    newBasicPattern.add(Triple.create(
                            replaceVarByQuadrupleValue(
                                    triple.getSubject(), vars,
                                    quad.getSubject()),
                            replaceVarByQuadrupleValue(
                                    triple.getPredicate(), vars,
                                    quad.getPredicate()),
                            replaceVarByQuadrupleValue(
                                    triple.getObject(), vars, quad.getObject())));
                }

                return new OpBGP(newBasicPattern);
            }

            @Override
            public Op transform(OpGraph opGraph, Op subOp) {
                // if the graph node from the subscription to rewrite is a
                // variable it has to be rewritten with the graph node from the
                // quadruple which matches the subscription
                if (opGraph.getNode().isVariable()) {
                    return new OpGraph(quad.getGraph(), subOp);
                }

                return new OpGraph(opGraph.getNode(), subOp);
            }
        };
    }

    private static final TransformCopy createBGPTransformCopy(final Binding binding) {
        return new TransformCopy() {
            @Override
            public Op transform(OpBGP opBGP) {
                BasicPattern oldBasicPattern = opBGP.getPattern();
                BasicPattern newBasicPattern = new BasicPattern();

                Iterator<Triple> it = oldBasicPattern.iterator();
                Triple triple;

                // skips the first triple pattern if possible
                if (!it.hasNext()) {
                    throw new IllegalArgumentException(
                            "The SPARQL query to rewrite must have at least 2 triple patterns");
                } else {
                    triple = it.next();
                }

                if (!it.hasNext()) {
                    throw new IllegalArgumentException(
                            "The SPARQL query to rewrite must have at least 2 triple patterns");
                }

                while (it.hasNext()) {
                    triple = it.next();
                    // replaces the variables which have the same name as the
                    // variables from the first triple pattern by the value from
                    // the quadruple that match the first triple pattern
                    newBasicPattern.add(Triple.create(
                            replaceVarByBindingValue(
                                    triple.getSubject(), binding),
                            replaceVarByBindingValue(
                                    triple.getPredicate(), binding),
                            replaceVarByBindingValue(
                                    triple.getObject(), binding)));
                }

                return new OpBGP(newBasicPattern);
            }
        };
    }

    /**
     * Returns the {@code quadNode} if the {@code tripleNode} is contained by
     * the {@code vars} set. Otherwise it returns the {@code tripleNode}.
     * 
     * @param tripleNode
     * @param vars
     * @param quadNode
     * 
     * @return the {@code quadNode} if the {@code tripleNode} is contained by
     *         the {@code vars} set. Otherwise it returns the {@code tripleNode}
     *         .
     */
    private static final Node replaceVarByQuadrupleValue(Node tripleNode,
                                                         Map<Node, Node> vars,
                                                         Node quadNode) {
        Node value = vars.get(tripleNode);

        if (value != null) {
            return value;
        }

        return tripleNode;
    }

    private static final Node replaceVarByBindingValue(Node tripleNode,
                                                       Binding binding) {

        if (tripleNode.isVariable()) {
            Node value = binding.get(Var.alloc(tripleNode.getName()));
            if (value != null) {
                return value;
            }
        }

        return tripleNode;
    }

}
