/**
 * Copyright (c) 2011 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
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

    private CoordinateFactory() {
        
    }
    
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
        DoubleElement[] elts =
                new DoubleElement[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()];
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
        StringElement[] elts =
                new StringElement[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()];
        for (int i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            elts[i] = new StringElement(value);
        }
        return new StringCoordinate(elts);
    }

}
