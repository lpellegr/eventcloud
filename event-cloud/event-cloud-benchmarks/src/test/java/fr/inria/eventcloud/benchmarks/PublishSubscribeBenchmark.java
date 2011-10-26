/**
 * Copyright (c) 2011 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terns of the GNU General Public License as published by
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
package fr.inria.eventcloud.benchmarks;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.generators.NodeGenerator;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;
import fr.inria.eventcloud.api.listeners.EventNotificationListener;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;

/**
 * Some benchmarks to try to evaluate the performances of the publish subscribe
 * algorithm.
 * 
 * @author lpellegr
 */
@RunWith(Parameterized.class)
public class PublishSubscribeBenchmark {

    private static final Logger log =
            LoggerFactory.getLogger(PublishSubscribeBenchmark.class);

    private static Status allEventsPublished =
            new PublishSubscribeBenchmark.Status(false);

    private static long nbEventsPublished = 0;

    private static Map<SubscriptionId, Counter> nbEventsReceivedBySubscriber =
            new HashMap<SubscriptionId, Counter>();

    private final int nbPeers;

    private final int nbSubscribers;

    private static final int NB_EVENTS = 5;

    public PublishSubscribeBenchmark(int nbPeers, int nbSubscribers) {
        this.nbPeers = nbPeers;
        this.nbSubscribers = nbSubscribers;
    }

    @Test
    public void testRatePerSecond() {
        benchmark();
    }

    @Parameters
    public static List<Object[]> getParametres() {
        return Arrays.asList(new Object[][] {
                {1, 1}, {1, 10}, {1, 100}, {10, 1}, {10, 10}, {10, 100},
                {50, 1}, {50, 10}, {50, 100}});
    }

    private void benchmark() {
        log.info(
                "Benchmark with {} peer(s) and {} subscriber(s)", this.nbPeers,
                this.nbSubscribers);

        JunitEventCloudInfrastructureDeployer deployer =
                new JunitEventCloudInfrastructureDeployer();

        EventCloudId ecId = deployer.createEventCloud(this.nbPeers);

        final ProxyFactory proxyFactory =
                ProxyFactory.getInstance(
                        deployer.getEventCloudsRegistryUrl(), ecId);

        final AlterableLong timeToPublishQuadruples = new AlterableLong();
        final AlterableLong timeToParseQuadruples = new AlterableLong();
        final AlterableLong timeToReceiveAllEvents = new AlterableLong();
        final AlterableLong startTime = new AlterableLong();

        allEventsPublished = new PublishSubscribeBenchmark.Status(false);
        nbEventsPublished = 0;
        nbEventsReceivedBySubscriber.clear();

        startTime.set(System.nanoTime());
        for (int i = 0; i < this.nbSubscribers; i++) {
            final SubscribeProxy subscribeProxy =
                    proxyFactory.createSubscribeProxy();

            // a thread that simulates a subscription
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SubscriptionId subscriptionId =
                            subscribeProxy.subscribe(
                                    "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }",
                                    new CustomBindingListener());

                    nbEventsReceivedBySubscriber.put(
                            subscriptionId, new Counter());
                }
            }).start();
        }

        // to ensure that the subscription is indexed before to publish
        // quadruples
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final PublishProxy publishProxy = proxyFactory.createPublishProxy();
        // a thread that simulates a publication

        final CountDownLatch doneSignal = new CountDownLatch(NB_EVENTS);

        new Thread(new Runnable() {
            @Override
            public void run() {
                long startTime = System.nanoTime();

                Collection<Quadruple> quadruples = null;
                for (int i = 0; i < 20; i++) {
                    quadruples = new Collection<Quadruple>();
                    Node graphNode =
                            NodeGenerator.createUri("http://streams.play-project.eu/");
                    for (int j = 0; j < 6; j++) {
                        quadruples.add(QuadrupleGenerator.create(graphNode));
                    }

                    // the publish is asynchronous but then the Peer.send
                    // request inside the publish method is handled in IS, hence
                    // each
                    // publish is finally done in parallel
                    publishProxy.publish(new Event(quadruples));
                    doneSignal.countDown();
                    nbEventsPublished++;
                }

                timeToPublishQuadruples.set(System.nanoTime() - startTime);
            }
        }).start();

        // waits to publish events
        try {
            doneSignal.await();
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
        }

        log.info("It takes " + timeToParseQuadruples + " ns to parse "
                + nbEventsPublished + " quadruples");
        log.info("It takes " + timeToPublishQuadruples
                + " ns to publish these quadruples");

        // waits to receive events
        synchronized (nbEventsReceivedBySubscriber) {
            while (!allEventsReceived()) {
                try {
                    nbEventsReceivedBySubscriber.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        timeToReceiveAllEvents.set(System.nanoTime() - startTime.get());

        log.info("All events received in "
                + timeToReceiveAllEvents
                + " ns\nFor "
                + this.nbPeers
                + " peer(s) and "
                + this.nbSubscribers
                + " subscriber(s) the rate is "
                + (nbEventsPublished / (double) (timeToReceiveAllEvents.value / (double) 10e8))
                + " quadruple(s) per second");

        deployer.undeploy();
    }

    private static boolean allEventsReceived() {
        for (Counter counter : nbEventsReceivedBySubscriber.values()) {
            if (counter.get() != nbEventsPublished) {
                return false;
            }
        }

        return true;
    }

    private static class CustomBindingListener extends
            EventNotificationListener {
        private static final long serialVersionUID = 1L;

        @Override
        public void onNotification(SubscriptionId id, Event solution) {
            log.info("New Event Received");
            synchronized (nbEventsReceivedBySubscriber) {
                nbEventsReceivedBySubscriber.get(id).inc();
                nbEventsReceivedBySubscriber.notifyAll();
            }
        }
    }

    private static class Status implements Serializable {

        private static final long serialVersionUID = 1L;

        public boolean received = false;

        public Status(boolean received) {
            super();
            this.received = received;
        }

    }

    private static class Counter implements Serializable {

        private static final long serialVersionUID = 1L;

        private AtomicLong count = new AtomicLong(1);

        public void inc() {
            count.incrementAndGet();
        }

        public long get() {
            return this.count.get();
        }

    }

    private static class AlterableLong implements Serializable {

        private static final long serialVersionUID = 1L;

        private long value = 0;

        @Override
        public String toString() {
            return Long.toString(this.value);
        }

        public void set(long value) {
            this.value = value;
        }

        public long get() {
            return this.value;
        }

    }

}
