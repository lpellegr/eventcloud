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
package fr.inria.eventcloud.benchmarks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.proactive.core.util.MutableInteger;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.utils.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;
import fr.inria.eventcloud.api.listeners.NotificationListener;
import fr.inria.eventcloud.api.listeners.SignalNotificationListener;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.providers.SemanticInMemoryOverlayProvider;
import fr.inria.eventcloud.providers.SemanticPersistentOverlayProvider;

/**
 * Functional test case to have the possibility to test the pub/sub layer in
 * local by using different parameters.
 * 
 * @author lpellegr
 */
@RunWith(Parameterized.class)
public class PublishSubscribeBenchmarkTest {

    private static final Logger log =
            LoggerFactory.getLogger(PublishSubscribeBenchmarkTest.class);

    private static Map<SubscriptionId, AtomicLong> nbEventsReceivedBySubscriber =
            new HashMap<SubscriptionId, AtomicLong>();

    private final int nbPeers;

    private final int nbPublishers;

    private final int nbSubscribers;

    private final MutableInteger nbEventsPublished;

    private final int expectedNbEvents;

    private final Supplier<? extends Event> supplier;

    private final Class<? extends NotificationListener<?>> notificationListenerType;

    private final DatastoreType datastoreType;

    private final Stopwatch receiveExpectedEventsStopwatch;

    private ExecutorService threadPool;

    public PublishSubscribeBenchmarkTest(int nbPeers, int nbPublishers,
            int nbSubscribers, int expectedNbEvents,
            Supplier<? extends Event> supplier,
            Class<? extends NotificationListener<?>> notificationListenerType,
            DatastoreType type) {
        this.nbPeers = nbPeers;
        this.nbPublishers = nbPublishers;
        this.nbSubscribers = nbSubscribers;
        this.nbEventsPublished = new MutableInteger();
        this.expectedNbEvents = expectedNbEvents;
        this.supplier = supplier;
        this.notificationListenerType = notificationListenerType;
        this.datastoreType = type;
        this.receiveExpectedEventsStopwatch = new Stopwatch();
        this.threadPool =
                Executors.newFixedThreadPool(SystemUtil.getOptimalNumberOfThreads());
    }

    @Parameters
    public static List<Object[]> getParameters() {
        // scenarios
        return Arrays.asList(new Object[][] {{
                10, 1, 1, 1000, new QuadrupleSupplier(),
                SignalNotificationListener.class, DatastoreType.PERSISTENT}
        // ,{
        // 10, 1, 1, 1000, new QuadrupleSupplier(),
        // BindingNotificationListener.class,
        // DatastoreType.PERSISTENT}
        // ,{
        // 10, 1, 1, 100, new CompoundEventSupplier(10),
        // CompoundEventNotificationListener.class,
        // DatastoreType.PERSISTENT}
        });
    }

    @BeforeClass
    public static void setUp() {
        EventCloudProperties.REPOSITORIES_AUTO_REMOVE.setValue(true);

        // EventCloudProperties.RECORD_STATS_MISC_DATASTORE.setValue(true);
        // EventCloudProperties.RECORD_STATS_PEER_STUBS_CACHE.setValue(true);
        // EventCloudProperties.RECORD_STATS_SUBSCRIBE_PROXIES_CACHE.setValue(true);
        // EventCloudProperties.RECORD_STATS_SUBSCRIPTIONS_CACHE.setValue(true);
    }

    @Test(timeout = 1800000)
    public void execute() throws EventCloudIdNotManaged {
        log.info(
                "Benchmark with {} peer(s), {} publisher(s) and {} subscriber(s) using {}",
                new Object[] {
                        this.nbPeers, this.nbPublishers, this.nbSubscribers,
                        this.notificationListenerType.getSimpleName()});

        JunitEventCloudInfrastructureDeployer deployer =
                new JunitEventCloudInfrastructureDeployer();

        SerializableProvider<? extends SemanticCanOverlay> overlayProvider;

        if (this.datastoreType == DatastoreType.IN_MEMORY) {
            overlayProvider = new SemanticInMemoryOverlayProvider();
        } else {
            overlayProvider = new SemanticPersistentOverlayProvider();
        }

        EventCloudId ecId =
                deployer.newEventCloud(new EventCloudDeploymentDescriptor(
                        overlayProvider), 1, this.nbPeers);

        final List<PublishApi> publishProxies =
                this.createPublishProxies(
                        deployer.getEventCloudsRegistryUrl(), ecId,
                        this.nbPublishers);
        final List<SubscribeApi> subscribeProxies =
                this.createSubscribeProxies(
                        deployer.getEventCloudsRegistryUrl(), ecId,
                        this.nbSubscribers);

        nbEventsReceivedBySubscriber.clear();
        this.receiveExpectedEventsStopwatch.reset();

        for (int i = 0; i < this.nbSubscribers; i++) {
            final SubscribeApi subscribeProxy = subscribeProxies.get(i);

            Subscription subscription =
                    new Subscription(
                            "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }");

            nbEventsReceivedBySubscriber.put(
                    subscription.getId(), new AtomicLong());

            subscribeProxy.subscribe(
                    subscription,
                    createNotificationListener(this.notificationListenerType));
        }

        this.receiveExpectedEventsStopwatch.start();

        boolean running = true;
        while (running) {
            for (int i = 0; i < this.nbPublishers; i++) {
                final PublishApi publishProxy = publishProxies.get(i);

                if (this.nbEventsPublished.getValue() < this.expectedNbEvents) {
                    this.threadPool.submit(new Runnable() {
                        @Override
                        public void run() {
                            PublishSubscribeBenchmarkTest.this.publish(
                                    publishProxy,
                                    PublishSubscribeBenchmarkTest.this.supplier);
                        }
                    });
                    this.nbEventsPublished.add(1);
                } else {
                    running = false;
                    break;
                }
            }
        }

        // waits to receive at least the number of expected events
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

        log.info(
                "It takes {} to receive {} events (~{} per second) with {} peer(s), {} publisher(s) and {} subscriber(s) using {}",
                new Object[] {
                        this.receiveExpectedEventsStopwatch,
                        this.expectedNbEvents,
                        ((this.expectedNbEvents * 10e2) / this.receiveExpectedEventsStopwatch.elapsedMillis()),
                        this.nbPeers, this.nbPublishers, this.nbSubscribers,
                        this.notificationListenerType.getSimpleName()});

        for (Peer p : deployer.getRandomSemanticTracker(ecId).getPeers()) {
            log.info(p.dump());
        }

        deployer.undeploy();
    }

    private void publish(PublishApi publishProxy, Supplier<?> supplier) {
        if (supplier instanceof QuadrupleSupplier) {
            publishProxy.publish(((QuadrupleSupplier) supplier).get());
        } else if (supplier instanceof CompoundEventSupplier) {
            publishProxy.publish(((CompoundEventSupplier) supplier).get());
        } else {
            throw new IllegalArgumentException("Unknow supplier type: "
                    + supplier.getClass());
        }
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

    private boolean allEventsReceived() {
        for (AtomicLong counter : nbEventsReceivedBySubscriber.values()) {
            if (counter.get() < this.expectedNbEvents - 1) {
                return false;
            }
        }

        return true;
    }

    private static NotificationListener<?> createNotificationListener(Class<? extends NotificationListener<?>> listenerType) {
        if (listenerType.equals(BindingNotificationListener.class)) {
            return new CustomBindingListener();
        } else if (listenerType.equals(CompoundEventNotificationListener.class)) {
            return new CustomEventListener();
        } else if (listenerType.equals(SignalNotificationListener.class)) {
            return new CustomSignalListener();
        } else {
            throw new IllegalArgumentException("Unknown listener type: "
                    + listenerType.getName());
        }
    }

    private static void handleNewEvent(SubscriptionId id) {
        synchronized (nbEventsReceivedBySubscriber) {
            nbEventsReceivedBySubscriber.get(id).incrementAndGet();
            nbEventsReceivedBySubscriber.notifyAll();
        }
    }

    private static enum DatastoreType {
        IN_MEMORY, PERSISTENT
    }

    private static final class CustomBindingListener extends
            BindingNotificationListener {
        private static final long serialVersionUID = 1L;

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
        private static final long serialVersionUID = 1L;

        @Override
        public void onNotification(SubscriptionId id, CompoundEvent solution) {
            // log.trace(
            // "New compound event received for subscription {}: {}", id,
            // solution);
            handleNewEvent(id);
        }
    }

    private static final class CustomSignalListener extends
            SignalNotificationListener {
        private static final long serialVersionUID = 1L;

        @Override
        public void onNotification(SubscriptionId id) {
            // log.trace("New signal received for subscription {}", id);
            handleNewEvent(id);
        }
    }

}
