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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;
import org.openjena.riot.lang.LangRIOT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
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
        return Arrays.asList(new Object[][] { {1, 1}, {1, 10}, {1, 100},});
    }

    private void benchmark() {
        log.info("Benchmark with {} peer(s) and {} subscriber(s)", this.nbPeers, this.nbSubscribers);
        
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
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final PublishProxy publishProxy = proxyFactory.createPublishProxy();
        // a thread that simulates a publication
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Quadruple> quadruples = new ArrayList<Quadruple>();

                Sink<Quad> sink = new Sink<Quad>() {
                    @Override
                    public void send(final Quad quad) {

                        Quadruple q =
                                new Quadruple(
                                        quad.getGraph(), quad.getSubject(),
                                        quad.getPredicate(), quad.getObject());

                        quadruples.add(q);

                    }

                    @Override
                    public void close() {
                        synchronized (allEventsPublished) {
                            allEventsPublished.received = true;
                            allEventsPublished.notifyAll();
                        }

                    }

                    @Override
                    public void flush() {

                    }

                };

                InputStream fis = null;
                LangRIOT parser = null;
                try {
                    fis =
                            PublishSubscribeBenchmark.class.getResourceAsStream("/chunk.nquads");
                    parser = RiotReader.createParserNQuads(fis, sink);

                    long startTime = System.nanoTime();
                    parser.parse();
                    timeToParseQuadruples.set(System.nanoTime() - startTime);

                    startTime = System.nanoTime();

                    final CountDownLatch doneSignal =
                            new CountDownLatch(quadruples.size());
                    for (final Quadruple quad : quadruples) {
                        publishProxy.publish(quad);
                        doneSignal.countDown();
                        nbEventsPublished++;
                    }
                    timeToPublishQuadruples.set(System.nanoTime() - startTime);

                    sink.close();
                } finally {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        // waits to publish events
        synchronized (allEventsPublished) {
            while (!allEventsPublished.received) {
                try {
                    allEventsPublished.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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

        log.info("All events received in " + timeToReceiveAllEvents + " ns");

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
            BindingNotificationListener {
        private static final long serialVersionUID = 1L;

        @Override
        public void onNotification(SubscriptionId id, Binding solution) {
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
