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
package fr.inria.eventcloud.benchmarks.pubsub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.util.MutableInteger;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.utils.LoggerUtils;
import org.objectweb.proactive.extensions.p2p.structured.utils.MicroBenchmark;
import org.objectweb.proactive.extensions.p2p.structured.utils.SystemUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;
import fr.inria.eventcloud.api.listeners.NotificationListenerType;
import fr.inria.eventcloud.api.listeners.SignalNotificationListener;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.providers.SemanticInMemoryOverlayProvider;
import fr.inria.eventcloud.providers.SemanticPersistentOverlayProvider;

/**
 * 
 * 
 * @author lpellegr
 */
public class PublishSubscribeBenchmark {

    @Parameter(names = {"-np", "--nb-publications"}, description = "Number of CE to publish")
    private int nbPublications = 10;

    @Parameter(names = {"-ces", "--compound-event-size"}, description = "Number of quadruples contained by each CE")
    private int compoundEventSize = 10;

    @Parameter(names = {"-nr", "--nb-runs"}, description = "Number of times the test is performed", required = true)
    private int nbRuns = 1;

    @Parameter(names = {"-p", "--nb-peers"}, description = "Number of peers composing the P2P network")
    private int nbPeers = 1;

    @Parameter(names = {"--nb-publishers"}, description = "Number of publishers")
    private int nbPublishers = 1;

    @Parameter(names = {"--nb-subscribers"}, description = "Number of subscribers")
    private int nbSubscribers = 1;

    @Parameter(names = {"-lt", "--listener-type"}, description = "Listener type to use for subscriptions", converter = ListenerTypeConverter.class)
    private NotificationListenerType listenerType =
            NotificationListenerType.COMPOUND_EVENT;

    @Parameter(names = {"-imds", "--in-memory-datastore"}, description = "Specifies whether datastores have to be persistent or not")
    private boolean inMemoryDatastore = false;

    @Parameter(names = {"-h", "--help"}, description = "Print help", help = true)
    private boolean help;

    // ------------------------

    private static Map<SubscriptionId, AtomicLong> nbEventsReceivedBySubscriber =
            new HashMap<SubscriptionId, AtomicLong>();

    private final Stopwatch receiveExpectedEventsStopwatch = new Stopwatch();

    private MutableInteger nbEventsPublished = new MutableInteger();

    private ExecutorService threadPool =
            Executors.newFixedThreadPool(SystemUtils.getOptimalNumberOfThreads());

    private final Supplier<? extends Event> supplier =
            new CompoundEventSupplier(this.compoundEventSize);

    public static void main(String[] args) {
        LoggerUtils.disableLoggers();

        PublishSubscribeBenchmark benchmark = new PublishSubscribeBenchmark();

        JCommander jCommander = new JCommander(benchmark);

        try {
            jCommander.parse(args);

            if (benchmark.help) {
                jCommander.usage();
                System.exit(0);
            }
        } catch (ParameterException e) {
            jCommander.usage();
            System.exit(1);
        }

        benchmark.execute();

        System.exit(0);
    }

    public void execute() {
        MicroBenchmark microBenchmark =
                new MicroBenchmark(this.nbRuns, new Callable<Long>() {

                    @Override
                    public Long call() throws Exception {
                        return PublishSubscribeBenchmark.this.test();
                    }

                });
        microBenchmark.showProgress();
        microBenchmark.execute();

        // for (Peer p : deployer.getRandomSemanticTracker(id).getPeers()) {
        // log.info(p.dump());
        // }

        System.out.println("Average time for " + this.nbRuns + " runs is "
                + microBenchmark.getMean());
    }

    public long test() {
        SerializableProvider<? extends SemanticCanOverlay> overlayProvider;

        if (this.inMemoryDatastore) {
            overlayProvider = new SemanticInMemoryOverlayProvider();
        } else {
            overlayProvider = new SemanticPersistentOverlayProvider();
        }

        EventCloudDeployer deployer =
                new EventCloudDeployer(
                        new EventCloudDescription(),
                        new EventCloudDeploymentDescriptor(overlayProvider));
        deployer.deploy(1, this.nbPeers);

        EventCloudsRegistry registry =
                EventCloudsRegistryFactory.newEventCloudsRegistry();
        registry.register(deployer);
        String registryURL = null;
        try {
            registryURL = registry.register("registry");
        } catch (ProActiveException e1) {
            e1.printStackTrace();
        }

        EventCloudId id = deployer.getEventCloudDescription().getId();

        List<PublishApi> publishProxies = null;
        List<SubscribeApi> subscribeProxies = null;
        try {
            publishProxies =
                    this.createPublishProxies(
                            registryURL, id, this.nbPublishers);
            subscribeProxies =
                    this.createSubscribeProxies(
                            registryURL, id, this.nbSubscribers);
        } catch (EventCloudIdNotManaged e) {
            e.printStackTrace();
        }

        nbEventsReceivedBySubscriber.clear();
        this.receiveExpectedEventsStopwatch.reset();

        for (int i = 0; i < this.nbSubscribers; i++) {
            final SubscribeApi subscribeProxy = subscribeProxies.get(i);

            Subscription subscription =
                    new Subscription(
                            "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }");

            nbEventsReceivedBySubscriber.put(
                    subscription.getId(), new AtomicLong());

            subscribe(subscribeProxy, subscription, this.listenerType);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        this.receiveExpectedEventsStopwatch.start();

        boolean running = true;
        while (running) {
            for (int i = 0; i < this.nbPublishers; i++) {
                final PublishApi publishProxy = publishProxies.get(i);

                if (this.nbEventsPublished.getValue() < this.nbPublications) {
                    this.threadPool.submit(new Runnable() {
                        @Override
                        public void run() {
                            PublishSubscribeBenchmark.this.publish(publishProxy);
                        }
                    });
                    this.nbEventsPublished.add(1);
                } else {
                    running = false;
                    break;
                }
            }
        }

        // waits to receive at least the number of expected
        // events
        synchronized (nbEventsReceivedBySubscriber) {
            while (!this.allEventsReceived()) {
                try {
                    nbEventsReceivedBySubscriber.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        this.receiveExpectedEventsStopwatch.stop();

        deployer.undeploy();

        long result = this.receiveExpectedEventsStopwatch.elapsedMillis();

        this.receiveExpectedEventsStopwatch.reset();

        nbEventsReceivedBySubscriber =
                new HashMap<SubscriptionId, AtomicLong>();
        this.nbEventsPublished = new MutableInteger();

        return result;
    }

    private List<PublishApi> createPublishProxies(String registryUrl,
                                                  EventCloudId id, int nb)
            throws EventCloudIdNotManaged {
        List<PublishApi> result = new ArrayList<PublishApi>(nb);

        for (int i = 0; i < nb; i++) {
            result.add(ProxyFactory.newPublishProxy(registryUrl, id));
        }

        return result;
    }

    private List<SubscribeApi> createSubscribeProxies(String registryUrl,
                                                      EventCloudId id, int nb)
            throws EventCloudIdNotManaged {
        List<SubscribeApi> result = new ArrayList<SubscribeApi>(nb);

        for (int i = 0; i < nb; i++) {
            result.add(ProxyFactory.newSubscribeProxy(registryUrl, id));
        }

        return result;
    }

    private void publish(PublishApi publishProxy) {
        if (this.supplier instanceof QuadrupleSupplier) {
            publishProxy.publish(((QuadrupleSupplier) this.supplier).get());
        } else if (this.supplier instanceof CompoundEventSupplier) {
            publishProxy.publish(((CompoundEventSupplier) this.supplier).get());
        } else {
            throw new IllegalArgumentException("Unknow supplier type: "
                    + this.supplier.getClass());
        }
    }

    private static void subscribe(SubscribeApi subscribeProxy,
                                  Subscription subscription,
                                  NotificationListenerType listenerType) {

        switch (listenerType) {
            case BINDING:
                subscribeProxy.subscribe(
                        subscription, new CustomBindingListener());
                break;
            case COMPOUND_EVENT:
                subscribeProxy.subscribe(
                        subscription, new CustomEventListener());
                break;
            case SIGNAL:
                subscribeProxy.subscribe(
                        subscription, new CustomSignalListener());
                break;
        }
    }

    private boolean allEventsReceived() {
        for (AtomicLong counter : nbEventsReceivedBySubscriber.values()) {
            if (counter.get() < this.nbPublications - 1) {
                return false;
            }
        }

        return true;
    }

    private static final class CustomBindingListener extends
            BindingNotificationListener {
        private static final long serialVersionUID = 140L;

        @Override
        public void onNotification(SubscriptionId id, Binding solution) {
            // log.trace(
            // "New binding received for subscription {}: {}", id,
            // solution);
            handleNewEvent(id);
        }
    }

    private static final class CustomEventListener extends
            CompoundEventNotificationListener {
        private static final long serialVersionUID = 140L;

        @Override
        public void onNotification(SubscriptionId id, CompoundEvent solution) {
            handleNewEvent(id);
        }
    }

    private static final class CustomSignalListener extends
            SignalNotificationListener {
        private static final long serialVersionUID = 140L;

        @Override
        public void onNotification(SubscriptionId id) {
            handleNewEvent(id);
        }
    }

    private static void handleNewEvent(SubscriptionId id) {
        synchronized (nbEventsReceivedBySubscriber) {
            nbEventsReceivedBySubscriber.get(id).incrementAndGet();
            nbEventsReceivedBySubscriber.notifyAll();
        }
    }

}
