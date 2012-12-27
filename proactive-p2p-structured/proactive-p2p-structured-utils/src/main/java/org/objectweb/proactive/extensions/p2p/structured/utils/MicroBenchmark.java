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
package org.objectweb.proactive.extensions.p2p.structured.utils;

import java.util.concurrent.Callable;

/**
 * A simple class that allows to perform a microbenchmark for a given task. The
 * micro benchmark consist in running the task the specified number of time and
 * to return the average execution among these several runs.
 * 
 * @author lpellegr
 */
public class MicroBenchmark {

    private final int nbRuns;

    private final Callable<Long> benchmark;

    private boolean discardFirstRun = true;

    private boolean showProgress = false;

    private long mean;

    public MicroBenchmark(int nbRuns, Callable<Long> task) {
        this.nbRuns = nbRuns;
        this.benchmark = task;
    }

    public void doNotDiscardFirstRun() {
        this.discardFirstRun = false;
    }

    public void showProgress() {
        this.showProgress = true;
    }

    public void execute() {
        this.mean = 0;

        long[] times = new long[this.nbRuns];

        for (int i = 0; i < (this.discardFirstRun
                ? this.nbRuns + 1 : this.nbRuns); i++) {
            long executionTime;
            try {
                executionTime = this.benchmark.call();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }

            // discard the first run
            if (this.discardFirstRun && i > 0) {
                times[i - 1] = executionTime;
            } else if (!this.discardFirstRun) {
                times[i] = executionTime;
            }

            if (this.showProgress) {
                System.out.print("Run #" + (i + 1) + " performed");
                if (this.discardFirstRun && i == 0) {
                    System.out.println(" (ignored)");
                } else {
                    System.out.println(" (" + executionTime + ")");
                }
            }
        }

        long sum = 0;

        for (int i = 0; i < times.length; i++) {
            sum += times[i];
        }

        this.mean = sum / this.nbRuns;
    }

    public long getMean() {
        return this.mean;
    }

    public int getNbRuns() {
        return this.nbRuns;
    }

}
