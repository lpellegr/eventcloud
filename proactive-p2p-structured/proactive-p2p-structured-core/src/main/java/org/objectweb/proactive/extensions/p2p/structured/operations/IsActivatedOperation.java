package org.objectweb.proactive.extensions.p2p.structured.operations;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

/**
 * Tests if a peer is activated (i.e. if has joined a network).
 * 
 * @author lpellegr
 */
public class IsActivatedOperation implements Operation {

    private static final long serialVersionUID = 1L;

	/**
	 * Returns a {@link BooleanResponseOperation} containing <code>true</code>
	 * value if the peer handling this message is activated, <code>false</code>
	 * otherwise.
	 * 
	 * @param overlay
	 *            the overlay used to handle the message.
	 * 
	 * @return a {@link BooleanResponseOperation} containing <code>true</code>
	 *         value if the peer handling this message is activated,
	 *         <code>false</code> otherwise.
	 */
    public BooleanResponseOperation handle(StructuredOverlay overlay) {
        return new BooleanResponseOperation(
        		overlay.getLocalPeer().isActivated());
    }
    
}
