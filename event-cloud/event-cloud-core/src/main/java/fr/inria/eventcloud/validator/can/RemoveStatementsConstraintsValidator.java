package fr.inria.eventcloud.validator.can;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCANOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;

import fr.inria.eventcloud.validator.AnycastConstraintsValidator;

/**
 * {@link ConstraintsValidator} for {@link RemoveStatementsRequest} and
 * {@link RemoveStatementsReply}.
 * 
 * @author lpellegr
 */
public class RemoveStatementsConstraintsValidator implements AnycastConstraintsValidator<Coordinate> {

    private static final long serialVersionUID = 1L;

    public boolean validatesKeyConstraints(StructuredOverlay overlay, Coordinate key) {
        return this.validatesKeyConstraints(((AbstractCANOverlay) overlay).getZone(), key);
    }

   public boolean validatesKeyConstraints(Zone zone, Coordinate key) {
        for (int i = 0; i < key.size(); i++) {
            // if coordinate is null we skip the test
            if (key.getElement(i) != null) {
                // the specified overlay does not contains the key
                if (zone.contains(i, key.getElement(i)) != 0) {
                    return false;
                }
            }
        }
        return true;
    }
    
}
