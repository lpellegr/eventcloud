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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.EventCloudInitializer;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;
import fr.inria.eventcloud.api.listeners.EventNotificationListener;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;

/**
 * 
 * @author lpellegr
 */
public class SubscribeProxyTest extends EventCloudInitializer implements
        Serializable {

    private ProxyFactory proxyFactory;

    @Before
    public void setUp() {
        super.setUp(1, 3);
        this.proxyFactory =
                ProxyFactory.getInstance(
                        super.getEventCloudRegistryUrl(), super.getEventCloud()
                                .getId());
    }

    // @Test
    // public void testSubscribeStringBindingNotificationListener() {
    // fail("Not yet implemented");
    // }

    /**
     * Test the subscription with an {@link EventNotificationListener} by
     * simulating a network congestion between the publication of two sets of
     * quadruples that belong to the same event.
     */
    @Test
    public void testSubscribeStringEventNotificationListenerSimulatingNetworkCongestion() {
        SubscribeProxy subscribeProxy =
                this.proxyFactory.createSubscribeProxy();

        final List<Event> eventsReceived = new ArrayList<Event>();

        // subscribes for any quadruples
        subscribeProxy.subscribe(
                "SELECT ?g WHERE { GRAPH ?g { ?s ?p ?o } }",
                new EventNotificationListener() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onNotification(SubscriptionId id, Event solution) {
                        eventsReceived.add(solution);
                        System.out.println("SOLUTION RECEIVED");
                    }
                });

        final Node eventId =
                Node.createURI(EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue()
                        + "587d8wq8gf7we4gsd4g4qw9");

        final PublishProxy publishProxy =
                this.proxyFactory.createPublishProxy();

        final Quadruple quadToPublish =
                new Quadruple(
                        eventId, eventId,
                        PublishSubscribeConstants.EVENT_NB_QUADRUPLES_NODE,
                        Node.createLiteral("9", null, XSDDatatype.XSDint));

        new Thread(new Runnable() {
            @Override
            public void run() {
                publishProxy.publish(quadToPublish);

                // inserts 4 quadruples that belongs to the same event
                for (int i = 0; i < 4; i++) {
                    publishProxy.publish(QuadrupleGenerator.create(eventId));
                }

                // waits some time to simulate a network congestion
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // inserts 4 quadruples that belongs to the same event
                for (int i = 0; i < 4; i++) {
                    publishProxy.publish(QuadrupleGenerator.create(eventId));
                }
            }
        }).start();

        try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // TODO: add assertions

        // TODO test has to be rewritten to detect termination cleanly

        System.out.println("END");
    }

    // @Test
    // public void testReconstructEventSubscriptionBinding() {
    // fail("Not yet implemented");
    // }

    // @Test
    // public void testReconstructEventNode() {
    // fail("Not yet implemented");
    // }

    // @Test
    // public void testUnsubscribe() {
    // fail("Not yet implemented");
    // }

    @After
    public void tearDown() {
        super.tearDown();
    }

}
