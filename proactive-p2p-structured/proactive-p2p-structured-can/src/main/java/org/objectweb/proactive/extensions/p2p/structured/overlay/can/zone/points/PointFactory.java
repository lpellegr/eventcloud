/**
 * Copyright (c) 2011-2013 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;

/**
 * Factory to create {@link Point}s.
 * 
 * @author lpellegr
 */
public final class PointFactory {

    private PointFactory() {

    }

    /**
     * Creates a new point whose all the {@link StringCoordinate}s are
     * {@code null}.
     * 
     * @return a new point whose all the {@link StringCoordinate}s are
     *         {@code null}.
     */
    public static Point<StringCoordinate> newStringCoordinate() {
        return new Point<StringCoordinate>(
                new StringCoordinate[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()]);
    }

    /**
     * Creates a new point containing {@link StringCoordinate}s initialized with
     * the specified {@code value}.
     * 
     * @param value
     *            the default value used to initialize each coordinate.
     * 
     * @return a new point containing
     *         {@link P2PStructuredProperties#CAN_NB_DIMENSIONS}
     *         {@link StringCoordinate}s.
     */
    public static Point<StringCoordinate> newStringCoordinate(String value) {
        StringCoordinate[] elts =
                new StringCoordinate[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()];

        for (int i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            elts[i] = new StringCoordinate(value);
        }

        return new Point<StringCoordinate>(elts);
    }

    /**
     * Creates a new point containing {@link StringCoordinate}s initialized with
     * the specified {@code value}.
     * 
     * @param value
     *            the default value used to initialize each coordinate.
     * 
     * @return a new point containing
     *         {@link P2PStructuredProperties#CAN_NB_DIMENSIONS}
     *         {@link StringCoordinate}s.
     */
    public static Point<StringCoordinate> newStringCoordinate(Character value) {
        return newStringCoordinate(Character.toString(value));
    }

}
