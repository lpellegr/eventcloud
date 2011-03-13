package org.objectweb.proactive.extensions.p2p.structured.validator.can;

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
public abstract class AnycastConstraintsValidator<K> extends ConstraintsValidator<K> {

	private static final long serialVersionUID = 1L;

	public AnycastConstraintsValidator(K key) {
		super(key);
	}

	abstract public boolean validatesKeyConstraints(Zone zone);

}
