/**
 * Copyright (c) 2011-2012 INRIA.
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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;

/**
 * A unicode zone view is a zone view maintaining coordinates as
 * {@link StringElement}s.
 * 
 * @author lpellegr
 */
public class UnicodeZoneView extends ZoneView<StringCoordinate, StringElement> {

    private static final long serialVersionUID = 1L;

    public UnicodeZoneView(StringCoordinate lowerBound,
            StringCoordinate upperBound) {
        super(lowerBound, upperBound);
    }

    public boolean containsLexicographically(StringCoordinate coordinate) {
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            if (this.containsLexicographically(dim, coordinate.getElement(dim)) != 0) {
                return false;
            }
        }

        return true;
    }

    public byte containsLexicographically(byte dimension, StringElement element) {
        if (element == null) {
            return 0;
        }

        if (element.compareLexicographicallyTo(this.upperBound.getElement(dimension)) >= 0) {
            return 1;
        } else if (element.compareLexicographicallyTo(this.lowerBound.getElement(dimension)) < 0) {
            return -1;
        }

        return 0;
    }

}
