package fr.inria.eventcloud.validator;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.Zone;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;

/**
 * Used by {@link SynchronousMessage}s to known if the current
 * {@link StructuredOverlay} or {@link Zone} which handles the message validates
 * the constraints associated to key contained by the message.
 * 
 * @author lpellegr
 */
public interface AnycastConstraintsValidator<K> extends ConstraintsValidator<K> {

    abstract public boolean validatesKeyConstraints(Zone zone, K key);

}
