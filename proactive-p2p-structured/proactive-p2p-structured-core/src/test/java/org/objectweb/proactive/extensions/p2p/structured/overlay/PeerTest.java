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
package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.factories.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.operations.BooleanResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.utils.SystemUtils;

/**
 * Test some methods in the {@link PeerImpl} class.
 * 
 * @author lpellegr
 */
public class PeerTest {

    private static final CanMockOverlayProvider OVERLAY_PROVIDER =
            new CanMockOverlayProvider();

    @Test
    public void testConcurrentJoinOperations() throws InterruptedException,
            NetworkAlreadyJoinedException, ExecutionException {
        final Peer p = PeerFactory.newPeer(OVERLAY_PROVIDER);
        p.create();

        ExecutorService threadPool =
                Executors.newFixedThreadPool(SystemUtils.getOptimalNumberOfThreads() + 1);

        int nbReceives = 10;

        final CountDownLatch doneSignal = new CountDownLatch(nbReceives);

        List<Future<Boolean>> futures =
                new ArrayList<Future<Boolean>>(nbReceives);

        for (int i = 0; i < nbReceives; i++) {
            futures.add(threadPool.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    try {
                        BooleanResponseOperation response =
                                (BooleanResponseOperation) PAFuture.getFutureValue(p.receive(new CustomOperation()));
                        return response.getValue();
                    } finally {
                        doneSignal.countDown();
                    }
                };
            }));
        }

        doneSignal.await();
        threadPool.shutdown();

        for (Future<Boolean> future : futures) {
            Assert.assertTrue(
                    "Concurrent join operation detected", future.get());
        }
    }

    private static final class CustomOperation extends CallableOperation {

        private static final long serialVersionUID = 140L;

        @Override
        public ResponseOperation handle(StructuredOverlay overlay) {
            CanMockOverlay canOverlay = ((CanMockOverlay) overlay);

            boolean result;

            if ((result = canOverlay.lock.compareAndSet(false, true))) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                canOverlay.lock.set(false);
            }

            return new BooleanResponseOperation(result);
        }

        @Override
        public boolean isJoinOperation() {
            return true;
        }

    }

    private static final class CanMockOverlayProvider extends
            SerializableProvider<CanMockOverlay> {

        private static final long serialVersionUID = 140L;

        @Override
        public CanMockOverlay get() {
            return new CanMockOverlay();
        }

    }

    public static final class CanMockOverlay extends StructuredOverlay {

        private AtomicBoolean lock = new AtomicBoolean(false);

        public CanMockOverlay() {
            super(new RequestResponseManager() {
                private static final long serialVersionUID = 140L;
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void create() {

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void join(Peer landmarkPeer) {

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void leave() {

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public OverlayType getType() {
            return OverlayType.CAN;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return this.id.toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String dump() {
            return this.toString();
        }

    }

}
