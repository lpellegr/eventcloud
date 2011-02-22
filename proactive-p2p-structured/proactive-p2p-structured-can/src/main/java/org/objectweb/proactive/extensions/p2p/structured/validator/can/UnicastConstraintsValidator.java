package org.objectweb.proactive.extensions.p2p.structured.validator.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.reply.can.LookupReply;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.LookupRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCANOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;

/**
 * {@link ConstraintsValidator} for {@link LookupRequest} and
 * {@link LookupReply}.
 * 
 * @author Laurent Pellegrino
 */
public class UnicastConstraintsValidator implements ConstraintsValidator<Coordinate> {

    private static final long serialVersionUID = 1L;

    public boolean validatesKeyConstraints(StructuredOverlay overlay, Coordinate key) {
        return ((AbstractCANOverlay) overlay).contains(key);
    }

}
