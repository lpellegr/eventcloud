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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;
import fr.inria.eventcloud.api.listeners.NotificationListener;
import fr.inria.eventcloud.api.listeners.SignalNotificationListener;
import fr.inria.eventcloud.deployment.DatastoreType;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;

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

    private final AtomicInteger nbEventsPublished;

    private final int expectedNbEvents;

    private final Supplier<? extends Event> supplier;

    private final Class<? extends NotificationListener<?>> notificationListenerType;

    private final DatastoreType datastoreType;

    private final int publishersWaitInterval;

    private final Stopwatch receiveExpectedEventsStopwatch;

    public PublishSubscribeBenchmarkTest(int nbPeers, int nbPublishers,
            int nbSubscribers, int expectedNbEvents,
            int publishersWaitInterval, Supplier<? extends Event> supplier,
            Class<? extends NotificationListener<?>> notificationListenerType,
            DatastoreType type) {
        super();
        this.nbPeers = nbPeers;
        this.nbPublishers = nbPublishers;
        this.nbSubscribers = nbSubscribers;
        this.nbEventsPublished = new AtomicInteger();
        this.publishersWaitInterval = publishersWaitInterval;
        this.expectedNbEvents = expectedNbEvents;
        this.supplier = supplier;
        this.notificationListenerType = notificationListenerType;
        this.datastoreType = type;
        this.receiveExpectedEventsStopwatch = new Stopwatch();
    }

    @Parameters
    public static List<Object[]> getParameters() {
        // scenarios
        return Arrays.asList(new Object[][] {
                {
                        1, 10, 1, 100, 120, new QuadrupleSupplier(),
                        SignalNotificationListener.class,
                        DatastoreType.PERSISTENT},
                {
                        1, 10, 1, 100, 120, new QuadrupleSupplier(),
                        BindingNotificationListener.class,
                        DatastoreType.PERSISTENT},
                {
                        1, 10, 1, 100, 120, new CompoundEventSupplier(10),
                        CompoundEventNotificationListener.class,
                        DatastoreType.PERSISTENT}});
    }

    @Test(timeout = 300000)
    public void execute() {
        log.info(
                "Benchmark with {} peer(s), {} publisher(s) and {} subscriber(s) using {}",
                new Object[] {
                        this.nbPeers, this.nbPublishers, this.nbSubscribers,
                        this.notificationListenerType.getSimpleName()});

        JunitEventCloudInfrastructureDeployer deployer =
                new JunitEventCloudInfrastructureDeployer(this.datastoreType);

        EventCloudId ecId = deployer.createEventCloud(this.nbPeers);

        final ProxyFactory proxyFactory =
                ProxyFactory.getInstance(
                        deployer.getEventCloudsRegistryUrl(), ecId);

        final List<PublishProxy> publishProxies =
                this.createPublishProxies(proxyFactory, this.nbPublishers);
        final List<SubscribeProxy> subscribeProxies =
                this.createSubscribeProxies(proxyFactory, this.nbSubscribers);

        nbEventsReceivedBySubscriber.clear();
        this.receiveExpectedEventsStopwatch.reset();

        Timer timer = new Timer(false);

        for (int i = 0; i < this.nbPublishers; i++) {
            final PublishProxy publishProxy = publishProxies.get(i);

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (nbEventsPublished.incrementAndGet() <= expectedNbEvents) {
                        publish(publishProxy, supplier);
                    } else {
                        this.cancel();
                    }
                }
            };

            timer.schedule(timerTask, 0, this.publishersWaitInterval);
        }

        for (int i = 0; i < this.nbSubscribers; i++) {
            final SubscribeProxy subscribeProxy = subscribeProxies.get(i);

            Subscription subscription =
                    new Subscription(
                            "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }");

            nbEventsReceivedBySubscriber.put(
                    subscription.getId(), new AtomicLong());

            subscribeProxy.subscribe(
                    subscription,
                    createNotificationListener(this.notificationListenerType));
        }

        receiveExpectedEventsStopwatch.start();

        // waits to receive at least the number of expected events
        synchronized (nbEventsReceivedBySubscriber) {
            while (!allEventsReceived()) {
                try {
                    nbEventsReceivedBySubscriber.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        receiveExpectedEventsStopwatch.stop();

        log.info(
                "It takes {} to receive {} events (~{} per second) with {} peer(s), {} publisher(s) and {} subscriber(s) using {}",
                new Object[] {
                        receiveExpectedEventsStopwatch,
                        this.expectedNbEvents,
                        ((this.expectedNbEvents * 10e2) / receiveExpectedEventsStopwatch.elapsedMillis()),
                        this.nbPeers, this.nbPublishers, this.nbSubscribers,
                        notificationListenerType.getSimpleName()});

        // System.err.println("DUMP:\n"
        // + deployer.getRandomSemanticPeer(ecId).dump());

        deployer.undeploy();
    }

    private void publish(PublishProxy publishProxy, Supplier<?> supplier) {
        if (supplier instanceof QuadrupleSupplier) {
            publishProxy.publish(((QuadrupleSupplier) supplier).get());
        } else if (supplier instanceof CompoundEventSupplier) {
            publishProxy.publish(((CompoundEventSupplier) supplier).get());
        } else {
            throw new IllegalArgumentException("Unknow supplier type: "
                    + supplier.getClass());
        }
    }

    private List<PublishProxy> createPublishProxies(ProxyFactory proxyFactory,
                                                    int nb) {
        List<PublishProxy> result = new ArrayList<PublishProxy>(nb);

        for (int i = 0; i < nb; i++) {
            result.add(proxyFactory.createPublishProxy());
        }

        return result;
    }

    private List<SubscribeProxy> createSubscribeProxies(ProxyFactory proxyFactory,
                                                        int nb) {
        List<SubscribeProxy> result = new ArrayList<SubscribeProxy>(nb);

        for (int i = 0; i < nb; i++) {
            result.add(proxyFactory.createSubscribeProxy());
        }

        return result;
    }

    private boolean allEventsReceived() {
        for (AtomicLong counter : nbEventsReceivedBySubscriber.values()) {
            if (counter.get() < this.expectedNbEvents) {
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

    private static final class CustomBindingListener extends
            BindingNotificationListener {
        private static final long serialVersionUID = 1L;

        @Override
        public void onNotification(SubscriptionId id, Binding solution) {
            log.trace(
                    "New binding received for subscription {}: {}", id,
                    solution);
            handleNewEvent(id);
        }
    }

    private static final class CustomEventListener extends
            CompoundEventNotificationListener {
        private static final long serialVersionUID = 1L;

        @Override
        public void onNotification(SubscriptionId id, CompoundEvent solution) {
            log.trace(
                    "New compound event received for subscription {}: {}", id,
                    solution);
            handleNewEvent(id);
        }
    }

    private static final class CustomSignalListener extends
            SignalNotificationListener {
        private static final long serialVersionUID = 1L;

        @Override
        public void onNotification(SubscriptionId id) {
            log.trace("New signal received for subscription {}", id);
            handleNewEvent(id);
        }
    }

}
