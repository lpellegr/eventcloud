package org.objectweb.proactive.extensions.p2p.structured.exceptions;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * An exception thrown when the {@link Peer#join(Peer)} method from a
 * {@link Peer} is called whereas it has already been called and it has
 * succeeded.
 * 
 * @author lpellegr
 */
public class NetworkAlreadyJoinedException extends StructuredP2PException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a <code>NetworkAlreadyJoinedException</code> with no specified
     * detail message.
     */
    public NetworkAlreadyJoinedException() {
        super();
    }

}
