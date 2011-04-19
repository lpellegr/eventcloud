package org.objectweb.proactive.extensions.p2p.structured.validator;

import java.io.Serializable;

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
public abstract class ConstraintsValidator<K> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The key used in order to route the query over the network.
     */
    protected final K key;

    public ConstraintsValidator(K key) {
        this.key = key;
    }

    abstract public boolean validatesKeyConstraints(StructuredOverlay overlay);

    /**
     * Returns the key to reach (i.e. the peer containing this key is the
     * receiver of this message).
     * 
     * @return the key to reach (i.e. the peer containing this key is the
     *         receiver of this message).
     */
    public K getKey() {
        return this.key;
    }

}
