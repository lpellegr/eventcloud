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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a means to measure the time taken to construct RDF2Go
 * objects in parallel.
 * 
 * @author lpellegr
 */
public class RDF2GoBuilderTest {

    private static final Logger logger =
            LoggerFactory.getLogger(RDF2GoBuilderTest.class);

    @Test
    public void testPerformance() {
        final int count = 1000000;
        final int threads = 3;
        final int frac = count / threads;

        ExecutorService threadPool = Executors.newFixedThreadPool(threads);
        final CountDownLatch doneSignal = new CountDownLatch(threads);

        long start = System.currentTimeMillis();

        for (int i = 0; i < threads; i++) {
            threadPool.execute(new Runnable() {
                public void run() {
                    try {
                        for (int j = 0; j < frac; j++) {
                            RDF2GoBuilder.createURI("http://www.inria.fr");
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
        }

        long total = System.currentTimeMillis() - start;

        logger.info("Total time=" + total + "ms, average=" + total
                / (double) count + "ms.");

    }

}
