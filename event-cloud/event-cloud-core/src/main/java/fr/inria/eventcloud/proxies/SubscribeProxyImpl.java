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
package fr.inria.eventcloud.proxies;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientFactoryBean;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.listeners.EventNotificationListener;
import fr.inria.eventcloud.api.listeners.NotificationListener;
import fr.inria.eventcloud.api.properties.AlterableElaProperty;
import fr.inria.eventcloud.api.webservices.SubscribeWsApi;
import fr.inria.eventcloud.api.webservices.SubscriberWsApi;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.messages.request.can.IndexSubscriptionRequest;
import fr.inria.eventcloud.messages.request.can.ReconstructEventRequest;
import fr.inria.eventcloud.messages.request.can.UnsubscribeRequest;
import fr.inria.eventcloud.messages.response.can.ReconstructEventResponse;
import fr.inria.eventcloud.pubsub.Notification;
import fr.inria.eventcloud.pubsub.NotificationId;
import fr.inria.eventcloud.pubsub.Solution;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.translators.wsnotif.webservices.ProxyWsNotificationTranslator;
import fr.inria.eventcloud.translators.wsnotif.webservices.ProxyWsNotificationTranslatorImpl;
import fr.inria.eventcloud.utils.LongLong;

/**
 * SubscribeProxyImpl is a concrete implementation of {@link SubscribeProxy}.
 * This class has to be instantiated as a ProActive/GCM component.
 * <p>
 * Currently the receive operation is handled sequentially because it is not set
 * as Immediate Service (IS). This means we don't have to synchronize the
 * datastructure that are used inside this method. However it would be nice to
 * evaluate/decide if it is interesting to put the receive operation as IS
 * (TODO).
 * 
 * @author lpellegr
 * @author bsauvan
 * 
 * @see ProxyFactory
 */
public class SubscribeProxyImpl extends ProxyCache implements SubscribeProxy,
        SubscribeWsApi {

    private static final long serialVersionUID = 1L;

    private static final Logger log =
            LoggerFactory.getLogger(SubscribeProxy.class);

    /**
     * The retry threshold defines the time to wait in milliseconds before to
     * re-execute the query for retrieving the quadruples that belongs to the
     * specified event id.
     */
    // TODO: evaluate which value to use for this threshold
    private static final int RECONSTRUCTION_RETRY_THRESHOLD = 200;

    // contains the subscriptions that have been registered from this proxy
    private Map<SubscriptionId, Subscription> subscriptions;

    // contains the listeners to use in order to deliver the solutions
    private Map<SubscriptionId, NotificationListener<?>> listeners;

    // contains the solutions that are being received
    private Map<NotificationId, Solution> solutions;

    // contains the subscriber web service clients to use in order to deliver
    // the solutions
    private Map<SubscriptionId, Client> subscriberWsClients;

    // TODO: this set has to be replace by a DataBag. The number of events ids
    // received will grow quickly and after some time it is possible to get an
    // OutOfMemory exception. That's why it would be nice to have the
    // possibility to define a threshold that defines what is the maximum number
    // of eventIds to store in memory. Then by using a DataBag and when the
    // threshold is reached, the data write are spilled to the disk
    // example from pig:
    // http://pig.apache.org/docs/r0.7.0/api/org/apache/pig/data/DataBag.html
    private Map<Node, SubscriptionId> eventIdsReceived;

    private ProxyWsNotificationTranslator translator;

    /**
     * Empty constructor required by ProActive.
     */
    public SubscribeProxyImpl() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    // TODO: add support for ELA properties. At least for the maximum number of
    // requests per seconds (by using a queue and a scheduled Timer).
    public void init(EventCloudCache proxy, AlterableElaProperty[] properties) {
        if (this.proxy == null) {
            this.proxy = proxy;
            this.subscriptions = new HashMap<SubscriptionId, Subscription>();
            this.listeners =
                    new HashMap<SubscriptionId, NotificationListener<?>>();
            this.solutions = new HashMap<NotificationId, Solution>();
            this.subscriberWsClients = new HashMap<SubscriptionId, Client>();
            this.eventIdsReceived = new HashMap<Node, SubscriptionId>();
            this.translator = new ProxyWsNotificationTranslatorImpl();
            // TODO: use the properties field to initialize ELA properties
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionId subscribe(String sparqlQuery,
                                    BindingNotificationListener listener) {
        return this.indexSubscription(sparqlQuery, listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionId subscribe(String sparqlQuery,
                                    EventNotificationListener listener) {
        // TODO rewrite the sparqlQuery to keep only the graph variable in the
        // solution variables. Indeed we need only the graph variable (which
        // identify the event which is matched) to reconstruct the event
        return this.indexSubscription(sparqlQuery, listener);
    }

    @Override
    public SubscriptionId subscribe(String wsNotifSubscriptionPayload,
                                    String topicNameSpacePayload,
                                    String[] topicsDefinitionPayloads,
                                    String subscriberWsUrl) {
        String sparqlQuery =
                this.translator.translateWsNotifSubscriptionToSparqlQuery(
                        wsNotifSubscriptionPayload, topicNameSpacePayload,
                        topicsDefinitionPayloads);

        return this.indexSubscription(sparqlQuery, subscriberWsUrl);
    }

    private SubscriptionId indexSubscription(String sparqlQuery,
                                             NotificationListener<?> listener) {
        Subscription subscription =
                new Subscription(
                        PAActiveObject.getUrl(PAActiveObject.getStubOnThis()),
                        sparqlQuery);

        log.debug(
                "New subscription has been registered from {} with id {}",
                PAActiveObject.getBodyOnThis().getUrl(), subscription.getId());

        NotificationListener<?> result =
                this.listeners.put(subscription.getId(), listener);

        if (result != null) {
            log.warn("The subscription is canceled because a listener associated to the same subscription id has been detected: "
                    + subscription.getId());
            return subscription.getId();
        }

        try {
            subscription.timestamp();
            this.subscriptions.put(subscription.getId(), subscription);
            super.proxy.selectTracker().getRandomPeer().send(
                    new IndexSubscriptionRequest(subscription));
        } catch (DispatchException e) {
            e.printStackTrace();
        }

        return subscription.getId();
    }

    private SubscriptionId indexSubscription(String sparqlQuery,
                                             String subscriberWsUrl) {
        Subscription subscription =
                new Subscription(
                        PAActiveObject.getUrl(PAActiveObject.getStubOnThis()),
                        sparqlQuery);

        log.debug(
                "New subscription has been registered from {} with id {}",
                PAActiveObject.getBodyOnThis().getUrl(), subscription.getId());

        ClientFactoryBean clientFactory = new ClientFactoryBean();
        clientFactory.setServiceClass(SubscriberWsApi.class);
        clientFactory.setAddress(subscriberWsUrl);
        Client client = clientFactory.create();

        this.subscriberWsClients.put(subscription.getId(), client);

        try {
            super.proxy.selectTracker().getRandomPeer().send(
                    new IndexSubscriptionRequest(subscription));
            this.subscriptions.put(subscription.getId(), subscription);
        } catch (DispatchException e) {
            e.printStackTrace();
        }

        return subscription.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Event reconstructEvent(Subscription subscription,
                                        Binding binding) {

        if (!subscription.getGraphNode().isVariable()) {
            throw new IllegalArgumentException(
                    "The subscription graph node is not a variable");
        }

        Node eventId;
        if ((eventId =
                binding.get(Var.alloc(subscription.getGraphNode().getName()))) == null) {
            throw new IllegalArgumentException(
                    "the specified binding does not contain the specified graph value: "
                            + subscription.getGraphNode());
        }

        return this.reconstructEvent(subscription.getId(), eventId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Event reconstructEvent(SubscriptionId id, Node eventId) {
        if (!eventId.isURI()
                || !eventId.getURI().startsWith(
                        EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue())) {
            throw new IllegalArgumentException(
                    "The event id must be an URI starting by "
                            + EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue()
                            + ": " + eventId);
        }

        // the reconstruction operation for an event which has been already
        // received is cancelled
        if (this.eventIdsReceived.containsKey(eventId)) {
            return null;
        }

        int expectedNumberOfQuadruples = -1;

        Collection<Quadruple> quadsReceived = new Collection<Quadruple>();
        Collection<LongLong> quadHashesReceived =
                new Collection<LongLong>(new HashSet<LongLong>());

        QuadruplePattern reconstructPattern =
                new QuadruplePattern(eventId, Node.ANY, Node.ANY, Node.ANY);

        // perform polling while all the quadruples have not been retrieved
        while (expectedNumberOfQuadruples == -1
                || quadsReceived.size() != expectedNumberOfQuadruples) {
            Collection<Quadruple> quads = null;
            try {
                quads =
                        ((ReconstructEventResponse) PAFuture.getFutureValue(super.proxy.selectTracker()
                                .getRandomPeer()
                                .send(
                                        new ReconstructEventRequest(
                                                reconstructPattern,
                                                quadHashesReceived)))).getResult();
            } catch (DispatchException e) {
                e.printStackTrace();
            }

            for (Quadruple quad : quads) {
                if (quad.getPredicate().equals(
                        PublishSubscribeConstants.EVENT_NB_QUADRUPLES_NODE)) {
                    expectedNumberOfQuadruples =
                            (Integer) quad.getObject().getLiteralValue();
                }
                quadsReceived.add(quad);
                quadHashesReceived.add(quad.hashValue());
            }

            try {
                Thread.sleep(RECONSTRUCTION_RETRY_THRESHOLD);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        this.eventIdsReceived.put(eventId, id);

        return new Event(quadsReceived);
    }

    public static Long[] toObject(long[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return new Long[0];
        }
        final Long[] result = new Long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = new Long(array[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(SubscriptionId id) {
        if (!this.subscriptions.containsKey(id)) {
            throw new IllegalArgumentException(
                    "No subscription registered with the specified subscription id: "
                            + id);
        }

        Subscription subscription = this.subscriptions.remove(id);
        if (this.subscriberWsClients.containsKey(id)) {
            Client client = this.subscriberWsClients.remove(id);
            client.destroy();
        } else {
            this.listeners.remove(id);
        }
        this.eventIdsReceived.values().remove(id);

        try {
            super.proxy.selectTracker().getRandomPeer().send(
                    new UnsubscribeRequest(subscription));
        } catch (DispatchException e) {
            e.printStackTrace();
        }
    }

    private void deliver(NotificationId id) {
        // TODO delivers the solution according the type of the listener
        NotificationListener<?> listener =
                this.listeners.get(id.getSubscriptionId());

        if (listener instanceof BindingNotificationListener) {
            this.deliver(id, (BindingNotificationListener) listener);
        } else if (listener instanceof EventNotificationListener) {
            this.deliver(id, (EventNotificationListener) listener);
        } else {
            log.error("Unknown notification listener: " + listener.getClass());
        }

        log.debug("Notification {} has been delivered", id);
    }

    private void deliver(NotificationId id, BindingNotificationListener listener) {
        listener.onNotification(id.getSubscriptionId(), this.solutions.remove(
                id).getSolution());
    }

    private void deliver(NotificationId id, EventNotificationListener listener) {
        Event event =
                this.reconstructEvent(
                        this.subscriptions.get(id.getSubscriptionId()),
                        this.solutions.remove(id).getSolution());

        if (event != null) {
            listener.onNotification(id.getSubscriptionId(), event);
        }
    }

    private void deliverWs(NotificationId id) {
        try {
            Event event =
                    this.reconstructEvent(
                            this.subscriptions.get(id.getSubscriptionId()),
                            this.solutions.remove(id).getSolution());

            if (event != null) {
                Client client =
                        this.subscriberWsClients.get(id.getSubscriptionId());
                String methodName = "notifyEvent";
                String wsNotification =
                        this.translator.translateEventToWsNotifNotification(event);

                client.invoke(methodName, new Object[] {
                        id.getSubscriptionId(), wsNotification});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void receive(Notification notification) {
        SubscriptionId subscriptionId =
                notification.getId().getSubscriptionId();

        // this condition is used to ignore the notifications which may be
        // received after an unsubscribe operation because the unsubscribe
        // operation is not atomic
        if (!this.subscriptions.containsKey(subscriptionId)) {
            return;
        }

        log.debug(
                "New notification received on {} from {} for subscription id {}",
                new Object[] {
                        PAActiveObject.getBodyOnThis().getUrl(),
                        notification.getSource(), subscriptionId});

        Solution solution = this.solutions.get(notification.getId());
        if (solution == null) {
            solution =
                    new Solution(
                            this.subscriptions.get(subscriptionId)
                                    .getSubSubscriptions().length,
                            notification.getBinding());
            this.solutions.put(notification.getId(), solution);
        } else {
            solution.addSubSolution(notification.getBinding());
        }

        // checks whether all the sub-solutions have been received or not
        if (solution.isReady()) {
            // TODO checks whether the ELA properties are verified
            // if yes, deliver the solution
            // else do nothing and wait for an ELA property that is verified
            if (this.subscriberWsClients.containsKey(subscriptionId)) {
                this.deliverWs(notification.getId());
            } else {
                this.deliver(notification.getId());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Subscription find(SubscriptionId id) {
        return this.subscriptions.get(id);
    }

}
