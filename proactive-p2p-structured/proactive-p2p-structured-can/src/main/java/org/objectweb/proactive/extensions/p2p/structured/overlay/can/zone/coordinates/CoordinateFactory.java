package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.DoubleElement;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;

/**
 * Factory class for creating {@code Coordinate}s.
 * 
 * @author lpellegr
 */
public class CoordinateFactory {

    /**
     * Creates a new coordinate containing {@link DoubleElement}s initialized
     * with the specified {@code value}.
     * 
     * @param value
     *            the default value used to initialize each element.
     * 
     * @return a new coordinate containing
     *         {@link P2PStructuredProperties#CAN_NB_DIMENSIONS}
     *         {@link DoubleElement}s.
     */
    public static DoubleCoordinate createDoubleCoordinate(double value) {
        DoubleElement[] elts = new DoubleElement[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()];
        for (int i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            elts[i] = new DoubleElement(value);
        }
        return new DoubleCoordinate(elts);
    }

    /**
     * Creates a new coordinate containing {@link StringElement}s initialized
     * with the specified {@code value}.
     * 
     * @param value
     *            the default value used to initialize each element.
     * 
     * @return a new coordinate containing
     *         {@link P2PStructuredProperties#CAN_NB_DIMENSIONS}
     *         {@link StringElement}s.
     */
    public static StringCoordinate createStringCoordinate(String value) {
        StringElement[] elts = new StringElement[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()];
        for (int i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            elts[i] = new StringElement(value);
        }
        return new StringCoordinate(elts);
    }
    
}
