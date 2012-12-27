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
package org.objectweb.proactive.extensions.p2p.structured.factories;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.objectweb.proactive.extensions.p2p.structured.utils.SystemUtils;

/**
 * Provides static method to execute a specified number of times a given task.
 * 
 * @author lpellegr
 */
public final class Executor {

    private Executor() {

    }

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
                Executors.newFixedThreadPool(SystemUtils.getOptimalNumberOfThreads(
                        1, 0));

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
