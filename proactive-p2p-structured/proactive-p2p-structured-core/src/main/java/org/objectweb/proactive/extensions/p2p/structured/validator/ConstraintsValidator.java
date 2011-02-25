package org.objectweb.proactive.extensions.p2p.structured.validator;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;

/**
 * Used by the {@link Router}s to known whether a {@link StructuredOverlay}
 * which handles the message validates the constraints associated to the key
 * contained by the message.
 * 
 * @author lpellegr
 * 
 * @param <K>
 *            the type of the key used to check whether the constraints are
 *            validated or not.
 */
public interface ConstraintsValidator<K> {

    abstract public boolean validatesKeyConstraints(StructuredOverlay overlay, K key);
    
}
