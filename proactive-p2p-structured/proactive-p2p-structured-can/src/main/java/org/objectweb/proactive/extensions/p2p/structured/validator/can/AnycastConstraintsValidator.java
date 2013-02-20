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
package org.objectweb.proactive.extensions.p2p.structured.validator.can;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;

/**
 * Used by a {@link Router} to know whether the current
 * {@link StructuredOverlay} which handles the message validates the constraints
 * associated to the key.
 * 
 * @author lpellegr
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 */
public abstract class AnycastConstraintsValidator<E extends Element> extends
        ConstraintsValidator<Coordinate<E>> {

    private static final long serialVersionUID = 140L;

    /**
     * Constructs a new {@link AnycastConstraintsValidator} with the specified
     * {@code key}.
     * 
     * @param key
     *            the key to reach.
     */
    public AnycastConstraintsValidator(Coordinate<E> key) {
        super(key);
    }

    /**
     * Indicates whether the key is contained by the specified {@code zone}.
     * 
     * @param zone
     *            the zone to use in order to perform the check.
     * 
     * @return {@code true} if the zone contains the key, {@code false}
     *         otherwise.
     */
    public abstract boolean validatesKeyConstraints(Zone<E> zone);

}
