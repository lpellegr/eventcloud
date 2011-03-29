package org.objectweb.proactive.extensions.p2p.structured.validator.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.LookupRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.LookupResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;

/**
 * {@link ConstraintsValidator} for {@link LookupRequest} and
 * {@link LookupResponse}.
 * 
 * @author lpellegr
 */
public class UnicastConstraintsValidator extends ConstraintsValidator<Coordinate> {

	private static final long serialVersionUID = 1L;

    public UnicastConstraintsValidator(Coordinate key) {
		super(key);
	}

    public boolean validatesKeyConstraints(StructuredOverlay overlay) {
        return ((AbstractCanOverlay) overlay).contains(super.key);
    }

}
