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
package fr.inria.eventcloud.benchmarks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;
import org.openjena.riot.lang.LangRIOT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.engine.binding.BindingInputStream;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingOutputStream;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;

/**
 * 
 * 
 * @author lpellegr
 */
public class PublishSubscribeBenchmark {

    private static final Logger log =
            LoggerFactory.getLogger(PublishSubscribeBenchmark.class);

    private static final List<Binding> bindingsReceived =
            new ArrayList<Binding>();

    private static Status allEventsPublished =
            new PublishSubscribeBenchmark.Status(false);

    private static long nbEventsPublished;

    private static Counter nbEventsReceived = new Counter();

    @Test
    public void testRatePerSecond() {
        JunitEventCloudInfrastructureDeployer deployer =
                new JunitEventCloudInfrastructureDeployer();

        EventCloudId ecId = deployer.createEventCloud(1);

        final ProxyFactory proxyFactory =
                ProxyFactory.getInstance(
                        deployer.getEventCloudsRegistryUrl(), ecId);

        final PublishProxy publishProxy = proxyFactory.createPublishProxy();

        final SubscribeProxy subscribeProxy =
                proxyFactory.createSubscribeProxy();

        long publishTime;
        long subscribeTime;

        // a thread that simulates a subscription
        new Thread(new Runnable() {
            @Override
            public void run() {

                subscribeProxy.subscribe(
                        "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }",
                        new CustomBindingListener());
            }
        }).start();

        try {
            // to ensure that the subscription has been indexed
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // a thread that simulates a publication
        new Thread(new Runnable() {
            @Override
            public void run() {
                // final ExecutorService threadPool =
                // Executors.newFixedThreadPool(SystemUtil.getOptimalNumberOfThreads());
                // Executors.newFixedThreadPool(1);

                Sink<Quad> sink = new Sink<Quad>() {
                    @Override
                    public void send(final Quad quad) {
                        // threadPool.submit(new Runnable() {
                        // @Override
                        // public void run() {
                        Quadruple q =
                                new Quadruple(
                                        quad.getGraph(), quad.getSubject(),
                                        quad.getPredicate(), quad.getObject());
                        publishProxy.publish(q);
                        nbEventsPublished++;
                        System.out.println("publication number "
                                + nbEventsPublished);
                        // }
                        // });
                    }

                    @Override
                    public void close() {
                        System.out.println("nbEventsPublished="
                                + nbEventsPublished);
                        System.out.println("nbEventsReceived="
                                + nbEventsReceived);

                        synchronized (allEventsPublished) {
                            allEventsPublished.received = true;
                            allEventsPublished.notifyAll();
                        }
                        // threadPool.shutdown();
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
                    parser.parse();
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

        System.out.println("Wait for publishing events");

        synchronized (allEventsPublished) {
            while (!allEventsPublished.received) {
                try {
                    allEventsPublished.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("All events published");

        synchronized (nbEventsReceived) {
            while (nbEventsReceived.count.get() < nbEventsPublished) {
                try {
                    nbEventsReceived.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            Thread.sleep(90000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("PublishSubscribeBenchmark.testRatePerSecond() END R="
                + nbEventsReceived.count + ", P=" + nbEventsPublished);

        deployer.undeploy();
    }

    static int line = 0;

    public static void main(String[] args) {
        final List<Quad> quadruples = new ArrayList<Quad>();

        Sink<Quad> sink = new Sink<Quad>() {
            @Override
            public void send(final Quad quad) {
                quadruples.add(quad);
            }

            @Override
            public void close() {
            }

            @Override
            public void flush() {
            }
        };

        FileInputStream fis = null;

        LangRIOT parser = null;
        try {
            fis = new FileInputStream("/user/lpellegr/home/Downloads/test5");
            parser = RiotReader.createParserNQuads(fis, sink);
            parser.parse();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Quad q = quadruples.get(0);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        BindingMap b = BindingFactory.create();
        // b.add(Var.alloc("g"), q.getGraph());
        // b.add(Var.alloc("s"), q.getSubject());
        // b.add(Var.alloc("p"), q.getPredicate());
        b.add(Var.alloc("o"), q.getObject());

        BindingOutputStream bos = new BindingOutputStream(baos);
        bos.write(b);
        bos.close();

        BindingInputStream bis =
                new BindingInputStream(new ByteArrayInputStream(
                        baos.toByteArray()));

        System.out.println(bis.next());
    }

    private static class CustomBindingListener implements
            BindingNotificationListener {
        private static final long serialVersionUID = 1L;

        @Override
        public void onNotification(SubscriptionId id, Binding solution) {
            System.out.println(solution + " number" + nbEventsReceived.count);
            synchronized (nbEventsReceived) {
                nbEventsReceived.count.incrementAndGet();
                nbEventsReceived.notifyAll();
            }
        }
    }

    private static class Status implements Serializable {

        public boolean received = false;

        public Status(boolean received) {
            super();
            this.received = received;
        }

    }

    private static class Counter implements Serializable {

        public AtomicLong count = new AtomicLong();

    }

}
