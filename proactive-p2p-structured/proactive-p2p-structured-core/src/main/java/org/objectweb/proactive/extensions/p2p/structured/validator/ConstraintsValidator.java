package org.objectweb.proactive.extensions.p2p.structured.validator;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;

/**
 * Used by {@link Router}s to known if the current {@link StructuredOverlay}
 * which handles the message validates the constraints associated to key
 * contained by the message.
 * 
 * @author Laurent Pellegrino
 * 
 * @param <K>
 *            the type of the key used to check if constraints are validated.
 */
public interface ConstraintsValidator<K> {

    abstract public boolean validatesKeyConstraints(StructuredOverlay overlay, K key);
    
}
