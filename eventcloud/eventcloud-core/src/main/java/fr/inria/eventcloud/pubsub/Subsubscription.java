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

import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_GRAPH_VALUE_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_GRAPH_VALUE_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_ID_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_INDEX_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_INDEX_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_NS;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_OBJECT_VALUE_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_OBJECT_VALUE_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_PREDICATE_VALUE_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_PREDICATE_VALUE_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_SUBJECT_VALUE_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_SUBJECT_VALUE_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_VAR_NAMES_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_VAR_NAMES_PROPERTY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruplable;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.QuadrupleIterator;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.reasoner.AtomicQuery;

/**
 * A Sub-subscription is modeled by using an {@link AtomicQuery} that knows who
 * is its parent and what is its position in the parent query.
 * 
 * @author lpellegr
 */
public class Subsubscription implements Quadruplable {

    private final SubscriptionId originalId;

    private final SubscriptionId parentId;

    private final SubscriptionId id;

    private final int index;

    private final AtomicQuery atomicQuery;

    public Subsubscription(SubscriptionId originalId, SubscriptionId parentId,
            AtomicQuery atomicQuery, int index) {
        this.originalId = originalId;
        this.parentId = parentId;
        this.atomicQuery = atomicQuery;
        this.index = index;
        this.id = new SubscriptionId();
    }

    private Subsubscription(SubscriptionId originalId, SubscriptionId parentId,
            SubscriptionId id, int index, Node graph, Node subject,
            Node predicate, Node object) {
        this.originalId = originalId;
        this.parentId = parentId;
        this.id = id;
        this.index = index;
        this.atomicQuery = new AtomicQuery(graph, subject, predicate, object);
    }

    public SubscriptionId getOriginalId() {
        return this.originalId;
    }

    public SubscriptionId getParentId() {
        return this.parentId;
    }

    public SubscriptionId getId() {
        return this.id;
    }

    /**
     * Returns the position of the sub-subscription in the parent subscription.
     * 
     * @return the position of the sub-subscription in the parent subscription.
     */
    public int getIndex() {
        return this.index;
    }

    public AtomicQuery getAtomicQuery() {
        return this.atomicQuery;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> toQuadruples() {
        Builder<Quadruple> result = new ImmutableList.Builder<Quadruple>();

        Node originalSubscriptionURI =
                PublishSubscribeUtils.createSubscriptionIdUri(this.originalId);
        Node subSubscriptionURI =
                Node.createURI(SUBSUBSCRIPTION_NS + this.id.toString());

        result.add(new Quadruple(
                originalSubscriptionURI, subSubscriptionURI,
                SUBSUBSCRIPTION_ID_NODE,
                Node.createLiteral(this.id.toString()), false, false));

        result.add(new Quadruple(
                originalSubscriptionURI, subSubscriptionURI,
                SUBSUBSCRIPTION_INDEX_NODE, Node.createLiteral(
                        Integer.toString(this.index), XSDDatatype.XSDint),
                false, false));

        result.add(new Quadruple(
                originalSubscriptionURI, subSubscriptionURI,
                SUBSUBSCRIPTION_GRAPH_VALUE_NODE,
                replaceVarNodeByConstant(this.atomicQuery.getGraph()), false,
                false));

        result.add(new Quadruple(
                originalSubscriptionURI, subSubscriptionURI,
                SUBSUBSCRIPTION_SUBJECT_VALUE_NODE,
                replaceVarNodeByConstant(this.atomicQuery.getSubject()), false,
                false));

        result.add(new Quadruple(
                originalSubscriptionURI, subSubscriptionURI,
                SUBSUBSCRIPTION_PREDICATE_VALUE_NODE,
                replaceVarNodeByConstant(this.atomicQuery.getPredicate()),
                false, false));

        result.add(new Quadruple(
                originalSubscriptionURI, subSubscriptionURI,
                SUBSUBSCRIPTION_OBJECT_VALUE_NODE,
                replaceVarNodeByConstant(this.atomicQuery.getObject()), false,
                false));

        result.add(createVarNamesQuadruple(
                originalSubscriptionURI, subSubscriptionURI, this.atomicQuery));

        return result.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Subsubscription
                && this.id.equals(((Subsubscription) obj).id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Subsubscription [parentId=" + this.parentId + ", id=" + this.id
                + ", index=" + this.index + ", atomicQuery=" + this.atomicQuery
                + "]";
    };

    public static final Subsubscription parseFrom(TransactionalTdbDatastore datastore,
                                                  SubscriptionId subscriptionId,
                                                  Node subSubscriptionIdNode) {
        TransactionalDatasetGraph txnGraph =
                datastore.begin(AccessMode.READ_ONLY);

        try {
            return parseFrom(txnGraph, subscriptionId, subSubscriptionIdNode);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            txnGraph.end();
        }
    }

    /**
     * Parses a {@link Subsubscription} from the specified
     * {@link TransactionalTdbDatastore} by using the specified
     * {@code subscriptionId} and {@code subSubscriptionId}.
     * 
     * @param txnGraph
     *            the dataset where the information about the sub subscription
     *            are stored.
     * @param subscriptionId
     *            the subscriptionId which is the parent id for the
     *            {@code subSubscriptionId}
     * @param subSubscriptionIdNode
     *            the identifier of the sub subscription as a Node.
     * 
     * @return the sub subscription which has been parsed.
     */
    public static final Subsubscription parseFrom(TransactionalDatasetGraph txnGraph,
                                                  SubscriptionId subscriptionId,
                                                  Node subSubscriptionIdNode) {
        // contains the properties and their associated values that are read
        // from the datastore for the given subSubscriptionId
        Map<String, Node> properties = new HashMap<String, Node>();

        SubscriptionId originalId = null;
        QuadrupleIterator it =
                txnGraph.find(
                        Node.ANY, subSubscriptionIdNode, Node.ANY, Node.ANY);

        while (it.hasNext()) {
            Quadruple quad = it.next();
            properties.put(quad.getPredicate().toString(), quad.getObject());

            if (!it.hasNext()) {
                originalId =
                        PublishSubscribeUtils.extractSubscriptionId(quad.getGraph());
            }
        }

        Node[] ssNodes =
                AtomicQuery.parseVarNamesFromString(properties.get(
                        SUBSUBSCRIPTION_VAR_NAMES_PROPERTY)
                        .getLiteralLexicalForm());

        if (!properties.get(SUBSUBSCRIPTION_GRAPH_VALUE_PROPERTY).equals(
                PublishSubscribeConstants.SUBSCRIPTION_VARIABLE_NODE)) {
            ssNodes[0] = properties.get(SUBSUBSCRIPTION_GRAPH_VALUE_PROPERTY);
        }

        if (!properties.get(SUBSUBSCRIPTION_SUBJECT_VALUE_PROPERTY).equals(
                PublishSubscribeConstants.SUBSCRIPTION_VARIABLE_NODE)) {
            ssNodes[1] = properties.get(SUBSUBSCRIPTION_SUBJECT_VALUE_PROPERTY);
        }

        if (!properties.get(SUBSUBSCRIPTION_PREDICATE_VALUE_PROPERTY).equals(
                PublishSubscribeConstants.SUBSCRIPTION_VARIABLE_NODE)) {
            ssNodes[2] =
                    properties.get(SUBSUBSCRIPTION_PREDICATE_VALUE_PROPERTY);
        }

        if (!properties.get(SUBSUBSCRIPTION_OBJECT_VALUE_PROPERTY).equals(
                PublishSubscribeConstants.SUBSCRIPTION_VARIABLE_NODE)) {
            ssNodes[3] = properties.get(SUBSUBSCRIPTION_OBJECT_VALUE_PROPERTY);
        }

        return new Subsubscription(
                originalId,
                subscriptionId,
                SubscriptionId.parseSubscriptionId(PublishSubscribeUtils.extractSubscriptionId(subSubscriptionIdNode.getURI())),
                (Integer) properties.get(SUBSUBSCRIPTION_INDEX_PROPERTY)
                        .getLiteralValue(), ssNodes[0], ssNodes[1], ssNodes[2],
                ssNodes[3]);
    }

    private static Quadruple createVarNamesQuadruple(Node originalSubscriptionURI,
                                                     Node subSubscriptionURI,
                                                     AtomicQuery atomicQuery) {
        return new Quadruple(
                originalSubscriptionURI, subSubscriptionURI,
                SUBSUBSCRIPTION_VAR_NAMES_NODE,
                Node.createLiteral(atomicQuery.getVarNamesAsString()));
    }

    private static Node replaceVarNodeByConstant(Node node) {
        if (node.isVariable()) {
            return PublishSubscribeConstants.SUBSCRIPTION_VARIABLE_NODE;
        }

        return node;
    }

}
