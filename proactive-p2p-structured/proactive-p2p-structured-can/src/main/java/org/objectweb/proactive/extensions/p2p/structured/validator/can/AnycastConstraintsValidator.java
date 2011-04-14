package org.objectweb.proactive.extensions.p2p.structured.validator.can;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;

/**
 * Used by a {@link Router} to know whether the current {@link StructuredOverlay}
 * which handles the message validates the constraints associated to the key.
 * 
 * @author lpellegr
 * 
 * @param <K>
 *            the key type.
 */
public abstract class AnycastConstraintsValidator<K> extends ConstraintsValidator<K> {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new {@link AnycastConstraintsValidator} with the specified
	 * {@code key}.
	 * 
	 * @param key
	 *            the key to reach.
	 */
	public AnycastConstraintsValidator(K key) {
		super(key);
	}

	/**
	 * Indicates if the key is contained by the specified {@code zone}.
	 * 
	 * @param zone
	 *            the zone to use in order to perform the check.
	 * 
	 * @return {@code true} if the zone contains the key, {@code false}
	 *         otherwise.
	 */
	abstract public boolean validatesKeyConstraints(Zone zone);

}
