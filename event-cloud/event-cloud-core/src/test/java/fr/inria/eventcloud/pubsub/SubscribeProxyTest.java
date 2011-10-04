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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;
import fr.inria.eventcloud.api.listeners.EventNotificationListener;
import fr.inria.eventcloud.api.listeners.SignalNotificationListener;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;

/**
 * Class used to test a {@link SubscribeProxy}.
 * 
 * @author lpellegr
 */
public class SubscribeProxyTest {

    private static final Logger log =
            LoggerFactory.getLogger(SubscribeProxyTest.class);

    private static Collection<Event> events = new Collection<Event>();

    private EventCloudId eventCloudId;

    private JunitEventCloudInfrastructureDeployer deployer;

    private ProxyFactory proxyFactory;

    @Before
    public void setUp() {
        this.deployer = new JunitEventCloudInfrastructureDeployer();
        this.eventCloudId = deployer.createEventCloud(5);
        this.proxyFactory =
                ProxyFactory.getInstance(
                        deployer.getEventCloudsRegistryUrl(), this.eventCloudId);
    }

    /**
     * Test a basic subscription with a {@link SignalNotificationListener}.
     */
    @Test(timeout = 60000)
    public void testSubscribeStringSignalNotificationListener() {
        SubscribeProxy subscribeProxy =
                this.proxyFactory.createSubscribeProxy();

        // subscribes for any quadruples
        subscribeProxy.subscribe(
                "SELECT ?g WHERE { GRAPH ?g { ?s ?p ?o } }",
                new CustomSignalNotificationListener());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final Node eventId =
                Node.createURI(EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue()
                        + "587d8wq8gf7we4gsd4g4qw9");

        Collection<Quadruple> quads = new Collection<Quadruple>();
        for (int i = 0; i < 4; i++) {
            quads.add(QuadrupleGenerator.create(eventId));
        }

        Event event = new Event(quads);

        this.proxyFactory.createPublishProxy().publish(event);

        synchronized (events) {
            while (events.size() != event.getQuadruples().size()) {
                try {
                    events.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Test a basic subscription with an {@link EventNotificationListener}.
     */
    @Test(timeout = 60000)
    public void testSubscribeStringEventNotificationListener() {
        SubscribeProxy subscribeProxy =
                this.proxyFactory.createSubscribeProxy();

        // subscribes for any quadruples
        subscribeProxy.subscribe(
                "SELECT ?g WHERE { GRAPH ?g { ?s ?p ?o } }",
                new CustomEventNotificationListener());

        final Node eventId =
                Node.createURI(EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue()
                        + "587d8wq8gf7we4gsd4g4qw9");

        Collection<Quadruple> quads = new Collection<Quadruple>();
        for (int i = 0; i < 4; i++) {
            quads.add(QuadrupleGenerator.create(eventId));
        }

        this.proxyFactory.createPublishProxy().publish(new Event(quads));

        synchronized (events) {
            while (events.size() != 1) {
                try {
                    events.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // try to republish the event which has been received
        this.proxyFactory.createPublishProxy()
                .publish(events.iterator().next());

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
     * Test a subscription with an {@link EventNotificationListener} by
     * simulating a network congestion between the publication of two sets of
     * quadruples that belong to the same event.
     */
    @Test(timeout = 60000)
    public void testSubscribeStringEventNotificationListenerSimulatingNetworkCongestion() {
        SubscribeProxy subscribeProxy =
                this.proxyFactory.createSubscribeProxy();
        // subscribes for any quadruples
        subscribeProxy.subscribe(
                "SELECT ?g WHERE { GRAPH ?g { ?s ?p ?o } }",
                new CustomEventNotificationListener());

        // waits a little to be sure that the subscription has been indexed
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final Node eventId =
                Node.createURI(EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue()
                        + "587d8wq8gf7we4gsd4g4qw9");

        final PublishProxy publishProxy =
                this.proxyFactory.createPublishProxy();

        long publicationDateTime = System.currentTimeMillis();

        Quadruple quadToPublish =
                new Quadruple(
                        eventId, eventId,
                        PublishSubscribeConstants.EVENT_NB_QUADRUPLES_NODE,
                        Node.createLiteral("9", XSDDatatype.XSDint));
        quadToPublish.timestamp(publicationDateTime);
        publishProxy.publish(quadToPublish);

        // inserts 4 quadruples that belongs to the same event
        for (int i = 0; i < 4; i++) {
            quadToPublish = QuadrupleGenerator.create(eventId);
            quadToPublish.timestamp(publicationDateTime);
            publishProxy.publish(quadToPublish);
        }

        // waits some time to simulate a network congestion
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // inserts 4 quadruples that belongs to the same event
        for (int i = 0; i < 4; i++) {
            quadToPublish = QuadrupleGenerator.create(eventId);
            quadToPublish.timestamp(publicationDateTime);
            publishProxy.publish(quadToPublish);
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

    @After
    public void tearDown() {
        this.deployer.undeploy();
        events.clear();
    }

    private static class CustomEventNotificationListener extends
            EventNotificationListener {

        private static final long serialVersionUID = 1L;

        /**
         * {@inheritDoc}
         */
        @Override
        public void onNotification(SubscriptionId id, Event solution) {
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
            synchronized (events) {
                // just a dummy event to have the possibility to count the
                // number of signals which have been received
                events.add(new Event(QuadrupleGenerator.create()));
                events.notifyAll();
            }
            log.info("New signal received");
        }
    }

}
