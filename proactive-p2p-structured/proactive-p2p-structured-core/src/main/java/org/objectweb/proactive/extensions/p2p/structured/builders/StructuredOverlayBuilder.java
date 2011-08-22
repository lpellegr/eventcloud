package org.objectweb.proactive.extensions.p2p.structured.builders;

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

/**
 * This class is used to build a {@link StructuredOverlay}. This is usefull to
 * define how to build an overlay, especially when the overlay constructor may
 * have several parameters which are not serializable (such as a datastore).
 * Thus, when a {@link Peer} has to be instantiated as an active object or a
 * component, only a builder is pass as parameter. Thanks to this builder is it
 * possible to pass a non-serializable object to an overlay constructor.
 * 
 * @author lpellegr
 */
public abstract class StructuredOverlayBuilder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Defines how to build a {@link StructuredOverlay}.
     * 
     * @return a {@link StructuredOverlay}.
     */
    public abstract StructuredOverlay build();

    /**
     * Builds a {@link StructuredOverlay} from the specified class that extends
     * {@link StructuredOverlay}. This method is a convenient method to build an
     * overlay by using its empty constructor.
     * 
     * @param clazz
     *            the class to instantiate.
     * 
     * @return a {@link StructuredOverlay} from the specified class that extends
     *         {@link StructuredOverlay}.
     */
    public static <T extends StructuredOverlay> StructuredOverlayBuilder build(final Class<T> clazz) {
        return new StructuredOverlayBuilder() {

            private static final long serialVersionUID = 1L;

            @Override
            public T build() {
                try {
                    return clazz.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

}
