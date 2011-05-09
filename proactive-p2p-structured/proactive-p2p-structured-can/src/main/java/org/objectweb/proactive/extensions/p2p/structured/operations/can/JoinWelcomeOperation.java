package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;

/**
 * This operation is used to performed the join welcome phase: it consists to
 * acknowledge the landmark peer that the data from the introduce phase have
 * been received and set. Therefore, it is time to the landmark node to update
 * its information.
 * 
 * @author lpellegr
 * 
 * @see CanOverlay#join(Peer)
 * @see CanOverlay#handleJoinWelcomeMessage(JoinWelcomeOperation)
 */
public class JoinWelcomeOperation implements SynchronousOperation {

    private static final long serialVersionUID = 1L;

    public JoinWelcomeOperation() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public EmptyResponseOperation handle(StructuredOverlay overlay) {
        return ((CanOverlay) overlay).handleJoinWelcomeMessage(this);
    }

}
