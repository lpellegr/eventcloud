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
package fr.inria.eventcloud.datastore;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.utils.RandomUtils;
import org.objectweb.proactive.extensions.p2p.structured.utils.SystemUtil;

import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;

/**
 * Test cases associated to {@link TransactionalTdbDatastore}.
 * 
 * @author lpellegr
 */
public final class TransactionalTdbDatastoreTest {

    private static final int SEQUENTIAL_ADD_OPERATIONS = 100;

    private static final int CONCURRENT_ADD_OPERATIONS = 100;

    private static final int CONCURRENT_RANDOM_OPERATIONS = 1000;

    private TransactionalTdbDatastore datastore;

    @Before
    public void setUp() {
        this.datastore = new TransactionalTdbDatastoreMem();
        this.datastore.open();
    }

    @Test
    public void testSequentialAdd() {
        TransactionalDatasetGraph txnGraph =
                this.datastore.begin(AccessMode.WRITE);
        try {
            for (int i = 0; i < SEQUENTIAL_ADD_OPERATIONS; i++) {
                txnGraph.add(QuadrupleGenerator.random());
            }
            txnGraph.commit();
        } finally {
            txnGraph.end();
        }

        this.assertNbQuadruples(this.datastore, SEQUENTIAL_ADD_OPERATIONS);
    }

    @Test
    public void testMultithreadedAdd() {
        ExecutorService executor =
                Executors.newFixedThreadPool(SystemUtil.getOptimalNumberOfThreads());

        final CountDownLatch doneSignal =
                new CountDownLatch(CONCURRENT_ADD_OPERATIONS);

        for (int i = 0; i < CONCURRENT_ADD_OPERATIONS; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    TransactionalDatasetGraph txnGraph =
                            TransactionalTdbDatastoreTest.this.datastore.begin(AccessMode.WRITE);
                    try {
                        txnGraph.add(QuadrupleGenerator.random());
                        txnGraph.commit();
                    } finally {
                        txnGraph.end();
                        doneSignal.countDown();
                    }
                }
            });
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }

        this.assertNbQuadruples(this.datastore, CONCURRENT_ADD_OPERATIONS);
    }

    @Test
    public void testRandomConcurrentAccess() {
        ExecutorService executor =
                Executors.newFixedThreadPool(SystemUtil.getOptimalNumberOfThreads());

        final AtomicInteger nbQuadruplesAdded = new AtomicInteger();

        final CountDownLatch doneSignal =
                new CountDownLatch(CONCURRENT_RANDOM_OPERATIONS);

        for (int i = 0; i < CONCURRENT_RANDOM_OPERATIONS; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (RandomUtils.nextInt(2) == 0) {
                            TransactionalDatasetGraph txnGraph = null;
                            txnGraph =
                                    TransactionalTdbDatastoreTest.this.datastore.begin(AccessMode.WRITE);

                            try {
                                txnGraph.add(QuadrupleGenerator.random());
                                txnGraph.commit();
                                nbQuadruplesAdded.incrementAndGet();
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                txnGraph.end();
                            }
                        } else {
                            TransactionalDatasetGraph txnGraph =
                                    TransactionalTdbDatastoreTest.this.datastore.begin(AccessMode.READ_ONLY);

                            try {
                                txnGraph.find(QuadruplePattern.ANY);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                txnGraph.end();
                            }
                        }
                    } finally {
                        doneSignal.countDown();
                    }
                }
            });
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }

        this.assertNbQuadruples(this.datastore, nbQuadruplesAdded.get());
    }

    @Test
    public void testDelete() {
        // adds some quadruples
        this.testMultithreadedAdd();

        TransactionalDatasetGraph txnGraph =
                this.datastore.begin(AccessMode.WRITE);

        try {
            txnGraph.delete(QuadruplePattern.ANY);
            txnGraph.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }

        this.assertNbQuadruples(this.datastore, 0);
    }

    private void assertNbQuadruples(TransactionalTdbDatastore datastore,
                                    int expectedNbQuadruples) {
        TransactionalDatasetGraph txnGraph =
                datastore.begin(AccessMode.READ_ONLY);

        try {
            Assert.assertEquals(expectedNbQuadruples, txnGraph.find(
                    QuadruplePattern.ANY).count());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }
    }

    @After
    public void tearDown() {
        this.datastore.close();
    }

}
