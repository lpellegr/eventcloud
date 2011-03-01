package org.objectweb.proactive.extensions.p2p.structured.exceptions;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * An exception thrown when the {@link Peer#leave()} method from a {@link Peer}
 * is called whereas the peer has not joined a network by using
 * {@link Peer#join(Peer)}.
 * 
 * @author lpellegr
 */
public class NetworkNotJoinedException extends StructuredP2PException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a <code>NetworkNotJoinedException</code> with no specified
     * detail message.
     */
    public NetworkNotJoinedException() {
        super();
    }

}
