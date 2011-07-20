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

import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.SUBSCRIPTION_CREATION_DATETIME_NODE;
import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.SUBSCRIPTION_CREATION_DATETIME_PROPERTY;
import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.SUBSCRIPTION_HAS_SUBSUBSCRIPTION_NODE;
import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.SUBSCRIPTION_ID_NODE;
import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.SUBSCRIPTION_ID_PROPERTY;
import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.SUBSCRIPTION_INDEXED_WITH_NODE;
import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.SUBSCRIPTION_NS;
import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.SUBSCRIPTION_NS_NODE;
import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.SUBSCRIPTION_PARENT_ID_NODE;
import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.SUBSCRIPTION_PARENT_ID_PROPERTY;
import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.SUBSCRIPTION_SERIALIZED_VALUE_NODE;
import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.SUBSCRIPTION_SPARQL_QUERY_PROPERTY;
import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.SUBSCRIPTION_SUBSCRIBER_NODE;
import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.SUBSCRIPTION_SUBSCRIBER_PROPERTY;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import com.google.common.base.Objects;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Rdfable;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.datastore.SemanticDatastore;
import fr.inria.eventcloud.reasoner.AtomicQuery;
import fr.inria.eventcloud.reasoner.SparqlDecomposer;
import fr.inria.eventcloud.utils.MurmurHash;

/**
 * A subscription is a continuous query that is registered into the Event-Cloud.
 * Each time a new event that matches the query is published, a notification is
 * sent to the subscriber.
 * 
 * @author lpellegr
 */
public class Subscription implements Rdfable, Serializable {

    private static final long serialVersionUID = 1L;

    private final SubscriptionId parentId;

    private final SubscriptionId id;

    private final long creationTime;

    private final String source;

    private final String sparqlQuery;

    // this field is transient because the subSubscriptions can be created from
    // the sparqlQuery on the fly
    private transient Subsubscription[] subSubscriptions;

    public Subscription(String source, String sparqlQuery) {
        this(null, source, sparqlQuery);
    }

    public Subscription(SubscriptionId parentId, String source,
            String sparqlQuery) {
        if (!sparqlQuery.contains("GRAPH")) {
            throw new IllegalArgumentException(
                    "The SPARQL query used for a subscription must always contain a GRAPH pattern.");
        }

        if (!sparqlQuery.contains("SELECT")) {
            throw new IllegalArgumentException(
                    "The SPARQL query used for a subscription must always use the SELECT query form.");
        }

        this.creationTime = System.currentTimeMillis();
        this.parentId = parentId;
        this.id =
                new SubscriptionId(MurmurHash.hash64(
                        source, sparqlQuery, Long.toString(this.creationTime)));
        this.source = source;
        this.sparqlQuery = sparqlQuery;
    }

    private Subscription(SubscriptionId parentId, SubscriptionId id,
            long creationTime, String source, String sparqlQuery) {
        this.parentId = parentId;
        this.id = id;
        this.creationTime = creationTime;
        this.source = source;
        this.sparqlQuery = sparqlQuery;
    }

    public static final Subscription parseSubscription(SemanticDatastore datastore,
                                                       SubscriptionId id) {
        Collection<Quadruple> quads =
                datastore.find(Node.ANY, Node.createURI(SUBSCRIPTION_NS
                        + id.toString()), Node.ANY, Node.ANY);

        // contains the data about the subscription itself
        Map<String, Node> basicInfo = new HashMap<String, Node>();
        // contains the identifier of the sub-subscriptions
        List<String> subSubscriptionsId = new ArrayList<String>();

        for (Quadruple quad : quads) {
            if (quad.getPredicate().equals(
                    SUBSCRIPTION_HAS_SUBSUBSCRIPTION_NODE)) {
                subSubscriptionsId.add(quad.getObject()
                        .getLiteralValue()
                        .toString());
            } else {
                basicInfo.put(quad.getPredicate().toString(), quad.getObject());
            }
        }

        SubscriptionId parentId = null;
        if (basicInfo.get(SUBSCRIPTION_PARENT_ID_PROPERTY) != null) {
            parentId =
            // we cannot use getLiteralValue in order to directly retrieve a
            // Long because it will return an Integer due to the fact that Jena
            // encodes internally all number with 56 bits :(
                    SubscriptionId.parseSubscriptionId(basicInfo.get(
                            SUBSCRIPTION_PARENT_ID_PROPERTY)
                            .getLiteralLexicalForm());
        }

        Subscription subscription =
                new Subscription(
                        parentId,
                        SubscriptionId.parseSubscriptionId(basicInfo.get(
                                SUBSCRIPTION_ID_PROPERTY)
                                .getLiteralLexicalForm()),
                        DatatypeConverter.parseDateTime(
                                basicInfo.get(
                                        SUBSCRIPTION_CREATION_DATETIME_PROPERTY)
                                        .getLiteralLexicalForm())
                                .getTimeInMillis(), basicInfo.get(
                                SUBSCRIPTION_SUBSCRIBER_PROPERTY)
                                .getLiteralLexicalForm(), basicInfo.get(
                                SUBSCRIPTION_SPARQL_QUERY_PROPERTY)
                                .getLiteralLexicalForm());

        // recreates the sub-subscriptions
        subscription.subSubscriptions =
                new Subsubscription[subSubscriptionsId.size()];

        for (int i = 0; i < subSubscriptionsId.size(); i++) {
            Subsubscription s =
                    Subsubscription.parseSubsubscription(
                            datastore, basicInfo.get(SUBSCRIPTION_ID_PROPERTY)
                                    .getLiteralLexicalForm(),
                            subSubscriptionsId.get(i));
            subscription.subSubscriptions[s.getIndex()] = s;
        }

        return subscription;
    }

    /**
     * Returns the parent identifier. If the value is {@code null}, it means
     * that this subscription has no parent.
     * 
     * @return the parent identifier.
     */
    public SubscriptionId getParentId() {
        return this.parentId;
    }

    public SubscriptionId getId() {
        return this.id;
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public String getSource() {
        return this.source;
    }

    public String getSparqlQuery() {
        return this.sparqlQuery;
    }

    public Subsubscription[] getSubSubscriptions() {
        if (this.subSubscriptions == null) {
            List<AtomicQuery> subQueries =
                    new SparqlDecomposer().decompose(this.sparqlQuery);
            this.subSubscriptions = new Subsubscription[subQueries.size()];
            for (int i = 0; i < subQueries.size(); i++) {
                this.subSubscriptions[i] =
                        new Subsubscription(this.id, subQueries.get(i), i);
            }
        }

        return this.subSubscriptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> toQuadruples() {
        Collection<Quadruple> quads = new Collection<Quadruple>();
        Node subscriptionURI =
                Node.createURI(SUBSCRIPTION_NS + this.id.toString());

        // the output is something which is similar to what is described at
        // http://code.google.com/p/event-cloud/wiki/PublishSubscribeRdfFormat
        quads.add(new Quadruple(
                SUBSCRIPTION_NS_NODE, subscriptionURI, SUBSCRIPTION_ID_NODE,
                Node.createLiteral(
                        this.id.toString(), null, XSDDatatype.XSDlong)));

        if (this.parentId != null) {
            quads.add(new Quadruple(
                    SUBSCRIPTION_NS_NODE,
                    subscriptionURI,
                    SUBSCRIPTION_PARENT_ID_NODE,
                    Node.createLiteral(
                            this.parentId.toString(), null, XSDDatatype.XSDlong)));
        }

        quads.add(new Quadruple(
                SUBSCRIPTION_NS_NODE, subscriptionURI,
                SUBSCRIPTION_SERIALIZED_VALUE_NODE,
                Node.createLiteral(this.sparqlQuery)));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(this.creationTime);
        quads.add(new Quadruple(
                SUBSCRIPTION_NS_NODE, subscriptionURI,
                SUBSCRIPTION_CREATION_DATETIME_NODE, Node.createLiteral(
                        DatatypeConverter.printDateTime(calendar), null,
                        XSDDatatype.XSDdateTime)));

        quads.add(new Quadruple(
                SUBSCRIPTION_NS_NODE, subscriptionURI,
                SUBSCRIPTION_SUBSCRIBER_NODE, Node.createLiteral(this.source)));

        quads.add(new Quadruple(
                SUBSCRIPTION_NS_NODE, subscriptionURI,
                SUBSCRIPTION_INDEXED_WITH_NODE, Node.createLiteral(
                        this.getSubSubscriptions()[0].getId().toString(), null,
                        XSDDatatype.XSDlong)));

        for (Subsubscription ssubscription : this.getSubSubscriptions()) {
            quads.add(new Quadruple(
                    SUBSCRIPTION_NS_NODE, subscriptionURI,
                    SUBSCRIPTION_HAS_SUBSUBSCRIPTION_NODE, Node.createLiteral(
                            ssubscription.getId().toString(), null,
                            XSDDatatype.XSDlong)));
            quads.addAll(ssubscription.toQuadruples());
        }

        return quads;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(
                this.parentId, this.id, this.creationTime, this.source,
                this.sparqlQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Subscription) {
            Subscription s = (Subscription) obj;
            return this.id.equals(s.id) && this.parentId.equals(s.parentId)
                    && this.creationTime == s.creationTime
                    && this.source.equals(s.source)
                    && this.sparqlQuery.equals(s.sparqlQuery);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Subscription [id=" + this.id + ", creationTime="
                + this.creationTime + ", source=" + this.source
                + ", sparqlQuery=" + this.sparqlQuery + "]";
    }

}
