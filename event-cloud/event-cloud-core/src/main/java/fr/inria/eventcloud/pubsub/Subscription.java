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

import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_CREATION_DATETIME_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_CREATION_DATETIME_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_DESTINATION_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_DESTINATION_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_HAS_SUBSUBSCRIPTION_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_ID_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_ID_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_INDEXATION_DATETIME_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_INDEXATION_DATETIME_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_INDEXED_WITH_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_NS_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_ORIGINAL_ID_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_ORIGINAL_ID_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_PARENT_ID_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_PARENT_ID_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_SERIALIZED_VALUE_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_SPARQL_QUERY_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_STUB_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_SUBSCRIBER_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_SUBSCRIBER_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_TYPE_PROPERTY;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.xml.bind.DatatypeConverter;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashCodes;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.Var;

import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruplable;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.NotificationListenerType;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.QuadrupleIterator;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.proxies.SubscribeProxy;
import fr.inria.eventcloud.proxies.SubscribeProxyImpl;
import fr.inria.eventcloud.reasoner.AtomicQuery;
import fr.inria.eventcloud.reasoner.SparqlDecomposer;

/**
 * A subscription is a continuous query that is registered into an Event Cloud.
 * Each time a new event that matches the query is published, a notification is
 * sent to the subscriber.
 * 
 * @author lpellegr
 */
public class Subscription implements Quadruplable, Serializable {

    private static final long serialVersionUID = 1L;

    public static final LoadingCache<String, SubscribeProxy> subscribeProxiesCache =
            CacheBuilder.newBuilder()
                    .softValues()
                    .maximumSize(
                            EventCloudProperties.SUBSCRIBE_PROXIES_CACHE_MAXIMUM_SIZE.getValue())
                    .build(new CacheLoader<String, SubscribeProxy>() {
                        @Override
                        public SubscribeProxy load(String subscriberUrl)
                                throws Exception {
                            return SubscribeProxyImpl.lookup(subscriberUrl);
                        }
                    });

    // the id of the subscription before someone rewrites it
    private final SubscriptionId originalId;

    // the id of the subscription identifying the subscription such that when it
    // is rewritten with the right quadruples, it gives the current subscription
    private final SubscriptionId parentId;

    // the id of the current subscription
    private final SubscriptionId id;

    private final long creationTime;

    private final long indexationTime;

    private final String sparqlQuery;

    private final String subscriberUrl;

    private final String subscriptionDestination;

    private final List<Stub> stubs;

    private final NotificationListenerType type;

    // the following fields are transient because they can be
    // created from the sparqlQuery on the fly
    private transient Set<Var> resultVars;

    private transient Subsubscription[] subSubscriptions;

    // the var name associated to the graph value
    private transient Node graphNode;

    public Subscription(SubscriptionId originalId, SubscriptionId parentId,
            SubscriptionId id, long indexationTime, String sparqlQuery,
            String subscribeProxyUrl, NotificationListenerType listenerType) {
        this(originalId, parentId, id, System.currentTimeMillis(),
                indexationTime, sparqlQuery, subscribeProxyUrl, null,
                listenerType);
    }

    public Subscription(SubscriptionId originalId, SubscriptionId parentId,
            SubscriptionId id, long indexationTime, String sparqlQuery,
            String subscribeProxyUrl, String subscriptionDestination,
            NotificationListenerType listenerType) {
        this(originalId, parentId, id, System.currentTimeMillis(),
                indexationTime, sparqlQuery, subscribeProxyUrl,
                subscriptionDestination, listenerType);
    }

    private Subscription(SubscriptionId originalId, SubscriptionId parentId,
            SubscriptionId id, long creationTime, long indexationTime,
            String sparqlQuery, String subscriberUrl,
            String subscriptionDestination,
            NotificationListenerType listenerType) {
        this.originalId = originalId;
        this.parentId = parentId;
        this.id = id;
        this.creationTime = creationTime;
        this.indexationTime = indexationTime;
        this.sparqlQuery = sparqlQuery;
        this.subscriberUrl = subscriberUrl;
        this.subscriptionDestination = subscriptionDestination;
        this.type = listenerType;
        this.stubs = new ArrayList<Stub>();
    }

    public final void addStub(Stub stub) {
        this.stubs.add(stub);
    }

    public static final Subscription parseFrom(TransactionalTdbDatastore datastore,
                                               SubscriptionId id) {
        // contains the data about the subscription itself
        Map<String, Node> basicInfo = new HashMap<String, Node>();
        // contains the identifier of the sub-subscriptions
        List<Node> subSubscriptionIds = new ArrayList<Node>();
        // contains the stub urls associated to the subscription
        List<String> stubs = new ArrayList<String>();

        TransactionalDatasetGraph txnGraph =
                datastore.begin(AccessMode.READ_ONLY);

        try {
            QuadrupleIterator it =
                    txnGraph.find(
                            Node.ANY,
                            PublishSubscribeUtils.createSubscriptionIdUri(id),
                            Node.ANY, Node.ANY);

            while (it.hasNext()) {
                Quadruple quad = it.next();

                if (quad.getPredicate().equals(
                        SUBSCRIPTION_HAS_SUBSUBSCRIPTION_NODE)) {
                    subSubscriptionIds.add(quad.getObject());
                } else if (quad.getPredicate().equals(SUBSCRIPTION_STUB_NODE)) {
                    stubs.add(quad.getObject().getLiteralLexicalForm());
                } else {
                    basicInfo.put(
                            quad.getPredicate().toString(), quad.getObject());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }

        SubscriptionId parentId = null;
        if (basicInfo.get(SUBSCRIPTION_PARENT_ID_PROPERTY) != null) {
            parentId =
                    SubscriptionId.parseSubscriptionId(basicInfo.get(
                            SUBSCRIPTION_PARENT_ID_PROPERTY)
                            .getLiteralLexicalForm());
        }

        SubscriptionId originalId = null;
        if (basicInfo.get(SUBSCRIPTION_ORIGINAL_ID_PROPERTY) != null) {
            originalId =
                    SubscriptionId.parseSubscriptionId(PublishSubscribeUtils.extractSubscriptionId(basicInfo.get(
                            SUBSCRIPTION_ORIGINAL_ID_PROPERTY)
                            .getURI()));
        }

        String subscriptionDestination = null;
        if (basicInfo.get(SUBSCRIPTION_DESTINATION_PROPERTY) != null) {
            subscriptionDestination =
                    basicInfo.get(SUBSCRIPTION_DESTINATION_PROPERTY)
                            .getLiteralLexicalForm();
        }

        Subscription subscription =
                new Subscription(
                        originalId,
                        parentId,
                        SubscriptionId.parseSubscriptionId(basicInfo.get(
                                SUBSCRIPTION_ID_PROPERTY)
                                .getLiteralLexicalForm()),
                        DatatypeConverter.parseDateTime(
                                basicInfo.get(
                                        SUBSCRIPTION_CREATION_DATETIME_PROPERTY)
                                        .getLiteralLexicalForm())
                                .getTimeInMillis(),
                        DatatypeConverter.parseDateTime(
                                basicInfo.get(
                                        SUBSCRIPTION_INDEXATION_DATETIME_PROPERTY)
                                        .getLiteralLexicalForm())
                                .getTimeInMillis(),
                        basicInfo.get(SUBSCRIPTION_SPARQL_QUERY_PROPERTY)
                                .getLiteralLexicalForm(),
                        basicInfo.get(SUBSCRIPTION_SUBSCRIBER_PROPERTY)
                                .getLiteralLexicalForm(),
                        subscriptionDestination,
                        NotificationListenerType.UNKNOWN.convert(((Integer) basicInfo.get(
                                SUBSCRIPTION_TYPE_PROPERTY)
                                .getLiteralValue()).shortValue()));

        // retrieves the stub urls
        for (String stub : stubs) {
            String[] parsedStub = stub.split(" ");
            subscription.addStub(new Stub(
                    parsedStub[1], fromString(parsedStub[0])));
        }

        // recreates the sub-subscriptions
        subscription.subSubscriptions =
                new Subsubscription[subSubscriptionIds.size()];

        for (Node subSubscriptionIdNode : subSubscriptionIds) {
            Subsubscription s =
                    Subsubscription.parseFrom(
                            datastore,
                            SubscriptionId.parseSubscriptionId(basicInfo.get(
                                    SUBSCRIPTION_ID_PROPERTY)
                                    .getLiteralLexicalForm()),
                            subSubscriptionIdNode);
            subscription.subSubscriptions[s.getIndex()] = s;
        }

        return subscription;
    }

    private static HashCode fromString(String hashCode) {
        return HashCodes.fromBytes(DatatypeConverter.parseHexBinary(hashCode));
    }

    /**
     * Returns the id of the parent subscription before someone rewrites it.
     * This means that if the subscription has not been rewritten, a call to
     * {@link #getId()} will return the same value as this method.
     * 
     * @return the id of the parent subscription before someone rewrites it.
     */
    public SubscriptionId getOriginalId() {
        return this.originalId;
    }

    /**
     * Returns the previous parent identifier. If the value is {@code null}, it
     * means that this subscription has no parent.
     * 
     * @return the previous parent identifier.
     */
    public SubscriptionId getParentId() {
        return this.parentId;
    }

    /**
     * Returns an identifier that uniquely identify the subscription.
     * 
     * @return an identifier that uniquely identify the subscription.
     */
    public SubscriptionId getId() {
        return this.id;
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public long getIndexationTime() {
        return this.indexationTime;
    }

    /**
     * Returns the subscriber URL. This URL is the registered reference of the
     * SubscribeProxy component associated to the subscriber.
     * 
     * @return the subscriber URL. This URL is the registered reference of the
     *         SubscribeProxy component associated to the subscriber.
     */
    public String getSubscriberUrl() {
        return this.subscriberUrl;
    }

    /**
     * Returns the subscription destination. The destination is assumed to be an
     * URL representing the endpoint of the subscriber.
     * 
     * @return the subscription destination.
     */
    public String getSubscriptionDestination() {
        return this.subscriptionDestination;
    }

    /**
     * Returns the type identifying the notification listener which is used to
     * deliver notifications for this subscription.
     * 
     * @return the type identifying the notification listener which is used to
     *         deliver notifications for this subscription.
     */
    public NotificationListenerType getType() {
        return this.type;
    }

    /**
     * Returns the {@link SubscribeProxy} associated to the original subscriber.
     * 
     * @return the {@link SubscribeProxy} associated to the original subscriber.
     * 
     * @throws ExecutionException
     *             when the lookup operation fails.
     */
    public SubscribeProxy getSubscriberProxy() throws ExecutionException {
        return subscribeProxiesCache.get(this.subscriberUrl);
    }

    public String getSparqlQuery() {
        return this.sparqlQuery;
    }

    public List<Stub> getStubs() {
        return this.stubs;
    }

    public synchronized Subsubscription[] getSubSubscriptions() {
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

    public synchronized Set<Var> getResultVars() {
        if (this.resultVars == null) {
            this.resultVars = new HashSet<Var>();
            for (String varName : QueryFactory.create(this.sparqlQuery)
                    .getResultVars()) {
                this.resultVars.add(Var.alloc(varName));
            }
        }

        return this.resultVars;
    }

    public synchronized Node getGraphNode() {
        if (this.graphNode == null) {
            this.graphNode =
                    this.getSubSubscriptions()[0].getAtomicQuery().getGraph();
        }

        return this.graphNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> toQuadruples() {
        List<Quadruple> quads = new ArrayList<Quadruple>();

        Node subscriptionURI =
                PublishSubscribeUtils.createSubscriptionIdUri(this.id);

        quads.add(new Quadruple(
                SUBSCRIPTION_NS_NODE, subscriptionURI, SUBSCRIPTION_ID_NODE,
                Node.createLiteral(this.id.toString()), false, false));

        if (this.parentId != null) {
            quads.add(new Quadruple(
                    SUBSCRIPTION_NS_NODE, subscriptionURI,
                    SUBSCRIPTION_PARENT_ID_NODE,
                    Node.createLiteral(this.parentId.toString()), false, false));
        }

        if (this.originalId != null) {
            quads.add(new Quadruple(
                    SUBSCRIPTION_NS_NODE,
                    subscriptionURI,
                    SUBSCRIPTION_ORIGINAL_ID_NODE,
                    PublishSubscribeUtils.createSubscriptionIdUri(this.originalId),
                    false, false));
        }

        quads.add(new Quadruple(
                SUBSCRIPTION_NS_NODE, subscriptionURI,
                SUBSCRIPTION_SERIALIZED_VALUE_NODE,
                Node.createLiteral(this.sparqlQuery), false, false));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(this.creationTime);
        quads.add(new Quadruple(
                SUBSCRIPTION_NS_NODE, subscriptionURI,
                SUBSCRIPTION_CREATION_DATETIME_NODE, Node.createLiteral(
                        DatatypeConverter.printDateTime(calendar),
                        XSDDatatype.XSDdateTime), false, false));

        quads.add(new Quadruple(
                SUBSCRIPTION_NS_NODE, subscriptionURI,
                PublishSubscribeConstants.SUBSCRIPTION_TYPE_NODE,
                Node.createLiteral(
                        Short.toString(this.type.convert()),
                        XSDDatatype.XSDshort), false, false));

        calendar.setTimeInMillis(this.indexationTime);
        quads.add(new Quadruple(
                SUBSCRIPTION_NS_NODE, subscriptionURI,
                SUBSCRIPTION_INDEXATION_DATETIME_NODE, Node.createLiteral(
                        DatatypeConverter.printDateTime(calendar),
                        XSDDatatype.XSDdateTime), false, false));

        quads.add(new Quadruple(
                SUBSCRIPTION_NS_NODE, subscriptionURI,
                SUBSCRIPTION_SUBSCRIBER_NODE,
                Node.createLiteral(this.subscriberUrl), false, false));

        if (this.subscriptionDestination != null) {
            quads.add(new Quadruple(
                    SUBSCRIPTION_NS_NODE, subscriptionURI,
                    SUBSCRIPTION_DESTINATION_NODE,
                    Node.createLiteral(this.subscriptionDestination), false,
                    false));
        }

        quads.add(new Quadruple(
                SUBSCRIPTION_NS_NODE, subscriptionURI,
                SUBSCRIPTION_INDEXED_WITH_NODE,
                Node.createLiteral(this.getSubSubscriptions()[0].getId()
                        .toString()), false, false));

        for (Stub stub : this.stubs) {
            quads.add(new Quadruple(
                    SUBSCRIPTION_NS_NODE, subscriptionURI,
                    SUBSCRIPTION_STUB_NODE,
                    Node.createLiteral(stub.quadrupleHash.toString() + " "
                            + stub.peerUrl), false, false));
        }

        for (Subsubscription ssubscription : this.getSubSubscriptions()) {
            quads.add(new Quadruple(
                    SUBSCRIPTION_NS_NODE,
                    subscriptionURI,
                    SUBSCRIPTION_HAS_SUBSUBSCRIPTION_NODE,
                    // Node.createLiteral(ssubscription.getId().toString()),
                    PublishSubscribeUtils.createSubSubscriptionIdUri(ssubscription.getId()),
                    false, false));
            quads.addAll(ssubscription.toQuadruples());
        }

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
        return obj instanceof Subscription
                && this.id.equals(((Subscription) obj).id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Subscription [originalId=" + this.originalId + ", parentId="
                + this.parentId + ", id=" + this.id + ", creationTime="
                + this.creationTime + ", indexationTime=" + this.indexationTime
                + ", subscriberUrl=" + this.subscriberUrl
                + ", subscription destination=" + this.subscriptionDestination
                + ", sparqlQuery=" + this.sparqlQuery + ", stubs=" + this.stubs
                + ", type=" + this.type + "]";
    }

    public static final class Stub implements Serializable {

        private static final long serialVersionUID = 1L;

        // the url which identifies the peer to visit
        public final String peerUrl;

        // hash value that identifies the quadruple to retrieve
        public final HashCode quadrupleHash;

        public Stub(String peerUrl, HashCode quadrupleHashValue) {
            this.peerUrl = peerUrl;
            this.quadrupleHash = quadrupleHashValue;
        }

    }

    public static void main(String[] args) {
        SubscriptionId id = new SubscriptionId();
        Subscription subscription =
                new Subscription(
                        id, null, id, System.currentTimeMillis(),
                        "SELECT ?g WHERE { GRAPH ?g { ?s ?p ?o }}",
                        "http://dummy.com", NotificationListenerType.BINDING);

        System.out.println(subscription.toQuadruples());
    }

}
