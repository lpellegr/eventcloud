package org.objectweb.proactive.extensions.p2p.structured.operations;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

/**
 * A {@link SynchronousOperation} is an {@link Operation} which returns a non
 * reifiable object. Hence, all operations extending this kind of operation is
 * handled synchronously.
 * 
 * @author lpellegr
 */
public interface SynchronousOperation extends Operation {

    /**
     * Handles the operation by using the specified {@code overlay}
     * 
     * @param overlay
     *            the overlay receiving the operation.
     * 
     * @return a response associated to the operation handled.
     */
    public abstract ResponseOperation handle(StructuredOverlay overlay);

}
