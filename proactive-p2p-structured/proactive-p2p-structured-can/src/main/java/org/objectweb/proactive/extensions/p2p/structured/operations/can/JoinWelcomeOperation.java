package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.Operation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;

/**
 * This operation is used to performed the join welcome phase: it consists to
 * acknowledge the landmark peer that the data from the introduce phase have 
 * been received and set. Therefore, it is time to the landmark node to update
 * its information.
 * 
 * @author lpellegr
 * 
 * @see AbstractCanOverlay#join(Peer)
 * @see AbstractCanOverlay#handleJoinWelcomeMessage(JoinWelcomeOperation)
 */
public class JoinWelcomeOperation implements Operation {

    private static final long serialVersionUID = 1L;

	public JoinWelcomeOperation() {
		super();
	}

	/**
     * {@inheritDoc}
     */
    public EmptyResponseOperation handle(StructuredOverlay overlay) {
        return ((AbstractCanOverlay) overlay).handleJoinWelcomeMessage(this);
    }

}
