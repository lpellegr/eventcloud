package org.objectweb.proactive.extensions.p2p.structured.operations;

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

/**
 * An Operation is a special message that is sent from a {@link Peer} to an another
 * {@link Peer} in one hop by using a RPC call (via ProActive) without routing
 * steps.
 * 
 * @author lpellegr
 */
public interface Operation extends Serializable {

    /**
     * Handles the message by using the specified <code>overlay</code>.
     * 
     * @param overlay
     *            the {@link StructuredOverlay} to use in order to handle the
     *            message.
     * @return a response associated to the message handled.
     */
    public abstract ResponseOperation handle(StructuredOverlay overlay);

}
