package org.objectweb.proactive.extensions.p2p.structured.overlay.datastore;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This Datastore interface makes no assumption on the datastore type: whether
 * it's a persistent datastore or not (i.e. an in-memory datastore).
 * 
 * @author lpellegr
 */
public abstract class Datastore implements PeerDataHandler {

    protected final UUID id;

    protected AtomicBoolean initialized;

    public Datastore() {
        this.id = UUID.randomUUID();
        this.initialized = new AtomicBoolean();
    }

    /**
     * Initializes the datastore (i.e. creates the repository). Once this
     * operation is performed, a new call will fail by throwing an
     * {@link IllegalStateException}.
     */
    public void open() {
        if (!this.initialized.compareAndSet(false, true)) {
            throw new IllegalStateException("Datastore already opened");
        } else {
            this.internalOpen();
        }
    }

    /**
     * Closes the repository (e.g. releases the resources). If someone attempt
     * to call this method whereas the datastore is not initialized, then an
     * {@link IllegalStateException} exception is thrown.
     */
    public void close() {
        if (!this.initialized.compareAndSet(true, false)) {
            throw new IllegalStateException(
                    "The datastore has not been initialized via open");
        } else {
            this.internalClose();
        }
    }

    protected abstract void internalOpen();

    protected abstract void internalClose();

    public UUID getId() {
        return this.id;
    }

    public boolean isInitialized() {
        return this.initialized.get();
    }

}
