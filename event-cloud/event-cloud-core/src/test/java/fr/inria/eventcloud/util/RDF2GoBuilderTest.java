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
		
		for (int i=0; i<threads; i++) {
			threadPool.execute(new Runnable() {
				public void run() {
					try {
						for (int j=0; j<frac; j++) {
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
		
		logger.info("Total time=" + total + "ms, average=" + total/(double)count + "ms.");
		
	}
	
}
