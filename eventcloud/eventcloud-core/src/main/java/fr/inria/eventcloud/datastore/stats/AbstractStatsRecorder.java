/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.datastore.stats;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import org.apfloat.Apfloat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.overlay.can.SemanticCoordinate;

/**
 * Defines methods to record statistics about {@link Quadruple}s which are
 * inserted into and removed from a {@link TransactionalTdbDatastore}.
 * 
 * @author lpellegr
 */
public abstract class AbstractStatsRecorder implements StatsRecorder {

    private static final long serialVersionUID = 160L;

    private static final Logger log =
            LoggerFactory.getLogger(AbstractStatsRecorder.class);

    private final Recorder recorder;

    public AbstractStatsRecorder(int nbBackgroundThreads) {
        if (nbBackgroundThreads > 0) {
            this.recorder = new BackgroundRecorder(nbBackgroundThreads);
        } else {
            this.recorder = new SameThreadRecorder();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(final Node g, final Node s, final Node p, final Node o) {
        this.recorder.register(g, s, p, o);
    }

    protected abstract void _register(final Node g, final Node s, final Node p,
                                      final Node o);

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregister(final Node g, final Node s, final Node p,
                           final Node o) {
        this.recorder.unregister(g, s, p, o);
    }

    protected abstract void _unregister(Node g, Node s, Node p, Node o);

    /**
     * {@inheritDoc}
     */
    @Override
    public SemanticCoordinate computeSplitEstimation(byte dimension) {
        Apfloat estimatedSplitValue = null;

        if (this.getNbQuadruples() == 0) {
            // no quadruple has been recorded, no estimation can be computed
            return null;
        }

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

        return new SemanticCoordinate(estimatedSplitValue);
    }

    /**
     * Statistics are computed in background by using threads. When this method
     * is called, we wait for the termination of all the threads. It is assumed
     * that the stats recorder is not registering new quadruples when this
     * method is called.
     */
    @Override
    public void sync() {
        this.recorder.sync();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        this.sync();
    }

    private interface Recorder extends Serializable {

        void register(final Node g, final Node s, final Node p, final Node o);

        void unregister(final Node g, final Node s, final Node p, final Node o);

        void sync();

    }

    private class SameThreadRecorder implements Recorder {

        private static final long serialVersionUID = 160L;

        @Override
        public void register(final Node g, final Node s, final Node p,
                             final Node o) {
            AbstractStatsRecorder.this._register(g, s, p, o);

            log.trace("Registering new quadruple");
        }

        @Override
        public void unregister(final Node g, final Node s, final Node p,
                               final Node o) {
            AbstractStatsRecorder.this._unregister(g, s, p, o);
            log.trace("Unregistering quadruple");
        }

        @Override
        public void sync() {
        }

    }

    private class BackgroundRecorder extends SameThreadRecorder {

        private static final long serialVersionUID = 160L;

        private transient ListeningExecutorService threadPool;

        private transient Queue<ListenableFuture<?>> futures;

        public BackgroundRecorder(int nbThreads) {
            this.futures = new ConcurrentLinkedQueue<ListenableFuture<?>>();
            this.threadPool =
                    MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(
                            nbThreads,
                            new ThreadFactoryBuilder().setNameFormat(
                                    this.getClass().getSimpleName()
                                            + "-pool-thread-%d").build()));
        }

        @Override
        public void register(final Node g, final Node s, final Node p,
                             final Node o) {

            final ListenableFuture<?> future =
                    this.threadPool.submit(new Runnable() {
                        @Override
                        public void run() {
                            BackgroundRecorder.super.register(g, s, p, o);
                        }
                    });

            this.futures.add(future);

            future.addListener(new Runnable() {
                @Override
                public void run() {
                    BackgroundRecorder.this.futures.remove(future);
                }
            }, MoreExecutors.sameThreadExecutor());

        }

        @Override
        public void unregister(final Node g, final Node s, final Node p,
                               final Node o) {
            final ListenableFuture<?> future =
                    this.threadPool.submit(new Runnable() {
                        @Override
                        public void run() {
                            BackgroundRecorder.super.unregister(g, s, p, o);
                        }
                    });

            this.futures.add(future);

            future.addListener(new Runnable() {
                @Override
                public void run() {
                    BackgroundRecorder.this.futures.remove(future);
                }
            }, MoreExecutors.sameThreadExecutor());
        }

        @Override
        public void sync() {
            long start = 0;

            if (log.isTraceEnabled()) {
                start = System.currentTimeMillis();
            }

            Iterator<ListenableFuture<?>> it = this.futures.iterator();

            while (it.hasNext()) {
                try {
                    ListenableFuture<?> future = it.next();

                    if (!future.isDone()) {
                        future.get();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    it.remove();
                }
            }

            if (log.isTraceEnabled()) {
                log.trace("Sync performed in "
                        + (System.currentTimeMillis() - start) + " ms");
            }
        }

    }

}
