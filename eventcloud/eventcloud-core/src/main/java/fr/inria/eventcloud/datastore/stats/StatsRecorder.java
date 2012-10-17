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
package fr.inria.eventcloud.datastore.stats;

import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.apfloat.Apfloat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.overlay.can.SemanticElement;

/**
 * Defines methods to record statistics about {@link Quadruple}s which are
 * inserted into a {@link TransactionalTdbDatastore}.
 * 
 * @author lpellegr
 */
public abstract class StatsRecorder implements Serializable {

    private static final long serialVersionUID = 1L;

    private AtomicLong nbQuads;

    private transient final ListeningExecutorService threadPool;

    private transient Queue<ListenableFuture<?>> futures;

    public StatsRecorder() {
        this.nbQuads = new AtomicLong();
        this.threadPool =
                MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));
        this.futures = new ConcurrentLinkedQueue<ListenableFuture<?>>();
    }

    public void quadrupleAdded(final Node g, final Node s, final Node p,
                               final Node o) {
        final ListenableFuture<?> future =
                this.threadPool.submit(new Runnable() {

                    @Override
                    public void run() {
                        StatsRecorder.this.quadrupleAddedComputeStats(
                                g, s, p, o);
                    }
                });
        this.futures.add(future);
        future.addListener(new Runnable() {

            @Override
            public void run() {
                StatsRecorder.this.futures.remove(future);
            }
        }, MoreExecutors.sameThreadExecutor());

        this.nbQuads.incrementAndGet();
    }

    protected abstract void quadrupleAddedComputeStats(final Node g,
                                                       final Node s,
                                                       final Node p,
                                                       final Node o);

    public void quadrupleRemoved(final Node g, final Node s, final Node p,
                                 final Node o) {
        final ListenableFuture<?> future =
                this.threadPool.submit(new Runnable() {

                    @Override
                    public void run() {
                        StatsRecorder.this.quadrupleRemovedComputeStats(
                                g, s, p, o);
                    }
                });
        this.futures.add(future);
        future.addListener(new Runnable() {

            @Override
            public void run() {
                StatsRecorder.this.futures.remove(future);
            }
        }, MoreExecutors.sameThreadExecutor());

        this.nbQuads.decrementAndGet();
    }

    protected abstract void quadrupleRemovedComputeStats(Node g, Node s,
                                                         Node p, Node o);

    public abstract Apfloat computeGraphEstimation();

    public abstract Apfloat computeSubjectEstimation();

    public abstract Apfloat computePredicateEstimation();

    public abstract Apfloat computeObjectEstimation();

    public SemanticElement computeSplitEstimation(byte dimension) {
        Apfloat estimatedSplitValue = null;

        switch (dimension) {
            case 0:
                estimatedSplitValue = this.computeGraphEstimation();
                break;
            case 1:
                estimatedSplitValue = this.computeSubjectEstimation();
                break;
            case 2:
                estimatedSplitValue = this.computePredicateEstimation();
                break;
            case 3:
                estimatedSplitValue = this.computeObjectEstimation();
                break;
            default:
                throw new IllegalArgumentException(
                        "Invalid dimension specified: " + dimension);
        }

        return new SemanticElement(estimatedSplitValue);
    }

    /**
     * Returns the number of quadruples which have been recorded.
     * 
     * @return the number of quadruples which have been recorded.
     */
    public long getNbQuads() {
        return this.nbQuads.get();
    }

    /**
     * Statistics are computed in background by using threads. When this method
     * is called, we wait for the termination of all the threads.
     */
    public void sync() {
        Future<?> future;

        while ((future = this.futures.poll()) != null) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

}
