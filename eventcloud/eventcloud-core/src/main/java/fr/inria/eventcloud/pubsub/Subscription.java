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
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_ORIGINAL_ID_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_ORIGINAL_ID_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_PARENT_ID_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_PARENT_ID_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_PEER_REFERENCES_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_SERIALIZED_VALUE_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_SPARQL_QUERY_PROPERTY;
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

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;

import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.SetMultimap;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashCodes;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
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
import fr.inria.eventcloud.exceptions.DecompositionException;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.formatters.QuadruplesFormatter;
import fr.inria.eventcloud.proxies.SubscribeProxy;
import fr.inria.eventcloud.reasoner.AtomicQuery;
import fr.inria.eventcloud.reasoner.SparqlDecomposer;

/**
 * A subscription is a continuous query that is registered into an EventCloud.
 * Each time a new event that matches the query is published, a notification is
 * sent to the subscriber.
 * 
 * @author lpellegr
 */
public class Subscription implements Quadruplable, Serializable {

    private static final long serialVersionUID = 160L;

    public static final LoadingCache<String, SubscribeProxy> SUBSCRIBE_PROXIES_CACHE;

    static {
        CacheBuilder<Object, Object> cacheBuilder =
                CacheBuilder.newBuilder()
                        .concurrencyLevel(
                                P2PStructuredProperties.MAO_SOFT_LIMIT_PEERS.getValue())
                        .softValues()
                        .maximumSize(
                                EventCloudProperties.SUBSCRIBE_PROXIES_CACHE_MAXIMUM_SIZE.getValue());

        if (EventCloudProperties.RECORD_STATS_SUBSCRIPTIONS_CACHE.getValue()) {
            cacheBuilder.recordStats();
        }

        SUBSCRIBE_PROXIES_CACHE =
                cacheBuilder.build(new CacheLoader<String, SubscribeProxy>() {
                    @Override
                    public SubscribeProxy load(String subscriberUrl)
                            throws Exception {
                        return (SubscribeProxy) ProxyFactory.lookupSubscribeProxy(subscriberUrl);
                    }
                });
    }

    // the id of the subscription before someone rewrites it
    private final SubscriptionId originalId;

    // the id of the subscription identifying the subscription such that when it
    // is rewritten with the right quadruples, it gives the current subscription
    private final SubscriptionId parentId;

    // the id of the current subscription
    private final SubscriptionId id;

    private final long creationTime;

    private long indexationTime;

    private final String sparqlQuery;

    private final String subscriberUrl;

    private final String subscriptionDestination;

    // peers to visit when the subscription is associated to a binding listener
    // in order to retrieve intermediate results
    private SetMultimap<String, HashCode> intermediatePeerReferences;

    private final NotificationListenerType type;

    // the following fields are transient because they can be
    // created from the sparqlQuery on the fly
    private transient Set<Var> resultVars;

    private transient Subsubscription[] subSubscriptions;

    // the var name associated to the graph value
    private transient Node graphNode;

    public Subscription(SubscriptionId originalId, SubscriptionId parentId,
            SubscriptionId id, long creationTime, String sparqlQuery,
            String subscriberUrl, String subscriptionDestination,
            NotificationListenerType listenerType) {
        this(originalId, parentId, id, creationTime, -1, sparqlQuery,
                subscriberUrl, subscriptionDestination, listenerType);
    }

    public Subscription(SubscriptionId originalId, SubscriptionId parentId,
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
    }

    public synchronized void addIntermediatePeerReference(String peerURL,
                                                          HashCode hashValue) {
        if (this.type != NotificationListenerType.BINDING) {
            throw new IllegalStateException(
                    "Trying to add an intermediate peer reference on a subscription that does not use a binding listener");
        }

        this.getOrCreateIntermediatePeerReferencesCollection().put(
                peerURL, hashValue);
    }

    public synchronized SetMultimap<String, HashCode> getIntermediatePeerReferences() {
        return this.intermediatePeerReferences;
    }

    private SetMultimap<String, HashCode> getOrCreateIntermediatePeerReferencesCollection() {
        if (this.intermediatePeerReferences == null) {
            this.intermediatePeerReferences = HashMultimap.create();
        }

        return this.intermediatePeerReferences;
    }

    public static final Subscription parseFrom(TransactionalTdbDatastore datastore,
                                               SubscriptionId id) {
        TransactionalDatasetGraph txnGraph =
                datastore.begin(AccessMode.READ_ONLY);

        try {
            return parseFrom(txnGraph, id);
        } finally {
            txnGraph.end();
        }
    }

    public static final Subscription parseFrom(TransactionalDatasetGraph txnGraph,
                                               SubscriptionId id) {
        // contains the data about the subscription itself
        Map<String, Node> basicInfo = new HashMap<String, Node>();
        // contains the identifier of the sub-subscriptions
        List<Node> subSubscriptionIds = new ArrayList<Node>();
        // contains the stub urls associated to the subscription
        String peerReferences = null;

        QuadrupleIterator it =
                txnGraph.find(
                        Node.ANY,
                        PublishSubscribeUtils.createSubscriptionIdUri(id),
                        Node.ANY, Node.ANY);

        // no subscription found for the specified subscription id
        if (!it.hasNext()) {
            return null;
        }

        while (it.hasNext()) {
            Quadruple quad = it.next();

            if (quad.getPredicate().equals(
                    SUBSCRIPTION_HAS_SUBSUBSCRIPTION_NODE)) {
                subSubscriptionIds.add(quad.getObject());
            } else if (quad.getPredicate().equals(
                    SUBSCRIPTION_PEER_REFERENCES_NODE)) {
                peerReferences = quad.getObject().getLiteralLexicalForm();
            } else {
                basicInfo.put(quad.getPredicate().toString(), quad.getObject());
            }
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
                                .getURI(),
                        subscriptionDestination,
                        NotificationListenerType.BINDING.convert(((Integer) basicInfo.get(
                                SUBSCRIPTION_TYPE_PROPERTY)
                                .getLiteralValue()).shortValue()));

        // re-insert the intermediate peer references
        if (peerReferences != null) {
            intermediatePeerReferencesFromString(subscription, peerReferences);
        }

        // recreates the sub-subscriptions
        subscription.subSubscriptions =
                new Subsubscription[subSubscriptionIds.size()];

        for (Node subSubscriptionIdNode : subSubscriptionIds) {
            Subsubscription s =
                    Subsubscription.parseFrom(
                            txnGraph,
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

    /**
     * Returns the creation time of the subscription.
     * 
     * @return the creation time of the subscription.
     */
    public long getCreationTime() {
        return this.creationTime;
    }

    /**
     * Returns the indexation time of the subscription.
     * 
     * @return the indexation time of the subscription.
     */
    public long getIndexationTime() {
        return this.indexationTime;
    }

    /**
     * Sets the indexation time of the subscription to the current time if it
     * has not already been set.
     */
    public void setIndexationTime() {
        if (this.indexationTime == -1) {
            this.indexationTime = System.currentTimeMillis();
        }
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
        return getSubscriberProxy(this.subscriberUrl);
    }

    public static SubscribeProxy getSubscriberProxy(String subscribeProxyURL)
            throws ExecutionException {
        return SUBSCRIBE_PROXIES_CACHE.get(subscribeProxyURL);
    }

    public String getSparqlQuery() {
        return this.sparqlQuery;
    }

    public synchronized Subsubscription[] getSubSubscriptions()
            throws DecompositionException {
        if (this.subSubscriptions == null) {
            List<AtomicQuery> atomicQueries =
                    SparqlDecomposer.getInstance()
                            .decompose(this.sparqlQuery)
                            .getAtomicQueries();

            this.subSubscriptions = new Subsubscription[atomicQueries.size()];
            for (int i = 0; i < atomicQueries.size(); i++) {
                this.subSubscriptions[i] =
                        new Subsubscription(
                                this.originalId, this.id, atomicQueries.get(i),
                                i);
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
            try {
                this.graphNode =
                        this.getSubSubscriptions()[0].getAtomicQuery()
                                .getGraph();
            } catch (DecompositionException e) {
                throw new IllegalStateException(e);
            }
        }

        return this.graphNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> toQuadruples() {
        Builder<Quadruple> result = new ImmutableList.Builder<Quadruple>();

        Node subscriptionURI =
                PublishSubscribeUtils.createSubscriptionIdUri(this.id);

        Node subscriptionOriginalURI =
                PublishSubscribeUtils.createSubscriptionIdUri(this.originalId);

        result.add(new Quadruple(
                subscriptionOriginalURI, subscriptionURI, SUBSCRIPTION_ID_NODE,
                NodeFactory.createLiteral(this.id.toString()), false, false));

        if (this.parentId != null) {
            result.add(new Quadruple(
                    subscriptionOriginalURI, subscriptionURI,
                    SUBSCRIPTION_PARENT_ID_NODE,
                    NodeFactory.createLiteral(this.parentId.toString()), false,
                    false));
        }

        if (this.originalId != null) {
            result.add(new Quadruple(
                    subscriptionOriginalURI,
                    subscriptionURI,
                    SUBSCRIPTION_ORIGINAL_ID_NODE,
                    PublishSubscribeUtils.createSubscriptionIdUri(this.originalId),
                    false, false));
        }

        result.add(new Quadruple(
                subscriptionOriginalURI, subscriptionURI,
                SUBSCRIPTION_SERIALIZED_VALUE_NODE,
                NodeFactory.createLiteral(this.sparqlQuery), false, false));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(this.creationTime);
        result.add(new Quadruple(
                subscriptionOriginalURI, subscriptionURI,
                SUBSCRIPTION_CREATION_DATETIME_NODE, NodeFactory.createLiteral(
                        DatatypeConverter.printDateTime(calendar),
                        XSDDatatype.XSDdateTime), false, false));

        result.add(new Quadruple(
                subscriptionOriginalURI, subscriptionURI,
                PublishSubscribeConstants.SUBSCRIPTION_TYPE_NODE,
                NodeFactory.createLiteral(
                        Short.toString(this.type.convert()),
                        XSDDatatype.XSDshort), false, false));

        calendar.setTimeInMillis(this.indexationTime);
        result.add(new Quadruple(
                subscriptionOriginalURI, subscriptionURI,
                SUBSCRIPTION_INDEXATION_DATETIME_NODE,
                NodeFactory.createLiteral(
                        DatatypeConverter.printDateTime(calendar),
                        XSDDatatype.XSDdateTime), false, false));

        result.add(new Quadruple(
                subscriptionOriginalURI, subscriptionURI,
                SUBSCRIPTION_SUBSCRIBER_NODE,
                NodeFactory.createURI(this.subscriberUrl), false, false));

        if (this.subscriptionDestination != null) {
            result.add(new Quadruple(
                    subscriptionOriginalURI, subscriptionURI,
                    SUBSCRIPTION_DESTINATION_NODE,
                    NodeFactory.createLiteral(this.subscriptionDestination),
                    false, false));
        }

        Subsubscription[] localSubSubscriptions;

        try {
            localSubSubscriptions = this.getSubSubscriptions();
        } catch (DecompositionException e) {
            throw new IllegalStateException(e);
        }

        result.add(new Quadruple(
                subscriptionOriginalURI, subscriptionURI,
                SUBSCRIPTION_INDEXED_WITH_NODE,
                NodeFactory.createLiteral(localSubSubscriptions[0].getId()
                        .toString()), false, false));

        if (this.intermediatePeerReferences != null) {
            result.add(new Quadruple(
                    subscriptionOriginalURI,
                    subscriptionURI,
                    SUBSCRIPTION_PEER_REFERENCES_NODE,
                    NodeFactory.createLiteral(this.intermediatePeerReferencesAsString()),
                    false, false));
        }

        for (Subsubscription ssubscription : localSubSubscriptions) {
            result.add(new Quadruple(
                    subscriptionOriginalURI,
                    subscriptionURI,
                    SUBSCRIPTION_HAS_SUBSUBSCRIPTION_NODE,
                    PublishSubscribeUtils.createSubSubscriptionIdUri(ssubscription.getId()),
                    false, false));
            result.addAll(ssubscription.toQuadruples());
        }

        return result.build();
    }

    private static void intermediatePeerReferencesFromString(Subscription s,
                                                             String iprs) {
        String[] peerURLs = iprs.split(" ");

        for (String peerURL : peerURLs) {
            for (String hashCode : peerURL.split(",")) {
                s.addIntermediatePeerReference(peerURL, fromString(hashCode));
            }
        }
    }

    private String intermediatePeerReferencesAsString() {
        StringBuilder result = new StringBuilder();

        for (String peerURL : this.intermediatePeerReferences.keySet()) {
            result.append(peerURL);
            result.append('=');

            for (HashCode hc : this.intermediatePeerReferences.get(peerURL)) {
                result.append(hc);
                result.append(',');
            }

            result.append(' ');
        }

        return result.toString();
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
        return Objects.toStringHelper(this.getClass())
                .add("originalId", this.originalId)
                .add("parentId", this.parentId)
                .add("id", this.id)
                .add("creationTime", this.creationTime)
                .add("indexationTime", this.indexationTime)
                .add("subscriberURL", this.subscriberUrl)
                .add("subscriptionDestination", this.subscriptionDestination)
                .add("sparqlQuery", this.sparqlQuery)
                .add("peerReferences", this.intermediatePeerReferences != null
                        ? this.intermediatePeerReferencesAsString() : "empty")
                .add("type", this.creationTime)
                .toString();
    }

    public static void main(String[] args) {
        SubscriptionId id = new SubscriptionId();
        Subscription subscription =
                new Subscription(
                        id, null, id, System.currentTimeMillis(),
                        System.currentTimeMillis(),
                        "SELECT ?g WHERE { GRAPH ?g { ?s ?p ?o }}",
                        "subscriberURI", "destinationURI",
                        NotificationListenerType.BINDING);

        QuadruplesFormatter.output(System.out, subscription.toQuadruples());
    }

}
