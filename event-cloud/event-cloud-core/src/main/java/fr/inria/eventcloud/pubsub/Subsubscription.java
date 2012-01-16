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

import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_NS_NODE;
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

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Rdfable;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.datastore.VariableDatatype;
import fr.inria.eventcloud.reasoner.AtomicQuery;
import fr.inria.eventcloud.reasoner.AtomicQuery.ParentQueryForm;

/**
 * A Sub-subscription is modeled by using an {@link AtomicQuery} that knows who
 * is its parent and what is its position in the parent query.
 * 
 * @author lpellegr
 */
public class Subsubscription implements Rdfable {

    private final SubscriptionId parentId;

    private final SubscriptionId id;

    private final int index;

    private final AtomicQuery atomicQuery;

    public Subsubscription(SubscriptionId parentId, AtomicQuery atomicQuery,
            int index) {
        this.parentId = parentId;
        this.atomicQuery = atomicQuery;
        this.index = index;
        this.id = new SubscriptionId();
    }

    private Subsubscription(SubscriptionId parentId, SubscriptionId id,
            int index, Node graph, Node subject, Node predicate, Node object) {
        this.parentId = parentId;
        this.id = id;
        this.index = index;
        this.atomicQuery =
                new AtomicQuery(
                        ParentQueryForm.SELECT, graph, subject, predicate,
                        object);
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
    public Collection<Quadruple> toQuadruples() {
        Collection<Quadruple> quads = new Collection<Quadruple>();
        Node subSubscriptionURI =
                Node.createURI(SUBSUBSCRIPTION_NS + this.id.toString());

        quads.add(new Quadruple(
                SUBSCRIPTION_NS_NODE, subSubscriptionURI,
                SUBSUBSCRIPTION_ID_NODE,
                Node.createLiteral(this.id.toString()), false, false));

        quads.add(new Quadruple(
                SUBSCRIPTION_NS_NODE, subSubscriptionURI,
                SUBSUBSCRIPTION_INDEX_NODE, Node.createLiteral(
                        Integer.toString(this.index), XSDDatatype.XSDint),
                false, false));

        quads.add(new Quadruple(
                SUBSCRIPTION_NS_NODE,
                subSubscriptionURI,
                SUBSUBSCRIPTION_GRAPH_VALUE_NODE,
                replaceVarNodeByVariableTypedLiteral(this.atomicQuery.getGraph()),
                false, false));

        quads.add(new Quadruple(
                SUBSCRIPTION_NS_NODE,
                subSubscriptionURI,
                SUBSUBSCRIPTION_SUBJECT_VALUE_NODE,
                replaceVarNodeByVariableTypedLiteral(this.atomicQuery.getSubject()),
                false, false));

        quads.add(new Quadruple(
                SUBSCRIPTION_NS_NODE,
                subSubscriptionURI,
                SUBSUBSCRIPTION_PREDICATE_VALUE_NODE,
                replaceVarNodeByVariableTypedLiteral(this.atomicQuery.getPredicate()),
                false, false));

        quads.add(new Quadruple(
                SUBSCRIPTION_NS_NODE,
                subSubscriptionURI,
                SUBSUBSCRIPTION_OBJECT_VALUE_NODE,
                replaceVarNodeByVariableTypedLiteral(this.atomicQuery.getObject()),
                false, false));

        return quads;
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

    /**
     * Parses a {@link Subsubscription} from the specified
     * {@link TransactionalTdbDatastore} by using the specified
     * {@code subscriptionId} and {@code subSubscriptionId}.
     * 
     * @param datastore
     *            the datastore where the information about the sub subscription
     *            are stored.
     * @param subscriptionId
     *            the subscriptionId which is the parent id for the
     *            {@code subSubscriptionId}
     * @param subSubscriptionId
     *            the identifier of the sub subscription.
     * 
     * @return the sub subscription which has been parsed.
     */
    public static final Subsubscription parseFrom(TransactionalTdbDatastore datastore,
                                                  SubscriptionId subscriptionId,
                                                  SubscriptionId subSubscriptionId) {
        // contains the properties and their associated values that are read
        // from the datastore for the given subSubscriptionId
        Map<String, Node> properties = new HashMap<String, Node>();

        TransactionalDatasetGraph txnGraph =
                datastore.begin(AccessMode.READ_ONLY);
        for (Quadruple quad : txnGraph.find(
                Node.ANY,
                PublishSubscribeUtils.createSubSubscriptionIdUrl(subSubscriptionId),
                Node.ANY, Node.ANY)) {
            properties.put(quad.getPredicate().toString(), quad.getObject());
        }
        txnGraph.close();

        return new Subsubscription(
                subscriptionId,
                subSubscriptionId,
                (Integer) properties.get(SUBSUBSCRIPTION_INDEX_PROPERTY)
                        .getLiteralValue(),
                // when they are serialized, variables are serialized as
                // typed-literal with VariableDatatype datatype. To get a
                // Node_Variable we have to get the parsed literal value but
                // only if the serialized value was a variable
                replaceVariableTypedLiteralByVarNode(properties.get(SUBSUBSCRIPTION_GRAPH_VALUE_PROPERTY)),
                replaceVariableTypedLiteralByVarNode(properties.get(SUBSUBSCRIPTION_SUBJECT_VALUE_PROPERTY)),
                replaceVariableTypedLiteralByVarNode(properties.get(SUBSUBSCRIPTION_PREDICATE_VALUE_PROPERTY)),
                replaceVariableTypedLiteralByVarNode(properties.get(SUBSUBSCRIPTION_OBJECT_VALUE_PROPERTY)));
    }

    private static final Node replaceVarNodeByVariableTypedLiteral(Node node) {
        if (node.isVariable()) {
            return Node.createLiteral(
                    node.getName(), VariableDatatype.getInstance());
        }

        return node;
    }

    private static final Node replaceVariableTypedLiteralByVarNode(Node node) {
        if (node.isLiteral()
                && node.getLiteralDatatype().equals(
                        VariableDatatype.getInstance())) {
            return (Node) node.getLiteralValue();
        }

        return node;
    }

}
