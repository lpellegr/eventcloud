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
package fr.inria.eventcloud.datastore;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extensions.p2p.structured.utils.SystemUtil;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ResultSet;

import fr.inria.eventcloud.api.generators.QuadrupleGenerator;

/**
 * Provides tests for some methods exposed by a {@link PersistentTdbDatastore}.
 * 
 * @author lpellegr
 */
public final class PersistentJenaTdbDatastoreTest {

    private static final int CONCURRENT_ADD_OPERATIONS = 100;

    private static final int CONCURRENT_RANDOM_OPERATIONS = 100;

    private PersistentJenaTdbDatastore datastore;

    @Before
    public void setUp() {
        this.datastore = new PersistentJenaTdbDatastore();
        this.datastore.open();
    }

    @Test
    public void testAddSingleThread() {
        for (int i = 0; i < 100; i++) {
            this.datastore.add(QuadrupleGenerator.create());
        }

        Assert.assertEquals(100, this.datastore.find(
                Node.ANY, Node.ANY, Node.ANY, Node.ANY).size());
    }

    @Test
    public void testAddMultiThread() {
        ExecutorService executor =
                Executors.newFixedThreadPool(SystemUtil.getOptimalNumberOfThreads());

        final CountDownLatch doneSignal =
                new CountDownLatch(CONCURRENT_ADD_OPERATIONS);

        for (int i = 0; i < CONCURRENT_ADD_OPERATIONS; i++) {
            executor.execute(new Runnable() {
                public void run() {
                    try {
                        datastore.add(QuadrupleGenerator.create());
                    } finally {
                        doneSignal.countDown();
                    }
                }
            });
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(
                CONCURRENT_ADD_OPERATIONS,
                size(this.datastore.executeSparqlSelect("SELECT ?s WHERE { GRAPH ?g { ?s ?p ?o } }")));
    }

    @Test
    public void testRandomConcurrentAccess() {
        ExecutorService executor =
                Executors.newFixedThreadPool(SystemUtil.getOptimalNumberOfThreads());

        final CountDownLatch doneSignal =
                new CountDownLatch(CONCURRENT_RANDOM_OPERATIONS);

        for (int i = 0; i < CONCURRENT_RANDOM_OPERATIONS; i++) {
            executor.execute(new Runnable() {
                public void run() {
                    try {
                        if (ProActiveRandom.nextInt(2) == 0) {
                            datastore.add(QuadrupleGenerator.create());
                        } else {
                            datastore.executeSparqlSelect("SELECT ?s WHERE { GRAPH ?g { ?s ?p ?o } }");
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
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testDeleteAny() {
        for (int i = 0; i < 100; i++) {
            this.datastore.add(QuadrupleGenerator.create());
        }

        this.datastore.deleteAny(Node.ANY, Node.ANY, Node.ANY, Node.ANY);

        Assert.assertEquals(0, this.datastore.find(
                Node.ANY, Node.ANY, Node.ANY, Node.ANY).size());
    }

    private static int size(ResultSet resultSet) {
        int count = 0;

        while (resultSet.hasNext()) {
            resultSet.next();
            count++;
        }

        return count;
    }

    @After
    public void tearDown() {
        this.datastore.close();
    }

}
