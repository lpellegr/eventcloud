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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.util.MutableInteger;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.objectweb.proactive.extensions.p2p.structured.utils.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.generators.NodeGenerator;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;
import fr.inria.eventcloud.api.generators.UuidGenerator;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;
import fr.inria.eventcloud.api.listeners.SignalNotificationListener;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.NotificationListenerFactory;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;

/**
 * Tests cases for operations in {@link SubscribeProxy} and {@link PublishProxy}
 * .
 * 
 * @author lpellegr
 */
public class SubscribeProxyTest {

    private static final Logger log =
            LoggerFactory.getLogger(SubscribeProxyTest.class);

    private static List<CompoundEvent> events = new ArrayList<CompoundEvent>();

    private static List<Binding> bindings = new ArrayList<Binding>();

    private static MutableInteger signals = new MutableInteger();

    private static Node eventId =
            NodeGenerator.randomUri(EventCloudProperties.EVENTCLOUD_ID_PREFIX.getValue());

    private EventCloudId eventCloudId;

    private JunitEventCloudInfrastructureDeployer deployer;

    private SubscribeApi subscribeProxy;

    private PublishApi publishProxy;

    @Before
    public void setUp() throws EventCloudIdNotManaged {
        this.deployer = new JunitEventCloudInfrastructureDeployer();
        this.eventCloudId = this.deployer.newEventCloud(1, 5);

        this.subscribeProxy =
                ProxyFactory.newSubscribeProxy(
                        this.deployer.getEventCloudsRegistryUrl(),
                        this.eventCloudId);
        this.publishProxy =
                ProxyFactory.newPublishProxy(
                        this.deployer.getEventCloudsRegistryUrl(),
                        this.eventCloudId);
    }

    /**
     * Test a basic subscription with concurrent publications.
     */
    @Test
    public void testSubscribeWithConcurrentPublications()
            throws EventCloudIdNotManaged {
        final int NB_PRODUCERS = 10;
        final int NB_EVENTS_TO_WAIT = 100;

        final List<PublishApi> publishProxies =
                this.createPublishProxies(NB_PRODUCERS);

        ScheduledExecutorService threadPool =
                Executors.newScheduledThreadPool(NB_PRODUCERS);

        List<ScheduledFuture<?>> futures = new ArrayList<ScheduledFuture<?>>();
        final AtomicInteger nbEventsPublished = new AtomicInteger();

        // simulates producers publishing at different rates
        for (int i = 0; i < NB_PRODUCERS; i++) {
            final PublishApi publishProxy = publishProxies.get(i);
            final int threadIndex = i;

            threadPool.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if (nbEventsPublished.incrementAndGet() <= NB_EVENTS_TO_WAIT) {
                        List<Quadruple> quadruples = new ArrayList<Quadruple>();
                        Node graphValue =
                                Node.createURI(EventCloudProperties.EVENTCLOUD_ID_PREFIX.getValue()
                                        + "/" + UuidGenerator.randomUuid());

                        for (int j = 0; j < 1 + RandomUtils.nextInt(30); j++) {
                            quadruples.add(QuadrupleGenerator.random(graphValue));
                        }

                        log.debug(
                                "Publishing an event composed of {} quadruples from thread {}",
                                quadruples.size(), threadIndex);

                        publishProxy.publish(new CompoundEvent(quadruples));
                    }
                }
            }, 0, (i + 1) * 200, TimeUnit.MILLISECONDS);
        }

        Subscription subscription =
                new Subscription(
                        "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }");

        this.subscribeProxy.subscribe(
                subscription, new CustomCompoundEventNotificationListener());

        synchronized (events) {
            while (events.size() < NB_EVENTS_TO_WAIT) {
                try {
                    events.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        for (ScheduledFuture<?> future : futures) {
            future.cancel(true);
        }

        threadPool.shutdown();
    }

    private List<PublishApi> createPublishProxies(int nb)
            throws EventCloudIdNotManaged {
        List<PublishApi> proxies = new ArrayList<PublishApi>(nb);

        for (int i = 0; i < nb; i++) {
            proxies.add(ProxyFactory.newPublishProxy(
                    this.deployer.getEventCloudsRegistryUrl(),
                    this.eventCloudId));
        }

        return proxies;
    }

    /**
     * Test a basic subscription with a {@link BindingNotificationListener}.
     */
    @Test(timeout = 60000)
    public void testSubscribeBindingNotificationListener() {
        Subscription subscription =
                new Subscription(
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT ?name ?email ?g WHERE { GRAPH ?g { ?id foaf:name ?name . ?id foaf:email ?email } }");

        // subscribes for any quadruples
        this.subscribeProxy.subscribe(
                subscription, new CustomBindingNotificationListener());

        long publicationTime = System.currentTimeMillis();

        // From the publish proxy it is possible to publish quadruples (events)
        Quadruple q1 =
                new Quadruple(
                        Node.createURI("https://plus.google.com/825349613"),
                        Node.createURI("https://plus.google.com/107234124364605485774"),
                        Node.createURI("http://xmlns.com/foaf/0.1/email"),
                        Node.createLiteral("user1@company.com"));
        q1.setPublicationTime(publicationTime);
        this.publishProxy.publish(q1);

        Quadruple q2 =
                new Quadruple(
                        Node.createURI("https://plus.google.com/825349613"),
                        Node.createURI("https://plus.google.com/107234124364605485774"),
                        Node.createURI("http://xmlns.com/foaf/0.1/name"),
                        Node.createLiteral("User1"));
        q2.setPublicationTime(publicationTime);
        this.publishProxy.publish(q2);

        // this quadruple shows chronicle context property because it is
        // delivered by reconsuming the first quadruple which was published
        Quadruple q3 =
                new Quadruple(
                        Node.createURI("https://plus.google.com/825349613"),
                        Node.createURI("https://plus.google.com/107234124364605485774"),
                        Node.createURI("http://xmlns.com/foaf/0.1/email"),
                        Node.createLiteral("user1.new.email@company.com"));
        q3.setPublicationTime(publicationTime);
        this.publishProxy.publish(q3);

        publicationTime = System.currentTimeMillis();

        Quadruple q4 =
                new Quadruple(
                        Node.createURI("https://plus.google.com/3283940594/2011-2012-08-30-18:13:05"),
                        Node.createURI("https://plus.google.com/107545688688906540962"),
                        Node.createURI("http://xmlns.com/foaf/0.1/email"),
                        Node.createLiteral("user2@company.com"));
        q4.setPublicationTime(publicationTime);
        this.publishProxy.publish(q4);

        Quadruple q5 =
                new Quadruple(
                        Node.createURI("https://plus.google.com/124324034/2011-2012-08-30-19:04:54"),
                        Node.createURI("https://plus.google.com/14023231238123495031/"),
                        Node.createURI("http://xmlns.com/foaf/0.1/name"),
                        Node.createLiteral("User 3"));
        q5.setPublicationTime();
        this.publishProxy.publish(q5);

        Quadruple q6 =
                new Quadruple(
                        Node.createURI("https://plus.google.com/3283940594/2011-2012-08-30-18:13:05"),
                        Node.createURI("https://plus.google.com/107545688688906540962"),
                        Node.createURI("http://xmlns.com/foaf/0.1/name"),
                        Node.createLiteral("User 2"));
        q6.setPublicationTime(publicationTime);
        this.publishProxy.publish(q6);

        // 3 notifications are expected
        synchronized (bindings) {
            while (bindings.size() != 3) {
                try {
                    bindings.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Test a basic subscription with a {@link SignalNotificationListener}.
     */
    @Test(timeout = 60000)
    public void testSubscribeSignalNotificationListener() {
        Subscription subscription =
                new Subscription(
                        "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }");

        // subscribes for any quadruples
        this.subscribeProxy.subscribe(
                subscription, new CustomSignalNotificationListener());

        List<Quadruple> quads = new ArrayList<Quadruple>();
        for (int i = 0; i < 4; i++) {
            quads.add(QuadrupleGenerator.random(eventId));
        }

        CompoundEvent event = new CompoundEvent(quads);

        this.publishProxy.publish(event);

        synchronized (signals) {
            while (signals.getValue() != event.getQuadruples().size()) {
                try {
                    signals.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Test a basic subscription with a
     * {@link CompoundEventNotificationListener}.
     */
    @Test(timeout = 60000)
    public void testSubscribeEventNotificationListener() {
        Subscription subscription =
                new Subscription(
                        "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }");

        // subscribes for any quadruples
        this.subscribeProxy.subscribe(
                subscription, new CustomCompoundEventNotificationListener());

        List<Quadruple> quads = new ArrayList<Quadruple>();
        for (int i = 0; i < 4; i++) {
            quads.add(QuadrupleGenerator.random(eventId));
        }

        CompoundEvent ce = new CompoundEvent(quads);

        this.publishProxy.publish(ce);

        synchronized (events) {
            while (events.size() != 1) {
                try {
                    events.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        ce = events.get(0);

        // tries to republish the event which has been received
        this.publishProxy.publish(ce);

        synchronized (events) {
            while (events.size() != 2) {
                try {
                    events.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Test a basic subscription with a compound event listener deployed as an
     * active object.
     */
    @Test(timeout = 60000)
    public void testSubscribeEventNotificationListenerActiveObject() {
        Subscription subscription =
                new Subscription(
                        "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }");

        // subscribes for any quadruples
        CustomCompoundEventNotificationListenerActiveObject notificationListener =
                NotificationListenerFactory.newNotificationListener(
                        CustomCompoundEventNotificationListenerActiveObject.class,
                        new Object[0]);
        this.subscribeProxy.subscribe(subscription, notificationListener);

        List<Quadruple> quads = new ArrayList<Quadruple>();
        for (int i = 0; i < 4; i++) {
            quads.add(QuadrupleGenerator.random(eventId));
        }

        CompoundEvent ce = new CompoundEvent(quads);

        this.publishProxy.publish(ce);

        while (notificationListener.getEvents().size() != 1) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Test a basic subscription with a
     * {@link CompoundEventNotificationListener}.
     */
    @Test(timeout = 60000)
    public void testSubscribeEventNotificationListenerWeatherUsecase() {
        Subscription subscription =
                new Subscription(
                        "SELECT ?g WHERE { GRAPH ?g { "
                                + "<urn:city:Nice> <urn:weather:avgtemp> ?temperature ."
                                + "<urn:city:Nice> <urn:weather:datetime> ?date } }");

        int nbDays = 10;

        this.subscribeProxy.subscribe(
                subscription, new CustomCompoundEventNotificationListener());

        Node subject = Node.createURI("urn:city:Nice");

        List<Quadruple> quads;

        DateTime datetime = new DateTime();

        for (int i = 0; i < nbDays; i++) {
            Node graphId =
                    Node.createURI(EventCloudProperties.EVENTCLOUD_ID_PREFIX.getValue()
                            + Integer.toString(i));
            quads = new ArrayList<Quadruple>(2);
            quads.add(new Quadruple(
                    graphId, subject, Node.createURI("urn:weather:avgtemp"),
                    Node.createLiteral(
                            Integer.toString(RandomUtils.nextInt(30)),
                            XSDDatatype.XSDint)));
            quads.add(new Quadruple(
                    graphId, subject, Node.createURI("urn:weather:datetime"),
                    Node.createLiteral(datetime.toString())));

            CompoundEvent ce = new CompoundEvent(quads);

            this.publishProxy.publish(ce);

            datetime = datetime.plusDays(1);
        }

        synchronized (events) {
            while (events.size() != nbDays) {
                try {
                    events.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Test a subscription with a {@link CompoundEventNotificationListener} by
     * simulating a network congestion between the publication of two sets of
     * quadruples that belong to the same compound event.
     * 
     * @throws InterruptedException
     */
    @Test(timeout = 60000)
    public void testSubscribeEventNotificationListenerSimulatingNetworkCongestion()
            throws InterruptedException {
        Subscription subscription =
                new Subscription(
                        "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }");

        // subscribes for any quadruples
        this.subscribeProxy.subscribe(
                subscription, new CustomCompoundEventNotificationListener());

        // waits a little to make sure the subscription has been indexed
        Thread.sleep(500);

        long publicationTime = System.currentTimeMillis();

        Quadruple quadToPublish =
                new Quadruple(
                        eventId, eventId,
                        PublishSubscribeConstants.EVENT_NB_QUADRUPLES_NODE,
                        Node.createLiteral("9", XSDDatatype.XSDint));
        quadToPublish.setPublicationTime(publicationTime);
        this.publishProxy.publish(quadToPublish);

        // inserts 4 quadruples that belongs to the same event
        for (int i = 0; i < 4; i++) {
            quadToPublish = QuadrupleGenerator.random(eventId);
            quadToPublish.setPublicationTime(publicationTime);
            this.publishProxy.publish(quadToPublish);
        }

        // waits some time to simulate a network congestion
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // inserts 4 quadruples that belongs to the same event
        for (int i = 0; i < 4; i++) {
            quadToPublish = QuadrupleGenerator.random(eventId);
            quadToPublish.setPublicationTime(publicationTime);
            this.publishProxy.publish(quadToPublish);
        }

        synchronized (events) {
            while (events.size() != 1) {
                try {
                    events.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Test to unsubscribe from a basic subscription.
     */
    @Test(timeout = 60000)
    public void testUnsubscribe() {
        Subscription subscription =
                new Subscription(
                        "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }");

        // subscribes for any quadruples
        this.subscribeProxy.subscribe(
                subscription, new CustomCompoundEventNotificationListener());

        List<Quadruple> quads = new ArrayList<Quadruple>();
        for (int i = 0; i < 4; i++) {
            quads.add(QuadrupleGenerator.random(eventId));
        }

        CompoundEvent ce = new CompoundEvent(quads);

        this.publishProxy.publish(ce);

        synchronized (events) {
            while (events.size() != 1) {
                try {
                    events.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // unsubscribes
        this.subscribeProxy.unsubscribe(subscription.getId());

        quads = new ArrayList<Quadruple>();
        for (int i = 0; i < 4; i++) {
            quads.add(QuadrupleGenerator.random(eventId));
        }

        ce = new CompoundEvent(quads);

        // publishes a new event
        this.publishProxy.publish(ce);

        synchronized (events) {
            try {
                events.wait(4000);
                Assert.assertTrue(events.size() == 1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @After
    public void tearDown() {
        this.deployer.undeploy();

        ComponentUtils.terminateComponent(this.publishProxy);
        ComponentUtils.terminateComponent(this.subscribeProxy);

        signals.setValue(0);
        bindings.clear();
        events.clear();
    }

    private static class CustomBindingNotificationListener extends
            BindingNotificationListener {

        private static final long serialVersionUID = 1L;

        /**
         * {@inheritDoc}
         */
        @Override
        public void onNotification(SubscriptionId id, Binding solution) {
            synchronized (bindings) {
                bindings.add(solution);
                bindings.notifyAll();
            }
            log.info("New binding received:\n{}", solution);
        }

    }

    private static class CustomCompoundEventNotificationListener extends
            CompoundEventNotificationListener {

        private static final long serialVersionUID = 1L;

        /**
         * {@inheritDoc}
         */
        @Override
        public void onNotification(SubscriptionId id, CompoundEvent solution) {
            synchronized (events) {
                events.add(solution);
                events.notifyAll();
            }
            log.info("New event received:\n{}", solution);
        }
    }

    private static class CustomSignalNotificationListener extends
            SignalNotificationListener {

        private static final long serialVersionUID = 1L;

        /**
         * {@inheritDoc}
         */
        @Override
        public void onNotification(SubscriptionId id) {
            synchronized (signals) {
                signals.add(1);
                signals.notifyAll();
            }
            log.info("New signal received");
        }
    }

}
