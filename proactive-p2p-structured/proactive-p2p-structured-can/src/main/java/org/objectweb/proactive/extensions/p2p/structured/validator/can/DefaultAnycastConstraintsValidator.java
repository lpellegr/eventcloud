package org.objectweb.proactive.extensions.p2p.structured.validator.can;

import static com.google.common.base.Preconditions.checkNotNull;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastRequestRouter;

/**
 * This class is the default validator for {@link AnycastRequestRouter}. This
 * validator assumes that all coordinate elements set to {@code null} match the
 * constraints.
 * 
 * @author lpellegr
 */
public final class DefaultAnycastConstraintsValidator extends
        AnycastConstraintsValidator<StringCoordinate> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@code DefaultAnycastConstraintsValidator} which is a very
     * permissive constraints validator (i.e. the valitor validates the
     * constraints on any peer).
     */
    public DefaultAnycastConstraintsValidator() {
        super(new StringCoordinate(null, null, null));
    }

    /**
     * Creates a new {@code DefaultAnycastConstraintsValidator} with the
     * specified {@code key} to reach.
     * 
     * @param key
     *            the key to reach.
     */
    public DefaultAnycastConstraintsValidator(StringCoordinate key) {
        super(checkNotNull(key));
    }

    public final boolean validatesKeyConstraints(StructuredOverlay overlay) {
        return this.validatesKeyConstraints(((AbstractCanOverlay) overlay).getZone());
    }

    public final boolean validatesKeyConstraints(Zone zone) {
        for (int i = 0; i < super.key.size(); i++) {
            // if coordinate is null we skip the test
            if (super.key.getElement(i) != null) {
                // the specified overlay does not contains the key
                if (zone.contains(i, super.key.getElement(i)) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

}
