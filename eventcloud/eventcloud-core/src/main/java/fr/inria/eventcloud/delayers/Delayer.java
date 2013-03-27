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
package fr.inria.eventcloud.delayers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Abstract delayer that factorizes some code shared between all delayers.
 * 
 * @author lpellegr
 */
public abstract class Delayer<T> {

    private final Logger log;

    private Thread commitThread;

    private boolean running = true;

    private final int bufsize;

    private final int delayerCommitTimeout;

    private final String postActionName;

    private final String bufferedObjectName;

    protected final SemanticCanOverlay overlay;

    protected final List<T> buffer;

    public Delayer(SemanticCanOverlay overlay, Logger logger,
            String postActionName, String bufferedObjectName, int bufsize,
            int delayerCommitTimeout) {
        this.overlay = overlay;
        this.log = logger;

        this.bufsize = bufsize;
        this.delayerCommitTimeout = delayerCommitTimeout;
        this.postActionName = postActionName;
        this.bufferedObjectName = bufferedObjectName;

        this.buffer = new ArrayList<T>(bufsize);
    }

    protected abstract void flushBuffer();

    protected abstract void postAction();

    public void receive(T obj) {
        synchronized (this.buffer) {
            this.buffer.add(obj);

            if (this.buffer.size() >= this.bufsize) {
                int nbObjectsFlushed = this.commit();
                this.log.trace(
                        "{} {} committed because threshold exceeded on {}",
                        nbObjectsFlushed, this.bufferedObjectName, this.overlay);
            } else {
                if (!this.buffer.isEmpty()) {
                    // check whether we have a commit thread running
                    synchronized (this) {
                        if (this.commitThread == null) {
                            this.log.trace(
                                    "Commit thread created on {}", this.overlay);

                            this.commitThread = new CommitThread(this);
                            this.commitThread.start();
                        }
                    }
                }
            }
        }
    }

    private int commit() {
        synchronized (this.buffer) {
            int size = this.buffer.size();

            long startTime = 0;

            if (this.log.isTraceEnabled()) {
                startTime = System.currentTimeMillis();
            }

            this.flushBuffer();

            if (this.log.isTraceEnabled()) {
                this.log.trace(
                        "Buffer flushed in {} ms on {}",
                        System.currentTimeMillis() - startTime, this.overlay);
                startTime = System.currentTimeMillis();
            }

            this.postAction();

            this.buffer.clear();

            if (this.log.isTraceEnabled()) {
                this.log.trace(
                        "Fired {} in {} ms on {}", this.postActionName,
                        System.currentTimeMillis() - startTime, this.overlay);
            }

            return size;
        }
    }

    public synchronized void close() {
        if (this.commitThread != null) {
            this.running = false;

            try {
                this.commitThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    private static final class CommitThread extends Thread {

        private final Delayer<?> delayer;

        public CommitThread(Delayer<?> delayer) {
            super("Commit thread " + delayer.getClass().getSimpleName());
            this.delayer = delayer;

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            while (this.delayer.running) {
                try {
                    Thread.sleep(this.delayer.delayerCommitTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }

                int nbObjectsFlushed = 0;

                nbObjectsFlushed = this.delayer.commit();

                if (nbObjectsFlushed == 0) {
                    this.delayer.log.trace(
                            "Commit thread terminated on {}",
                            this.delayer.overlay);
                    // nothing was committed, we should
                    // stop the thread
                    synchronized (this.delayer) {
                        this.delayer.commitThread = null;
                    }
                    return;
                } else {
                    this.delayer.log.trace(
                            "{} {} committed because timeout exceeded on {}",
                            nbObjectsFlushed, this.delayer.bufferedObjectName,
                            this.delayer.overlay);
                }
            }
        }

    }

}
