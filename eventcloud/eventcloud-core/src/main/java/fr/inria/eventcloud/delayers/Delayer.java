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

import java.util.Collection;

import org.slf4j.Logger;

import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Abstract delayer that factorizes some code shared between all delayers.
 * 
 * @author lpellegr
 */
public abstract class Delayer<R, B extends Collection<R>> {

    private final Logger log;

    private Thread commitThread;

    private boolean running = true;

    private final int bufsize;

    private final int delayerCommitTimeout;

    protected final String postActionName;

    protected final String bufferedObjectName;

    protected final SemanticCanOverlay overlay;

    protected final B buffer;

    public Delayer(SemanticCanOverlay overlay, Logger logger,
            String postActionName, String bufferedObjectName, int bufsize,
            int delayerCommitTimeout) {
        this.overlay = overlay;
        this.log = logger;

        this.bufsize = bufsize;
        this.delayerCommitTimeout = delayerCommitTimeout;
        this.postActionName = postActionName;
        this.bufferedObjectName = bufferedObjectName;

        this.buffer = this.createEmptyBuffer(bufsize);
    }

    protected abstract B createEmptyBuffer(int bufsize);

    protected abstract void flushBuffer();

    protected abstract void triggerAction();

    public void receive(R request) {
        synchronized (this.buffer) {
            this.buffer.add(request);

            this.commitOrCreateCommitThread();
        }
    }

    protected void commitOrCreateCommitThread() {
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

                        this.commitThread = new CommitThread();
                        this.commitThread.start();
                    }
                }
            }
        }
    }

    protected int commit() {
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

            this.triggerAction();

            if (this.log.isTraceEnabled()) {
                this.log.trace(
                        "Fired {} in {} ms on {}", this.postActionName,
                        System.currentTimeMillis() - startTime, this.overlay);
            }

            this.buffer.clear();

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

    private final class CommitThread extends Thread {

        public CommitThread() {
            super("Commit thread " + Delayer.this.getClass().getSimpleName());

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            while (Delayer.this.running) {
                try {
                    Thread.sleep(Delayer.this.delayerCommitTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }

                int nbObjectsFlushed = 0;

                nbObjectsFlushed = Delayer.this.commit();

                if (nbObjectsFlushed == 0) {
                    Delayer.this.log.trace(
                            "Commit thread terminated on {}",
                            Delayer.this.overlay);
                    // nothing was committed, we should
                    // stop the thread
                    synchronized (Delayer.this) {
                        Delayer.this.commitThread = null;
                    }
                    return;
                } else {
                    Delayer.this.log.trace(
                            "{} {} committed because timeout exceeded on {}",
                            nbObjectsFlushed, Delayer.this.bufferedObjectName,
                            Delayer.this.overlay);
                }
            }
        }

    }

}
