package org.objectweb.proactive.extensions.p2p.structured.operations;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

/**
 * An {@link AsynchronousOperation} is an {@link Operation} which does not
 * return any response. Hence, all operations extending this kind of operation
 * are handled asynchronously.
 * 
 * @author lpellegr
 */
public abstract interface AsynchronousOperation extends Operation {

    /**
     * Handles the operation by using the specified {@code overlay}
     * 
     * @param overlay
     *            the overlay receiving the operation.
     */
    public abstract void handle(StructuredOverlay overlay);

}
