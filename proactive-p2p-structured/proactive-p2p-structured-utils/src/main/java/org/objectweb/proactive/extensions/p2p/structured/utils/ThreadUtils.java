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
package org.objectweb.proactive.extensions.p2p.structured.utils;

import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

/**
 * Utility methods to get information about threads.
 * 
 * @author lpellegr
 */
public class ThreadUtils {

    /**
     * Returns the number of threads that are in state {@link State#RUNNABLE} in
     * the current JVM.
     * 
     * @return the number of threads that are in state {@link State#RUNNABLE} in
     *         the current JVM.
     */
    public static int countActive() {
        return countActive(getAllThreads());
    }

    /**
     * Returns the number of threads that are in state {@link State#RUNNABLE} in
     * in the specified array of threads..
     * 
     * @return the number of threads that are in state {@link State#RUNNABLE} in
     *         the specified array of threads.
     */
    public static int countActive(Thread[] threads) {
        int result = 0;

        for (Thread t : threads) {
            if (t.getState() == State.RUNNABLE) {
                result++;
            }
        }

        return result;
    }

    /**
     * Returns the number of threads that are in state
     * {@link State#TIMED_WAITING}, {@link State#WAITING} or
     * {@link State#BLOCKED} in the current JVM.
     * 
     * @return the number of threads that are in state
     *         {@link State#TIMED_WAITING}, {@link State#WAITING} or
     *         {@link State#BLOCKED} in the current JVM.
     */
    public static int countWaiting() {
        return countWaiting(getAllThreads());
    }

    /**
     * Returns the number of threads that are in state
     * {@link State#TIMED_WAITING}, {@link State#WAITING} or
     * {@link State#BLOCKED} in the specified array of threads.
     * 
     * @param threads
     *            threads to consider.
     * 
     * @return the number of threads that are in state
     *         {@link State#TIMED_WAITING}, {@link State#WAITING} or
     *         {@link State#BLOCKED} in the specified array of threads.
     */
    public static int countWaiting(Thread[] threads) {
        int result = 0;

        for (Thread t : threads) {
            if (t.getState() == State.TIMED_WAITING
                    || t.getState() == State.WAITING
                    || t.getState() == State.BLOCKED) {
                result++;
            }
        }

        return result;
    }

    /**
     * Returns all threads that have a name matching the specified pattern name.
     * 
     * @param threadNamePattern
     *            regex to match.
     * 
     * @return all threads that have a name matching the specified pattern name.
     */
    public static Thread[] getAllThreads(String threadNamePattern) {
        Thread[] threads = getAllThreads();

        Thread[] filteredThreads = new Thread[threads.length];

        int i = 0;
        for (Thread t : threads) {
            if (t.getName().matches(threadNamePattern)) {
                filteredThreads[i] = t;
                i++;
            }
        }

        return Arrays.copyOfRange(filteredThreads, 0, i);
    }

    /*
     * The following two methods have been copied from
     * http://nadeausoftware.com/articles/2008/04/java_tip_how_list_and_find_threads_and_thread_groups
     */

    public static Thread[] getAllThreads() {
        final ThreadGroup root = getRootThreadGroup();
        final ThreadMXBean thbean = ManagementFactory.getThreadMXBean();

        int nAlloc = thbean.getThreadCount();
        int n = 0;

        Thread[] threads;
        do {
            nAlloc *= 2;
            threads = new Thread[nAlloc];
            n = root.enumerate(threads, true);
        } while (n == nAlloc);

        return java.util.Arrays.copyOf(threads, n);
    }

    private static ThreadGroup getRootThreadGroup() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup parentThreadGroup;

        while ((parentThreadGroup = threadGroup.getParent()) != null) {
            threadGroup = parentThreadGroup;
        }

        return threadGroup;
    }

}
