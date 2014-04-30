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
package fr.inria.eventcloud.delayers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.delayers.actions.Action;
import fr.inria.eventcloud.delayers.buffers.Buffer;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Defines a delayer that aims to cache elements of type T inside a
 * {@link Buffer}. Once the buffer is flushed (i.e. persisted to disk), an
 * {@link Action} is performed with elements from the buffer.
 * 
 * @author lpellegr
 */
public class Delayer<T> {

    private static final Logger log = LoggerFactory.getLogger(Delayer.class);

    private final SemanticCanOverlay overlay;

    private final List<Observer<T>> observers;

    protected final Buffer<T> buffer;

    protected final Action<T> action;

    private final String elementsName;

    protected final int commitInterval;

    protected final int commitSize;

    private volatile CommitThread commitThread;

    public Delayer(SemanticCanOverlay overlay, Buffer<T> buffer,
            Action<T> action, String elementNames, int commitInterval,
            int commitSize) {
        this.overlay = overlay;

        this.action = action;
        this.buffer = buffer;

        this.commitInterval = commitInterval;
        this.commitSize = commitSize;

        this.elementsName = elementNames;

        // to have observers should be something rare
        this.observers = new ArrayList<Observer<T>>(0);
    }

    public void receive(T request) {
        synchronized (this.buffer) {
            this.buffer.add(request);
            this.commitOrCreateCommitThread();
        }
    }

    public void register(Observer<T> r) {
        this.observers.add(r);
    }

    protected void commitOrCreateCommitThread() {
        if (this.buffer.size() >= this.commitSize) {
            int nbObjectsFlushed = this.commit();
            log.trace(
                    "{} {} committed because threshold exceeded on {}",
                    nbObjectsFlushed, this.elementsName, this.overlay);
        } else {
            if (this.commitThread == null) {
                log.trace("Commit thread created on {}", this.overlay);

                this.commitThread = new CommitThread();
                this.commitThread.start();
            }
        }
    }

    protected int commit() {
        boolean isTraceEnabled = log.isTraceEnabled();
        long startTime = 0;

        synchronized (this.buffer) {
            int size = this.buffer.size();

            if (size == 0) {
                return size;
            }

            if (isTraceEnabled) {
                startTime = System.currentTimeMillis();
            }

            this.flushBuffer();

            if (isTraceEnabled) {
                boolean dueToTimeout = this.buffer.size() < this.commitSize;

                log.trace(
                        "Buffer flushed in {} ms on {} {}",
                        System.currentTimeMillis() - startTime, this.overlay,
                        dueToTimeout
                                ? "due to timeout" : "due to bufsize");
                startTime = System.currentTimeMillis();
            }

            this.triggerAction();

            if (isTraceEnabled) {
                log.trace(
                        "Fired {} action in {} ms on {}", this.elementsName,
                        System.currentTimeMillis() - startTime, this.overlay);
            }

            this.buffer.clear();

            return size;
        }
    }

    private void flushBuffer() {
        this.buffer.persist();

        for (Observer<T> observer : this.observers) {
            observer.bufferFlushed(this.buffer, this.overlay);
        }
    }

    private void triggerAction() {
        this.action.perform(this.buffer);

        for (Observer<T> observer : this.observers) {
            observer.actionTriggered(this.buffer, this.overlay);
        }
    }

    public void sync() {
        if (this.commitThread != null) {
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
            while (true) {
                try {
                    Thread.sleep(Delayer.this.commitInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }

                int nbObjectsFlushed = Delayer.this.commit();

                if (nbObjectsFlushed == 0) {
                    log.trace(
                            "Commit thread terminated on {}",
                            Delayer.this.overlay);
                    // nothing was committed, we should
                    // stop the thread
                    Delayer.this.commitThread = null;

                    return;
                } else {
                    log.trace(
                            "{} {} committed because timeout exceeded on {}",
                            nbObjectsFlushed, Delayer.this.elementsName,
                            Delayer.this.overlay);
                }
            }
        }

    }

}
