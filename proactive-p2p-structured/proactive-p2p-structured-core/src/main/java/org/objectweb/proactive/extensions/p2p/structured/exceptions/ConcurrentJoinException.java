package org.objectweb.proactive.extensions.p2p.structured.exceptions;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * Exceptions thrown when a {@link Peer} tries to join a peer which is already
 * handling a join operation.
 * 
 * @author lpellegr
 */
public class ConcurrentJoinException extends ProActiveRuntimeException {

    private static final long serialVersionUID = 1L;

    public ConcurrentJoinException() {
        super();
    }

}
