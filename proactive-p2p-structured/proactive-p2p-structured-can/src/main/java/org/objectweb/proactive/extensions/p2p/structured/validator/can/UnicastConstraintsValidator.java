package org.objectweb.proactive.extensions.p2p.structured.validator.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.LookupRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.LookupResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;

/**
 * {@link ConstraintsValidator} for {@link LookupRequest} and
 * {@link LookupResponse}.
 * 
 * @author lpellegr
 */
public class UnicastConstraintsValidator extends
        ConstraintsValidator<StringCoordinate> {

    private static final long serialVersionUID = 1L;

    public UnicastConstraintsValidator(StringCoordinate key) {
        super(key);
    }

    public boolean validatesKeyConstraints(StructuredOverlay overlay) {
        return ((CanOverlay) overlay).getZone().contains(super.key);
    }

}
