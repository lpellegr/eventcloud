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
package org.objectweb.proactive.extensions.p2p.structured.overlay.datastore;

import java.io.Closeable;
import java.util.UUID;

/**
 * This Datastore interface makes no assumption on the datastore type: whether
 * it's a persistent datastore or not (i.e. an in-memory datastore).
 * 
 * @author lpellegr
 */
public abstract class Datastore implements Closeable, PeerDataHandler {

    protected final UUID id;

    protected boolean initialized;

    public Datastore() {
        this.id = UUID.randomUUID();
        this.initialized = false;

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (Datastore.this.initialized) {
                    Datastore.this.close();
                }
            }
        }));
    }

    /**
     * Initializes the datastore (i.e. creates the repository). Once this
     * operation is performed, a new call will fail by throwing an
     * {@link IllegalStateException}.
     */
    public synchronized void open() {
        if (this.initialized) {
            throw new IllegalStateException("Datastore already opened");
        } else {
            this.internalOpen();
            this.initialized = true;
        }
    }

    /**
     * Closes the repository (e.g. releases the resources). If someone attempt
     * to call this method whereas the datastore is not initialized, then an
     * {@link IllegalStateException} exception is thrown.
     */
    @Override
    public synchronized void close() {
        if (!this.initialized) {
            throw new IllegalStateException(
                    "The datastore has not been initialized via open");
        } else {
            this.internalClose();
            this.initialized = false;
        }
    }

    protected abstract void internalOpen();

    protected abstract void internalClose();

    public UUID getId() {
        return this.id;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

}
