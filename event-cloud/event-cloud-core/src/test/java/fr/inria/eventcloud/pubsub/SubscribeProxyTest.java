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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;
import fr.inria.eventcloud.api.listeners.EventNotificationListener;
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

    /**
     * Test the subscription with an {@link EventNotificationListener} by
     * simulating a network congestion between the publication of two sets of
     * quadruples that belong to the same event.
     */
    @Test
    public void testSubscribeStringEventNotificationListenerSimulatingNetworkCongestion() {
        JunitEventCloudInfrastructureDeployer deployer =
                new JunitEventCloudInfrastructureDeployer();

        EventCloudId ecId = deployer.createEventCloud(10);

        final ProxyFactory proxyFactory =
                ProxyFactory.getInstance(
                        deployer.getEventCloudsRegistryUrl(), ecId);

        Thread subscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SubscribeProxy subscribeProxy =
                        proxyFactory.createSubscribeProxy();
                // subscribes for any quadruples
                subscribeProxy.subscribe(
                        "SELECT ?g WHERE { GRAPH ?g { ?s ?p ?o } }",
                        new CustomEventNotificationListener());
            }
        });

        subscribeThread.start();

        // waits a little to be sure that the subscription has been indexed
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final Node eventId =
                Node.createURI(EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue()
                        + "587d8wq8gf7we4gsd4g4qw9");

        Thread publishThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final PublishProxy publishProxy =
                        proxyFactory.createPublishProxy();

                final Quadruple quadToPublish =
                        new Quadruple(
                                eventId,
                                eventId,
                                PublishSubscribeConstants.EVENT_NB_QUADRUPLES_NODE,
                                Node.createLiteral("9", XSDDatatype.XSDint));

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
        });
        publishThread.start();

        synchronized (events) {
            while (events.size() == 0) {
                try {
                    events.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        deployer.destroyEventCloud(ecId);
    }

    private static class CustomEventNotificationListener implements
            EventNotificationListener {

        private static final long serialVersionUID = 1L;

        @Override
        public void onNotification(SubscriptionId id, Event solution) {
            synchronized (events) {
                events.add(solution);
                events.notify();
            }
            log.info("New event received: {}", solution);
        }
    }

}
