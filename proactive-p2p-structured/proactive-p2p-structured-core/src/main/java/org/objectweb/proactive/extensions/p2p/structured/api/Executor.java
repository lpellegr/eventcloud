package org.objectweb.proactive.extensions.p2p.structured.api;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.objectweb.proactive.extensions.p2p.structured.util.SystemUtil;

/**
 * Provides static method to execute a specified number of times a given task.
 * 
 * @author lpellegr
 */
public class Executor {

    /**
     * Creates the specified number of active objects by using the specified
     * task which knows how to create an active object.
     * 
     * @param <T>
     *            The type of the object returned by the execution of the
     *            specified task.
     * @param clazz
     *            the class associated to the object returned by the execution
     *            of the specified task.
     * @param task
     *            the task to execute.
     * @param nbTasks
     *            the number of instance of the specified task to run.
     * @return an array containing the objects created by the execution of the
     *         specified task <code>nbTasks</code> times.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] execute(Class<T> clazz, Callable<T> task, int nbTasks) {
        T[] result = (T[]) Array.newInstance(clazz, nbTasks);
        ExecutorService threadPool = 
        		Executors.newFixedThreadPool(
        				SystemUtil.getOptimalNumberOfThreads(1, 0));

        // Run nbTasks tasks in parallel
        List<Future<T>> futures = new ArrayList<Future<T>>();
        for (int i = 0; i < nbTasks; i++) {
            futures.add(threadPool.submit(task));
        }

        // Wait for tasks termination
        for (int i = 0; i < futures.size(); i++) {
            try {
                result[i] = futures.get(i).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        threadPool.shutdown();

        return result;
    }
    
}
