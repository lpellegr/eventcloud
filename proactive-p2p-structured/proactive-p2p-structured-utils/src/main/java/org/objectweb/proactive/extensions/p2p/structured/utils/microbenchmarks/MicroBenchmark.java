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
package org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks;

/**
 * A simple class that allows to perform a microbenchmark for a given task. The
 * micro benchmark consist in running the task the specified number of time and
 * to return the average execution among these several runs.
 * 
 * @author lpellegr
 */
public class MicroBenchmark {

    private final int nbRuns;

    private final MicroBenchmarkRun benchmark;

    private int discardFirstRuns = 1;

    private boolean showProgress = false;

    private StatsRecorder statsRecorder;

    public MicroBenchmark(int nbRuns, MicroBenchmarkRun task) {
        this(1, nbRuns, task);
    }

    public MicroBenchmark(int nbCategories, int nbRuns, MicroBenchmarkRun task) {
        this.nbRuns = nbRuns;
        this.benchmark = task;
        this.statsRecorder =
                new StatsRecorderImpl(
                        nbCategories, nbRuns, this.discardFirstRuns);
    }

    public void discardFirstRuns(int x) {
        if (x < 0) {
            throw new IllegalArgumentException("Invalid discard value: " + x);
        }

        this.discardFirstRuns = x;
    }

    public void showProgress() {
        this.showProgress = true;
    }

    public void execute() {
        for (int i = 0; i < this.nbRuns + this.discardFirstRuns; i++) {
            try {
                this.benchmark.run(this.statsRecorder);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }

            if (this.showProgress) {
                System.out.print("Run #" + (i + 1) + " performed");
                System.out.print(" ("
                        + this.statsRecorder.getCategory(0).getTime(i) + ")");

                if (this.discardFirstRuns > 0 && i < this.discardFirstRuns) {
                    System.out.println(" [ignored]");
                } else {
                    System.out.println();
                }
            }
        }
    }

    public int getNbRuns() {
        return this.nbRuns;
    }

    public StatsRecorder getStatsRecorder() {
        return this.statsRecorder;
    }

}